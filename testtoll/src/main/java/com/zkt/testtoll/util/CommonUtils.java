package com.zkt.testtoll.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

/**
 * 通用工具类
 **/
public class CommonUtils {


    //隐藏键盘
    public static void hideInputBoard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //获取屏幕chicun
    public static int[] getScreenSize(Context context) {
        int[] sizes = new int[2];
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        sizes[0] = metrics.widthPixels;
        sizes[1] = metrics.heightPixels;
        return sizes;
    }

    //获取设备机型
    @NonNull
    public static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }


    //获取App版本
    @NonNull
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
