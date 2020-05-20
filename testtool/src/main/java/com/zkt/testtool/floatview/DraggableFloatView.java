package com.zkt.testtool.floatview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zkt.testtool.LogsListActivity;
import com.zkt.testtool.R;

@SuppressLint("ViewConstructor")
public class DraggableFloatView extends LinearLayout {

    private static final String TAG = DraggableFloatView.class.getSimpleName();

    private Context mContext;
    private TextView btnLogs, btnClose;
    private OnFloatListener mOnFlingListener;
    public DraggableFloatView(final Context context, OnFloatListener flingListener) {
        super(context);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.test_layout_floatview, this);
        btnLogs = findViewById(R.id.test_go);
        btnClose = findViewById(R.id.test_close);
        mOnFlingListener = flingListener;
        btnLogs.setOnTouchListener(new OnTouchListener() {

            //刚按下是起始位置的坐标
            float startDownX, startDownY;
            float downX, downY;
            float moveX, moveY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN");
                        startDownX = downX = motionEvent.getRawX();
                        startDownY = downY = motionEvent.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "ACTION_MOVE");
                        moveX = motionEvent.getRawX();
                        moveY = motionEvent.getRawY();
                        if (mOnFlingListener != null) {
                            mOnFlingListener.onMove(moveX - downX, moveY - downY);
                        }
                        downX = moveX;
                        downY = moveY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP");
                        float upX = motionEvent.getRawX();
                        float upY = motionEvent.getRawY();
                        if (upX == startDownX && upY == startDownY) {
                            return false;
                        } else {
                            return true;
                        }
                }
                return true;
            }
        });
        btnLogs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LogsListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        btnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnFlingListener.onClose();
            }
        });
    }

}
