package com.zkt.testtool.floatview;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import com.zkt.testtool.util.CommonUtils;

import java.util.LinkedList;
import java.util.Set;

/**
 * 悬浮窗基类
 */
public abstract class FloatWindow extends Service {
    static final String TAG = "FloatWindow";

    public int screenSizes[];
    //悬浮窗窗口id
    public static final int DEFAULT_ID = 0;

    //特殊的悬浮窗id，你可能用不到
    public static final int ONGOING_NOTIFICATION_ID = -1;

    //Intent action: 显示与ID相对应的新窗口
    public static final String ACTION_SHOW = "SHOW";

    //Intent action: 根据id显示之前隐藏的悬浮窗
    public static final String ACTION_RESTORE = "RESTORE";


    //Intent action:关闭所有的悬浮窗
    public static final String ACTION_CLOSE_ALL = "CLOSE_ALL";


    //Intent action: 根据存在的id隐藏悬浮窗.为可以重新显示此悬浮窗
    public static final String ACTION_HIDE = "HIDE";


    // 悬浮窗缓存
    static WindowCache sWindowCache = new WindowCache();

    // 系统 services
    WindowManager mWindowManager;
    private NotificationManager mNotificationManager;

    // 是否在前台
    private boolean startedForeground;


    /**
     * 根据id显示一个新的悬浮窗获取或者重新显示之前隐藏的窗口
     */
    public static void show(Context context,
                            Class<? extends FloatWindow> cls, int id) {
        context.startService(getShowIntent(context, cls, id));
    }


    //关闭所有的悬浮窗
    public static void closeAll(Context context,
                                Class<? extends FloatWindow> cls) {
        Intent intent = new Intent(context, cls).setAction(ACTION_CLOSE_ALL);
        context.startService(intent);
    }


    //获取显示悬浮窗Intent
    public static Intent getShowIntent(Context context,
                                       Class<? extends FloatWindow> cls, int id) {
        boolean cached = sWindowCache.isCached(id, cls);
        String action = cached ? ACTION_RESTORE : ACTION_SHOW;
        Uri uri = cached ? Uri.parse("standout://" + cls + '/' + id) : null;
        return new Intent(context, cls).putExtra("id", id).setAction(action)
                .setData(uri);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        screenSizes = CommonUtils.getScreenSize(this);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        startedForeground = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            String action = intent.getAction();
            int id = intent.getIntExtra("id", DEFAULT_ID);

//             this will interfere with getPersistentNotification()
            if (id == ONGOING_NOTIFICATION_ID) {
                throw new RuntimeException(
                        "ID cannot equals FloatWindow.ONGOING_NOTIFICATION_ID");
            }

            if (ACTION_SHOW.equals(action) || ACTION_RESTORE.equals(action)) {
                show(id);
            } else if (ACTION_HIDE.equals(action)) {
                hide(id);
            } else if (ACTION_CLOSE_ALL.equals(action)) {
                closeAll();
            }
        } else {
            Log.w(TAG, "Tried to onStartCommand() with a null intent.");
        }

        // the service is started in foreground in show()
        // so we don't expect Android to kill this service
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeAll();
    }


    //获取悬浮窗的布局参数
    public abstract FloatLayoutParams getParams();


    //获取一个Notification对象
    public abstract Notification getPersistentNotification(int id);


    //显示悬浮窗
    public final synchronized FloatView show(int id) {
        // get the window corresponding to the id
        FloatView cachedWindow = getWindow(id);
        final FloatView window;

        // check cache first
        if (cachedWindow != null) {
            window = cachedWindow;
        } else {
            window = new FloatView(this, id);
        }

        // focus an already shown window
        if (window.visibility == FloatView.VISIBILITY_VISIBLE) {
            Log.d(TAG, "Window " + id + " is already shown.");
            return window;
        }

        window.visibility = FloatView.VISIBILITY_VISIBLE;

        // get the params corresponding to the id
        FloatLayoutParams params = window.getLayoutParams();

        try {
            // add the view to the window manager
            mWindowManager.addView(window, params);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // add view to internal map
        sWindowCache.putCache(id, getClass(), window);

        // get the persistent notification
        Notification notification = getPersistentNotification(id);

        // show the notification
        if (notification != null) {
            notification.flags = notification.flags
                    | Notification.FLAG_NO_CLEAR;

            // only show notification if not shown before
            if (!startedForeground) {
                // tell Android system to show notification
                startForeground(
                        getClass().hashCode() + ONGOING_NOTIFICATION_ID,
                        notification);
                startedForeground = true;
            } else {
                // update notification if shown before
                mNotificationManager.notify(getClass().hashCode()
                        + ONGOING_NOTIFICATION_ID, notification);
            }
        } else {
            // notification can only be null if it was provided before
            if (!startedForeground) {
                throw new RuntimeException("Your FloatWindow service must"
                        + "provide a persistent notification."
                        + "The notification prevents Android"
                        + "from killing your service in low"
                        + "memory situations.");
            }
        }


        return window;
    }

    //隐藏悬浮窗
    public final synchronized void hide(int id) {
        // get the view corresponding to the id
        final FloatView window = getWindow(id);

        if (window == null) {
            throw new IllegalArgumentException("Tried to ic_hide(" + id
                    + ") a null window.");
        }

        // check if ic_hide enabled
        window.visibility = FloatView.VISIBILITY_TRANSITION;
        try {
            // remove the window from the window manager
            mWindowManager.removeView(window);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //关闭悬浮窗
    public final synchronized void close(final int id) {
        // get the view corresponding to the id
        final FloatView window = getWindow(id);

        if (window == null) {
            throw new IllegalArgumentException("Tried to ic_close(" + id
                    + ") a null window.");
        }

        if (window.visibility == FloatView.VISIBILITY_TRANSITION) {
            return;
        }

        // remove hidden notification
        mNotificationManager.cancel(getClass().hashCode() + id);


        window.visibility = FloatView.VISIBILITY_TRANSITION;

        // remove window
        try {
            // remove the window from the window manager
            mWindowManager.removeView(window);

            // remove view from internal map
            sWindowCache.removeCache(id, getClass());

            // if we just released the last window, quit
            if (sWindowCache.getCacheSize(getClass()) == 0) {
                // tell Android to remove the persistent notification
                // the Service will be shutdown by the system on low memory
                startedForeground = false;
                stopForeground(true);
            }
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //关闭所有的悬浮窗
    public final synchronized void closeAll() {
        // add ids to temporary set to avoid concurrent modification
        LinkedList<Integer> ids = new LinkedList<>();
        ids.addAll(getExistingIds());

        // ic_close each window
        for (int id : ids) {
            close(id);
        }
    }

    //获取存在悬浮窗ids
    public final Set<Integer> getExistingIds() {
        return sWindowCache.getCacheIds(getClass());
    }

    //根据id获取悬浮窗
    public final FloatView getWindow(int id) {
        return sWindowCache.getCache(id, getClass());
    }


    //更新悬浮窗布局参数
    public void updateViewLayout(int id, FloatLayoutParams params) {
        FloatView window = getWindow(id);

        if (window == null) {
            throw new IllegalArgumentException("Tried to updateViewLayout("
                    + id + ") a null window.");
        }

        if (window.visibility != FloatView.VISIBILITY_VISIBLE) {
            return;
        }

        try {
            window.setLayoutParams(params);
            mWindowManager.updateViewLayout(window, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //悬浮窗布局
//    public class FloatLayoutParams extends WindowManager.LayoutParams {
//        /**
//         * Special value for x position that represents the right of the screen.
//         */
//        public static final int RIGHT = Integer.MAX_VALUE;
//        /**
//         * Special value for y position that represents the bottom of the
//         * screen.
//         */
//        public static final int BOTTOM = Integer.MAX_VALUE;
//        /**
//         * Special value for x or y position that represents the center of the
//         * screen.
//         */
//        public static final int CENTER = Integer.MIN_VALUE;
//        /**
//         * Special value for x or y position which requests that the system
//         * determine the position.
//         */
//        public static final int AUTO_POSITION = Integer.MIN_VALUE + 1;
//
//        //判断是滑动还是点击
//        public int threshold;
//
//        /**
//         * Optional constraints of the window.
//         */
//        public int minWidth, minHeight, maxWidth, maxHeight;
//
//        /**
//         * @param id The id of the window.
//         */
//        public FloatLayoutParams(int id) {
//            super(200, 200, TYPE_PHONE, FloatLayoutParams.FLAG_NOT_TOUCH_MODAL | FloatLayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
//
//            flags = flags | FloatLayoutParams.FLAG_NOT_FOCUSABLE;
//
//            x = getX(id, width);
//            y = getY(id, height);
//
//            gravity = Gravity.TOP | Gravity.LEFT;
//
//            threshold = 10;
//            minWidth = minHeight = 0;
//            maxWidth = maxHeight = Integer.MAX_VALUE;
//        }
//
//        /**
//         * @param id The id of the window.
//         * @param w  The width of the window.
//         * @param h  The height of the window.
//         */
//        public FloatLayoutParams(int id, int w, int h) {
//            this(id);
//            width = w;
//            height = h;
//        }
//
//        /**
//         * @param id   The id of the window.
//         * @param w    The width of the window.
//         * @param h    The height of the window.
//         * @param xpos The x position of the window.
//         * @param ypos The y position of the window.
//         */
//        public FloatLayoutParams(int id, int w, int h, int xpos, int ypos) {
//            this(id, w, h);
//
//            if (xpos != AUTO_POSITION) {
//                x = xpos;
//            }
//            if (ypos != AUTO_POSITION) {
//                y = ypos;
//            }
//
//            int width = screenSizes[0];
//            int height = screenSizes[1];
//
//            if (x == RIGHT) {
//                x = width - w;
//            } else if (x == CENTER) {
//                x = (width - w) / 2;
//            }
//
//            if (y == BOTTOM) {
//                y = height - h;
//            } else if (y == CENTER) {
//                y = (height - h) / 2;
//            }
//        }
//
//        /**
//         * @param id        The id of the window.
//         * @param w         The width of the window.
//         * @param h         The height of the window.
//         * @param xpos      The x position of the window.
//         * @param ypos      The y position of the window.
//         * @param minWidth  The minimum width of the window.
//         * @param minHeight The mininum height of the window.
//         */
//        public FloatLayoutParams(int id, int w, int h, int xpos, int ypos,
//                                    int minWidth, int minHeight) {
//            this(id, w, h, xpos, ypos);
//
//            this.minWidth = minWidth;
//            this.minHeight = minHeight;
//        }
//
//
//        // helper to create cascading windows
//        private int getX(int id, int width) {
//            int displayWidth = screenSizes[0];
//
//            int types = sWindowCache.size();
//
//            int initialX = 100 * types;
//            int variableX = 100 * id;
//            int rawX = initialX + variableX;
//
//            return rawX % (displayWidth - width);
//        }
//
//        // helper to create cascading windows
//        private int getY(int id, int height) {
//            int displayWidth = screenSizes[0];
//            int displayHeight = screenSizes[1];
//
//            int types = sWindowCache.size();
//
//            int initialY = 100 * types;
//            int variableY = x + 200 * (100 * id) / (displayWidth - width);
//
//            int rawY = initialY + variableY;
//
//            return rawY % (displayHeight - height);
//        }
//
//    }

}
