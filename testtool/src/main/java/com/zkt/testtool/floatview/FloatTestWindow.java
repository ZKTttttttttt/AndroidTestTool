package com.zkt.testtool.floatview;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.zkt.testtool.R;
import com.zkt.testtool.util.CommonUtils;

/**
 * 具体的悬浮窗
 */
public class FloatTestWindow extends FloatWindow {


    @Override
    public FloatLayoutParams getParams() {
        FloatLayoutParams params = new FloatLayoutParams(CommonUtils.getScreenSize(getApplicationContext()),
                screenSizes[0] / 5, screenSizes[0] / 5,
                FloatLayoutParams.RIGHT, FloatLayoutParams.CENTER,
                0, 0);
        return params;
    }

    @Override
    public Notification getPersistentNotification(int id) {
        int icon = getApplicationInfo().icon;
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = getString(R.string.test_name);
        String tickerText = getString(R.string.click_show);
        Intent notificationIntent = FloatWindow.getShowIntent(this, FloatTestWindow.class, id);
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
}
