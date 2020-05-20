package com.zkt.testapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zkt.testtool.TestToolManager;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "哈哈哈";
    TextView btnCrash, btnLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCrash = (TextView) findViewById(R.id.btncrash);
        btnLogs = (TextView) findViewById(R.id.btnlogs);
        btnCrash.setOnClickListener(this);
        btnLogs.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btncrash:
                int i = 10 / 0;
                break;
            case R.id.btnlogs:
                if (commonROMPermissionCheck(MainActivity.this)) {
                    TestToolManager.initTestTool(MainActivity.this);
                } else {
                    requestAlertWindowPermission();
                }
                printlogs();
                break;
        }
    }

    private void printlogs() {
        btnLogs.postDelayed(new Runnable() {
            @Override
            public void run() {
                printlogs();
            }
        }, 5000);
        android.util.Log.v(TAG, System.currentTimeMillis() + "VVVVVVVVVVVV");
        android.util.Log.d(TAG, System.currentTimeMillis() + "DDDDDDDDDDDD");
        android.util.Log.i(TAG, System.currentTimeMillis() + "IIIIIIIIIIII");
        android.util.Log.w(TAG, System.currentTimeMillis() + "WWWWWWWWWWWW");
        android.util.Log.e(TAG, System.currentTimeMillis() + "EEEEEEEEEEEE");
    }

    private boolean commonROMPermissionCheck(Context context) {
        Boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                Settings.canDrawOverlays(context);
                result = (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return result;
    }

    //申请权限
    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE);
    }

    private static final int REQUEST_CODE = 1;

    @Override
    //处理回调
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Log.i(TAG, "onActivityResult granted");
                    TestToolManager.initTestTool(MainActivity.this);
                } else {
                    Log.i(TAG, "onActivityResult noGranted!");
                }
            }
        }
    }
}
