package com.zkt.testtoll.floatview;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.zkt.testtoll.LogsListActivity;
import com.zkt.testtoll.util.CommonUtils;

import java.util.LinkedList;
import java.util.Set;


public class FloatWindow extends Service {
    static final String TAG = "FloatWindow";

    public int sizes[];
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


    // internal map of ids to shown/hidden views
    static WindowCache sWindowCache = new WindowCache();

    // internal system services
    WindowManager mWindowManager;
    private NotificationManager mNotificationManager;

    // internal state variables
    private boolean startedForeground;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sizes = CommonUtils.getScreenSize(this);
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

            // this will interfere with getPersistentNotification()
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
    public StandOutLayoutParams getParams(int id) {
        return new FloatWindow.StandOutLayoutParams(id, sizes[0] / 5, sizes[0] / 5,
                FloatWindow.StandOutLayoutParams.RIGHT, FloatWindow.StandOutLayoutParams.CENTER, 0, 0);

    }


    //获取一个Notification对象
    public Notification getPersistentNotification(int id) {
        int icon = getApplicationInfo().icon;
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = "新点测试组件";
        String tickerText = "点击显示测试悬浮窗";

        // getPersistentNotification() is called for every new window
        // so we replace the old notification with a new one that has
        // a bigger id
        Intent notificationIntent = FloatWindow.getShowIntent(this, TestToolFloatView.class, id);

        PendingIntent contentIntent = null;

        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0,
                    notificationIntent,
                    // flag updates existing persistent notification
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(c);
        builder.setContentTitle(contentTitle);
        builder.setContentText(tickerText);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(icon);
        builder.setWhen(when);
        return builder.build();
    }


    /**
     * Show or restore a window corresponding to the id. Return the window that
     * was shown/restored.
     *
     * @param id The id of the window.
     * @return The window shown.
     */
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
        StandOutLayoutParams params = window.getLayoutParams();

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

    /**
     * Hide a window corresponding to the id. Show a notification for the hidden
     * window.
     *
     * @param id The id of the window.
     */
    public final synchronized void hide(int id) {
        // get the view corresponding to the id
        final FloatView window = getWindow(id);

        if (window == null) {
            throw new IllegalArgumentException("Tried to ic_hide(" + id
                    + ") a null window.");
        }


        // check if ic_hide enabled
        window.visibility = FloatView.VISIBILITY_TRANSITION;

        // get animation
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        try {
            // animate
            if (animation != null) {
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // remove the window from the window manager
                        mWindowManager.removeView(window);
                        window.visibility = FloatView.VISIBILITY_GONE;
                    }
                });
                window.getChildAt(0).startAnimation(animation);
            } else {
                // remove the window from the window manager
                mWindowManager.removeView(window);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Close a window corresponding to the id.
     *
     * @param id The id of the window.
     */
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

        // get animation
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        // remove window
        try {
            // animate
            if (animation != null) {
                animation.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // remove the window from the window manager
                        mWindowManager.removeView(window);
                        window.visibility = FloatView.VISIBILITY_GONE;

                        // remove view from internal map
                        sWindowCache.removeCache(id,
                                FloatWindow.this.getClass());

                        // if we just released the last window, quit
                        if (getExistingIds().size() == 0) {
                            // tell Android to remove the persistent
                            // notification
                            // the Service will be shutdown by the system on low
                            // memory
                            startedForeground = false;
                            stopForeground(true);
                        }
                    }
                });
                window.getChildAt(0).startAnimation(animation);
            } else {
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
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Close all existing windows.
     */
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


    /**
     * Internal touch handler for handling moving the window.
     *
     * @param id
     * @param window
     * @param view
     * @param event
     * @return
     * @see {@link View#onTouchEvent(MotionEvent)}
     */
    public boolean onTouchHandleMove(int id, FloatView window, View view,
                                     MotionEvent event) {
        StandOutLayoutParams params = window.getLayoutParams();

        // how much you have to move in either direction in order for the
        // gesture to be a move and not tap

        int totalDeltaX = window.touchInfo.lastX - window.touchInfo.firstX;
        int totalDeltaY = window.touchInfo.lastY - window.touchInfo.firstY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getRawX() - window.touchInfo.lastX;
                int deltaY = (int) event.getRawY() - window.touchInfo.lastY;

                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                if (window.touchInfo.moving
                        || Math.abs(totalDeltaX) >= params.threshold
                        || Math.abs(totalDeltaY) >= params.threshold) {
                    window.touchInfo.moving = true;

                    // update the position of the window
                    if (event.getPointerCount() == 1) {
                        params.x += deltaX;
                        params.y += deltaY;
                    }

                    window.setPosition(params.x, params.y);
                }
                break;
            case MotionEvent.ACTION_UP:
                window.touchInfo.moving = false;
                if (event.getPointerCount() == 1) {
                    //判断是否是点击事件
                    boolean tap = Math.abs(totalDeltaX) < params.threshold && Math.abs(totalDeltaY) < params.threshold;
                    if (tap) {
                        Intent intent = new Intent(this, LogsListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }

                break;
        }

        return true;
    }


    //更新悬浮窗布局参数
    public void updateViewLayout(int id, StandOutLayoutParams params) {
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

    /**
     * LayoutParams specific to floating StandOut windows.
     *
     * @author Mark Wei <markwei@gmail.com>
     */
    public class StandOutLayoutParams extends WindowManager.LayoutParams {
        /**
         * Special value for x position that represents the right of the screen.
         */
        public static final int RIGHT = Integer.MAX_VALUE;
        /**
         * Special value for y position that represents the bottom of the
         * screen.
         */
        public static final int BOTTOM = Integer.MAX_VALUE;
        /**
         * Special value for x or y position that represents the center of the
         * screen.
         */
        public static final int CENTER = Integer.MIN_VALUE;
        /**
         * Special value for x or y position which requests that the system
         * determine the position.
         */
        public static final int AUTO_POSITION = Integer.MIN_VALUE + 1;

        //判断是滑动还是点击
        public int threshold;

        /**
         * Optional constraints of the window.
         */
        public int minWidth, minHeight, maxWidth, maxHeight;

        /**
         * @param id The id of the window.
         */
        public StandOutLayoutParams(int id) {
            super(200, 200, TYPE_PHONE, StandOutLayoutParams.FLAG_NOT_TOUCH_MODAL | StandOutLayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);

            flags = flags | StandOutLayoutParams.FLAG_NOT_FOCUSABLE;

            x = getX(id, width);
            y = getY(id, height);

            gravity = Gravity.TOP | Gravity.LEFT;

            threshold = 10;
            minWidth = minHeight = 0;
            maxWidth = maxHeight = Integer.MAX_VALUE;
        }

        /**
         * @param id The id of the window.
         * @param w  The width of the window.
         * @param h  The height of the window.
         */
        public StandOutLayoutParams(int id, int w, int h) {
            this(id);
            width = w;
            height = h;
        }

        /**
         * @param id   The id of the window.
         * @param w    The width of the window.
         * @param h    The height of the window.
         * @param xpos The x position of the window.
         * @param ypos The y position of the window.
         */
        public StandOutLayoutParams(int id, int w, int h, int xpos, int ypos) {
            this(id, w, h);

            if (xpos != AUTO_POSITION) {
                x = xpos;
            }
            if (ypos != AUTO_POSITION) {
                y = ypos;
            }

            int width = sizes[0];
            int height = sizes[1];

            if (x == RIGHT) {
                x = width - w;
            } else if (x == CENTER) {
                x = (width - w) / 2;
            }

            if (y == BOTTOM) {
                y = height - h;
            } else if (y == CENTER) {
                y = (height - h) / 2;
            }
        }

        /**
         * @param id        The id of the window.
         * @param w         The width of the window.
         * @param h         The height of the window.
         * @param xpos      The x position of the window.
         * @param ypos      The y position of the window.
         * @param minWidth  The minimum width of the window.
         * @param minHeight The mininum height of the window.
         */
        public StandOutLayoutParams(int id, int w, int h, int xpos, int ypos,
                                    int minWidth, int minHeight) {
            this(id, w, h, xpos, ypos);

            this.minWidth = minWidth;
            this.minHeight = minHeight;
        }


        // helper to create cascading windows
        private int getX(int id, int width) {
            Display display = mWindowManager.getDefaultDisplay();
            int displayWidth = display.getWidth();

            int types = sWindowCache.size();

            int initialX = 100 * types;
            int variableX = 100 * id;
            int rawX = initialX + variableX;

            return rawX % (displayWidth - width);
        }

        // helper to create cascading windows
        private int getY(int id, int height) {
            int displayWidth = sizes[0];
            int displayHeight = sizes[1];

            int types = sWindowCache.size();

            int initialY = 100 * types;
            int variableY = x + 200 * (100 * id) / (displayWidth - width);

            int rawY = initialY + variableY;

            return rawY % (displayHeight - height);
        }

    }

}
