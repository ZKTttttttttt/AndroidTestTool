package com.zkt.testtool.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.zkt.testtool.R;

/**
 * 通用工具类
 **/
public class CommonUtils {

    /**
     * 隐藏软键盘(只适用于Activity，不适用于Fragment)
     */
    public static void hideInputBoard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
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


    //复制错误信息到剪切板
    public static void copyInfoToClipboard(Context context, String s) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && !TextUtils.isEmpty(s)) {
            ClipData clip = ClipData.newPlainText(context.getResources().getString(R.string.error_details_clipboard_label), s);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
        }
    }
}
