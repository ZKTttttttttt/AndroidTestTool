package com.zkt.testtool;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


import com.zkt.testtool.logcat.Level;
import com.zkt.testtool.logcat.Log;
import com.zkt.testtool.logcat.LogAdapter;
import com.zkt.testtool.logcat.Logcat;
import com.zkt.testtool.util.CommonUtils;
import com.zkt.testtool.util.PrefUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 描述：日志输出界面
 */
public class LogsListActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    public static final int MAX_LOG_ITEMS = 500;

    private RecyclerView mRecyclerView;
    private ImageView btnSearch;
    private TextView btnSetting, btnSelect;
    private EditText etKeyword;

    private LogAdapter mRecyclerAdapter;

    private Logcat mLogcat;
    private Handler mLogHandler;


    private int mToolbarColor;//ToolBar颜色

    private RelativeLayout toolbar;//顶部工具栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        toolbar = (RelativeLayout) findViewById(R.id.test_nb_title);
        Spinner spinner = (Spinner) findViewById(R.id.test_spinner);
        btnSetting = (TextView) findViewById(R.id.test_setting);
        btnSearch = (ImageView) findViewById(R.id.test_search);
        etKeyword = (EditText) findViewById(R.id.test_keyword);
        btnSelect = (TextView) findViewById(R.id.test_select);
        mRecyclerView = (RecyclerView) findViewById(R.id.activity_main_recyclerview);

        //根据保存的日志筛选级别设置ToolBar的颜色
        mToolbarColor = PrefUtils.getLevel(this).getColor();
        toolbar.setBackgroundColor(mToolbarColor);

        initSpinner(spinner);

        initRecyclerView();

    }


    //初始化下拉框
    private void initSpinner(Spinner spinner) {
        final ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                LogsListActivity.this,
                R.array.log_levels,
                R.layout.layout_spinner_item);

        spinnerAdapter.setDropDownViewResource(R.layout.layout_spinner_item_dropdown);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(PrefUtils.getLevel(this).ordinal());
        spinner.setOnItemSelectedListener(this);
    }

    //初始化RecyclerView
    private void initRecyclerView() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mRecyclerAdapter = new LogAdapter();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    //初始化Search和Setting
    private void initEvent() {
        btnSelect.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
        final String filter = PrefUtils.getSearchFilter(this);
        if (!TextUtils.isEmpty(filter)) {
            if (mLogcat != null) {
                etKeyword.setText(filter);
                mLogcat.setSearchFilter(filter);
            }
            etKeyword.clearFocus();
        }
    }


    @Override
    public void onClick(View view) {
        //到设置界面
        if (view == btnSetting) {
            startActivity(new Intent(LogsListActivity.this, SettingsActivity.class));
        }
        //进行日志过滤
        if (view == btnSearch) {
            String key = etKeyword.getText().toString();
            if (TextUtils.isEmpty(key)) {
                PrefUtils.removeSearchFilter(LogsListActivity.this);
                mLogcat.setSearchFilter("");
            } else {
                btnSearch.clearFocus();
                PrefUtils.setSearchFilter(LogsListActivity.this, key);
                if (mLogcat != null) {
                    mLogcat.setSearchFilter(key);
                }
            }
            etKeyword.clearFocus();
            CommonUtils.hideInputBoard(LogsListActivity.this);
        }

        //选择日志
        if (view == btnSelect) {
            //将选中日志复制到剪切板
            if (mRecyclerAdapter.isCanSelect()) {
                StringBuffer logsDetails = new StringBuffer();
                for (int i = 0; i < mRecyclerAdapter.getmLogList().size(); i++) {
                    if (mRecyclerAdapter.getmLogList().get(i).isSelected()) {
                        logsDetails.append(mRecyclerAdapter.getmLogList().get(i).getMessage() + "\n");
                        mRecyclerAdapter.getmLogList().get(i).setSelected(false);
                    }
                }
                CommonUtils.copyInfoToClipboard(LogsListActivity.this, logsDetails.toString());
                mRecyclerAdapter.setSelectState(false);
                btnSelect.setText(getResources().getString(R.string.select));
            } else {
                //进入选择模式
                mRecyclerAdapter.setSelectState(true);
                btnSelect.setText(getResources().getString(R.string.confirm));
            }
            mRecyclerAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        //根据选择的日志筛选级别进行过滤
        final Level level = Level.get(position);
        PrefUtils.setLevel(this, level);
        mLogcat.setLevel(level);
        changeToolbarBackgroundColor(level.getColor());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLogHandler == null) mLogHandler = new Handler(this);
        if (mLogcat == null) mLogcat = new Logcat(
                mLogHandler,
                PrefUtils.getLevel(this),
                PrefUtils.getFormat(this),
                PrefUtils.getBuffer(this));
        mLogcat.start();
        initEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLogHandler = null;
        if (mLogcat != null) mLogcat.stop();
        mLogcat = null;

    }

    //动画改变ToolBar的颜色
    private void changeToolbarBackgroundColor(final int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mToolbarColor, toColor);
        colorAnimation.setDuration(300);
        colorAnimation.setInterpolator(new AccelerateInterpolator());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                toolbar.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mToolbarColor = toColor;
            }
        });
        colorAnimation.start();
    }

    //刷新日志
    private void updateLogs(final List<Log> logList) {
        boolean mAutoScroll = true;
        final boolean scroll = mAutoScroll;
        int currentSize = mRecyclerAdapter.getItemCount();
        mRecyclerAdapter.addAll(currentSize, logList);
        if (scroll) mRecyclerView.smoothScrollToPosition(mRecyclerAdapter.getItemCount() - 1);
    }

    private static class Handler extends android.os.Handler {

        private final WeakReference<LogsListActivity> mActivity;

        public Handler(LogsListActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LogsListActivity activity = mActivity.get();
            if (activity == null) return;
            switch (msg.what) {
                case Logcat.CAT_LOGS:
                    @SuppressWarnings("unchecked")
                    List<Log> catLogs = (List<Log>) msg.obj;
                    activity.updateLogs(catLogs);
                    break;
                case Logcat.CLEAR_LOGS:
                    if (!activity.mRecyclerView.canScrollVertically(-1)) return;
                    if (activity.mRecyclerAdapter.getItemCount() > MAX_LOG_ITEMS)
                        activity.mRecyclerAdapter.removeFirstItems(
                                activity.mRecyclerAdapter.getItemCount() - MAX_LOG_ITEMS);
                    break;
                case Logcat.REMOVE_LOGS:
                    activity.mRecyclerAdapter.clear();
                    break;
            }
        }

    }

}
