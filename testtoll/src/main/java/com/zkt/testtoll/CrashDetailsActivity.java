
package com.zkt.testtoll;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import com.zkt.testtoll.crash.CrashCatcher;
import com.zkt.testtoll.util.CommonUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 描述：崩溃捕捉界面
 */
public final class CrashDetailsActivity extends Activity {
    private String errorInformation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initErrorData();
        initErrorDialog();
    }

    private void initErrorDialog() {
        AlertDialog dialog = new AlertDialog
                .Builder(CrashDetailsActivity.this)
                .setTitle(R.string.error_details_title)
                .setMessage(errorInformation)
                .setPositiveButton(R.string.error_details_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNeutralButton(R.string.error_details_copy,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                copyErrorToClipboard();
                                finish();
                            }
                        })
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.crash_log_text_size));
        }
    }

    //获取异常信息
    private void initErrorData() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        StringBuffer errorDetails = new StringBuffer();
        errorDetails.append("Build version: " + CommonUtils.getVersionName(this) + " \n");
        errorDetails.append("Current date: " + dateFormat.format(new Date()) + " \n");
        errorDetails.append("Device: " + CommonUtils.getDeviceModelName() + " \n \n");
        errorDetails.append("Stack trace:  \n");
        errorDetails.append(getIntent().getStringExtra(CrashCatcher.EXTRA_ERRORS));
        errorInformation = errorDetails.toString();
    }

    //复制错误信息到剪切板
    private void copyErrorToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(getString(R.string.error_details_clipboard_label), errorInformation);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(CrashDetailsActivity.this, R.string.error_details_copied, Toast.LENGTH_SHORT).show();
        }
    }
}
