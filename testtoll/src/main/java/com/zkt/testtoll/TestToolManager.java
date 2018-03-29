package com.zkt.testtoll;

import android.content.Context;

import com.zkt.testtoll.crash.CrashCatcher;
import com.zkt.testtoll.floatview.FloatWindow;
import com.zkt.testtoll.floatview.TestToolFloatView;


/**
 * 描述：测试组件管理类
 */
public class TestToolManager {
    public static void initTestTool(Context context) {
        //开启异常捕捉
        CrashCatcher.install(context);
        //悬浮窗开启
        FloatWindow.closeAll(context, TestToolFloatView.class);
        FloatWindow.show(context, TestToolFloatView.class, FloatWindow.DEFAULT_ID);
    }

    public static void closeTestTool(Context context) {
        //关闭悬浮窗
        FloatWindow.closeAll(context, TestToolFloatView.class);
    }
}
