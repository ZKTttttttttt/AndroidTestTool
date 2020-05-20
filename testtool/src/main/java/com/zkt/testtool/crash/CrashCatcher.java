
package com.zkt.testtool.crash;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zkt.testtool.CrashDetailsActivity;
import com.zkt.testtool.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * 崩溃异常捕捉器
 */
public class CrashCatcher {

    private final static String TAG = "CrashCatcher";

    //Extras传递Tag
    public static final String EXTRA_ERRORS = "extra_errors";

    //SharedPreferences存储
    private static final String SP_SAVE = "sp_save";
    private static final String SP_CRASH_LASTTIME = "sp_last_crash_time";

    //当前CrashCatcher标识
    private static final String CAOC_HANDLER_PACKAGE_NAME = "com.zkt.testtoll.crash.CrashCatcher";
    //系统CrashCatcher标识
    private static final String DEFAULT_HANDLER_PACKAGE_NAME = "com.android.internal.os";

    //Intent传递数据限制不大于128K
    private static final int MAX_STACK_TRACE_SIZE = 131071; //128 KB - 1

    /*
     *定义应用程序崩溃之间必须经过的时间，以确定我们不在崩溃循环中。
     * 如果这一次发生的崩溃距离上次小于此时间，错误活动将不会启动。默认值是3000。
     */
    private static int minTimeBetweenCrashesMs = 3000;

    //展示错误信息的ErrorActivity类
    private static Class<? extends Activity> errorActivityClass = CrashDetailsActivity.class;
    ;

    //注册CrashCatcher
    public static void install(@Nullable final Context context) {
        try {
            if (context == null) {
                Log.e(TAG, context.getString(R.string.init_error));
            } else {
                final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

                if (oldHandler != null && oldHandler.getClass().getName().startsWith(CAOC_HANDLER_PACKAGE_NAME)) {
                    Log.e(TAG, context.getString(R.string.alreadinit));
                } else {
                    if (oldHandler != null && !oldHandler.getClass().getName().startsWith(DEFAULT_HANDLER_PACKAGE_NAME)) {
                        Log.e(TAG, context.getString(R.string.shouldone) + oldHandler.getClass().getName());
                    }

                    final Application application = (Application) context.getApplicationContext();

                    //设置默认异常处理器
                    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(Thread thread, final Throwable throwable) {
                            if (hasCrashedInTheLastSeconds(application)) {
                                Log.e(TAG, application.getString(R.string.error_more), throwable);
                                if (oldHandler != null) {
                                    oldHandler.uncaughtException(thread, throwable);
                                    return;
                                }
                            } else {
                                setLastCrashTimestamp(application, new Date().getTime());

                                if (isStackTraceLikelyConflictive(throwable, errorActivityClass)) {
                                    Log.e(TAG, application.getString(R.string.cannot_show));
                                    if (oldHandler != null) {
                                        oldHandler.uncaughtException(thread, throwable);
                                        return;
                                    }
                                } else {
                                    Log.e(TAG, application.getString(R.string.excute_catch), throwable);
                                    final Intent intent = new Intent(application, errorActivityClass);
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    throwable.printStackTrace(pw);
                                    String stackTraceString = sw.toString();

                                    //限制传递的错误信息不大于128KB
                                    if (stackTraceString.length() > MAX_STACK_TRACE_SIZE) {
                                        String disclaimer = " [stack trace too large]";
                                        stackTraceString = stackTraceString.substring(0, MAX_STACK_TRACE_SIZE - disclaimer.length()) + disclaimer;
                                    }
                                    intent.putExtra(EXTRA_ERRORS, stackTraceString);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    application.startActivity(intent);

                                }
                            }
                            killCurrentProcess();

                        }
                    });
                }
                Log.i(TAG, context.getString(R.string.init_finish));
            }
        } catch (Throwable t) {
            Log.e(TAG, context.getString(R.string.init_error_check), t);
        }
    }


    //检测Application和ErrorActivity是否发生异常
    private static boolean isStackTraceLikelyConflictive(@NonNull Throwable throwable, @NonNull Class<? extends Activity> activityClass) {
        do {
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                if ((element.getClassName().equals("android.app.ActivityThread") && element.getMethodName().equals("handleBindApplication")) || element.getClassName().equals(activityClass.getName())) {
                    return true;
                }
            }
        } while ((throwable = throwable.getCause()) != null);
        return false;
    }

    //杀死当前进程
    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    //设置崩溃的时间
    @SuppressLint("ApplySharedPref")
    private static void setLastCrashTimestamp(@NonNull Context context, long timestamp) {
        context.getSharedPreferences(SP_SAVE, Context.MODE_PRIVATE).edit().putLong(SP_CRASH_LASTTIME, timestamp).commit();
    }

    //获取上次崩溃的时间
    private static long getLastCrashTimestamp(@NonNull Context context) {
        return context.getSharedPreferences(SP_SAVE, Context.MODE_PRIVATE).getLong(SP_CRASH_LASTTIME, -1);
    }

    //是否在config.getMinTimeBetweenCrashesMs()时间内连续崩溃
    private static boolean hasCrashedInTheLastSeconds(@NonNull Context context) {
        long lastTimestamp = getLastCrashTimestamp(context);
        long currentTimestamp = new Date().getTime();
        return (lastTimestamp <= currentTimestamp && currentTimestamp - lastTimestamp < minTimeBetweenCrashesMs);
    }

}
