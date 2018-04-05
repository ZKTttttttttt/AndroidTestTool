package com.zkt.testapp;

import android.app.Application;

import com.zkt.testtool.TestToolManager;


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
