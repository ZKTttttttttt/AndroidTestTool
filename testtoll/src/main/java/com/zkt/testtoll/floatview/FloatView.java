package com.zkt.testtoll.floatview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
    public FloatWindow.StandOutLayoutParams layoutParams;

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
        this.layoutParams = context.getParams(id);
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
    public FloatWindow.StandOutLayoutParams getLayoutParams() {
        FloatWindow.StandOutLayoutParams params = (FloatWindow.StandOutLayoutParams) super
                .getLayoutParams();
        if (params == null) {
            params = layoutParams;
        }
        return params;
    }

    //更新悬浮窗位置
    public void setPosition(int x, int y) {
        FloatWindow.StandOutLayoutParams mParams = getLayoutParams();
        if (mParams != null) {
            mParams.x = x;
            mParams.y = y;
            mContext.updateViewLayout(id, mParams);
        }
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
        boolean consumed = mContext.onTouchHandleMove(id, FloatView.this, view, motionEvent);
        return consumed;
    }
}