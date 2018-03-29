package com.zkt.testtoll.floatview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zkt.testtoll.LogsListActivity;
import com.zkt.testtoll.R;


public class FloatView extends FrameLayout implements View.OnClickListener, View.OnTouchListener {
    public static final int VISIBILITY_GONE = 0;
    public static final int VISIBILITY_VISIBLE = 1;
    public static final int VISIBILITY_TRANSITION = 2;

    //悬浮窗id
    public int id;

    //悬浮窗可见性
    public int visibility;

    //悬浮窗布局参数
    public FloatLayoutParams layoutParams;

    //悬浮窗点击信息
    public TouchInfo touchInfo;

    //悬浮窗
    private final FloatWindow mContext;

    //悬浮窗隐藏、关闭按钮
    ImageView btnHide, btnClose;

    public FloatView(Context context) {
        super(context);
        mContext = null;
    }

    public FloatView(final FloatWindow context, final int id) {
        super(context);
        mContext = context;
        this.id = id;
        this.layoutParams = context.getParams();
        this.touchInfo = new TouchInfo();
        touchInfo.ratio = (float) layoutParams.width / layoutParams.height;
        initView();

    }

    private void initView() {
        final View decorations = LayoutInflater.from(mContext).inflate(R.layout.layout_floatview, null);
        btnHide = (ImageView) decorations.findViewById(R.id.hide);
        btnClose = (ImageView) decorations.findViewById(R.id.close);
        btnHide.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        decorations.findViewById(R.id.go).setOnTouchListener(this);
        this.addView(decorations);
    }

    //获取悬浮窗布局参数
    @Override
    public FloatLayoutParams getLayoutParams() {
        FloatLayoutParams params = (FloatLayoutParams) super.getLayoutParams();
        if (params == null) {
            params = layoutParams;
        }
        return params;
    }


    @Override
    public void onClick(View view) {
        if (view == btnHide) {
            mContext.hide(id);
        } else {
            mContext.close(id);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        boolean consumed = onTouchHandleMove(FloatView.this, motionEvent);
        return consumed;
    }

    //悬浮窗触摸事件处理
    public boolean onTouchHandleMove(FloatView window,
                                     MotionEvent event) {
        FloatLayoutParams params = window.getLayoutParams();

        int totalDeltaX = window.touchInfo.lastX - window.touchInfo.firstX;
        int totalDeltaY = window.touchInfo.lastY - window.touchInfo.firstY;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                window.touchInfo.firstX = window.touchInfo.lastX;
                window.touchInfo.firstY = window.touchInfo.lastY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) event.getRawX() - window.touchInfo.lastX;
                int deltaY = (int) event.getRawY() - window.touchInfo.lastY;

                window.touchInfo.lastX = (int) event.getRawX();
                window.touchInfo.lastY = (int) event.getRawY();

                if (window.touchInfo.moving
                        || Math.abs(totalDeltaX) >= params.threshold
                        || Math.abs(totalDeltaY) >= params.threshold) {
                    window.touchInfo.moving = true;

                    // update the position of the window
                    if (event.getPointerCount() == 1) {
                        params.x += deltaX;
                        params.y += deltaY;
                    }

                    window.setPosition(params.x, params.y);
                }
                break;
            case MotionEvent.ACTION_UP:
                window.touchInfo.moving = false;
                if (event.getPointerCount() == 1) {
                    //判断是否是点击事件
                    boolean tap = Math.abs(totalDeltaX) < params.threshold && Math.abs(totalDeltaY) < params.threshold;
                    if (tap) {
                        Intent intent = new Intent(mContext, LogsListActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.getApplication().startActivity(intent);
                    }
                }

                break;
        }

        return true;
    }

    //更新悬浮窗位置
    public void setPosition(int x, int y) {
        FloatLayoutParams mParams = getLayoutParams();
        if (mParams != null) {
            mParams.x = x;
            mParams.y = y;
            mContext.updateViewLayout(id, mParams);
        }
    }
}