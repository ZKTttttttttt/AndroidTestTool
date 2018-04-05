package com.zkt.testapp;

import android.app.Application;

import com.zkt.testtool.TestToolManager;

/**
 * 作者： ZKT
 * 创建时间：2018/3/29.
 * 版本： [1.0, 2018/3/29]
 * 版权： 江苏国泰新点软件有限公司
 * 描述：
 **/

public class TestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TestToolManager.initTestTool(this);
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        TestToolManager.closeTestTool(this);
    }
}
