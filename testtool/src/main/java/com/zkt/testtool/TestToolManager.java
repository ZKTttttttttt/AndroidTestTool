package com.zkt.testtool;

import android.content.Context;

import com.zkt.testtool.crash.CrashCatcher;
import com.zkt.testtool.floatview.DraggableFloatWindow;


/**
 * 描述：测试组件管理类
 */
public class TestToolManager {

    public static void initTestTool(Context context) {
        //开启异常捕捉
        CrashCatcher.install(context);
        //悬浮窗开启
        DraggableFloatWindow.getDraggableFloatWindow(context, null).show();

    }

    public static void closeTestTool(Context context) {
        //关闭悬浮窗
        DraggableFloatWindow.getDraggableFloatWindow(context, null).dismiss();
    }



}
