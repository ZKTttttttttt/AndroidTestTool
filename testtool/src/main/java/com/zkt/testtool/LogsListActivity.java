package com.zkt.testtool;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    private ListView lvlogs;
    private ImageView btnSearch;
    private TextView btnSelect, btnAutoFresh,btnClear;
    private EditText etKeyword;

    private LogAdapter logsAdapter;

    private Logcat mLogcat;
    private Handler mLogHandler;

    boolean mAutoScroll = false;//自动滑动到最新log处

    private int mToolbarColor;//ToolBar颜色

    private LinearLayout toolbar;//顶部工具栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_logs);
        toolbar = (LinearLayout) findViewById(R.id.test_nb_title);
        Spinner spinner = (Spinner) findViewById(R.id.test_spinner);
        btnSearch = (ImageView) findViewById(R.id.test_search);
        etKeyword = (EditText) findViewById(R.id.test_keyword);
        btnSelect = (TextView) findViewById(R.id.test_select);
        btnAutoFresh = (TextView) findViewById(R.id.test_auto);
        btnClear = (TextView) findViewById(R.id.test_clear);
        lvlogs = (ListView) findViewById(R.id.activity_main_recyclerview);
        lvlogs.setStackFromBottom(true);
        btnSelect.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        btnAutoFresh.setOnClickListener(this);
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
                R.layout.test_layout_spinner_item);

        spinnerAdapter.setDropDownViewResource(R.layout.test_layout_spinner_item_dropdown);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(PrefUtils.getLevel(this).ordinal());
        spinner.setOnItemSelectedListener(this);
    }

    private void initRecyclerView() {
        logsAdapter = new LogAdapter(this);
        lvlogs.setAdapter(logsAdapter);
    }

    //初始化Search和Setting
    private void initEvent() {
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
            if (logsAdapter.isCanSelect()) {
                StringBuffer logsDetails = new StringBuffer();
                for (int i = 0; i < logsAdapter.getmLogList().size(); i++) {
                    if (logsAdapter.getmLogList().get(i).isSelected()) {
                        logsDetails.append(logsAdapter.getmLogList().get(i).getMessage() + "\n");
                        logsAdapter.getmLogList().get(i).setSelected(false);
                    }
                }
                CommonUtils.copyInfoToClipboard(LogsListActivity.this, logsDetails.toString());
                logsAdapter.setSelectState(false);
                btnSelect.setText(getResources().getString(R.string.select));
            } else {
                //进入选择模式
                logsAdapter.setSelectState(true);
                btnSelect.setText(getResources().getString(R.string.confirm));
            }
            logsAdapter.notifyDataSetChanged();
        }
        if (view == btnAutoFresh) {
            mAutoScroll = !mAutoScroll;
            btnAutoFresh.setText(mAutoScroll?getString(R.string.noauto_logs):getString(R.string.auto_logs));
        }
        if (view==btnClear){
            logsAdapter.clear();
            if (mLogcat != null) {
                mLogcat.clearLogs();
            }
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
        if (mLogHandler == null) {
            mLogHandler = new Handler(this);
        }
        if (mLogcat == null) {
            mLogcat = new Logcat(mLogHandler, PrefUtils.getLevel(this), PrefUtils.getFormat(this));
        }
        mLogcat.start();
        initEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLogHandler = null;
        if (mLogcat != null) {
            mLogcat.stop();
        }
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
        int currentSize = logsAdapter.getCount();
        logsAdapter.addAll(currentSize, logList);
        if (mAutoScroll) {
            lvlogs.smoothScrollToPosition(logsAdapter.getCount() - 1);
        }
    }

    private static class Handler extends android.os.Handler {

        private final WeakReference<LogsListActivity> mActivity;

        public Handler(LogsListActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LogsListActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {
                case Logcat.CAT_LOGS:
                    @SuppressWarnings("unchecked")
                    List<Log> catLogs = (List<Log>) msg.obj;
                    activity.updateLogs(catLogs);
                    break;
                case Logcat.CLEAR_LOGS:
                    if (!activity.lvlogs.canScrollVertically(-1)) {
                        return;
                    }
                    if (activity.logsAdapter.getCount() > MAX_LOG_ITEMS) {
                        activity.logsAdapter.removeFirstItems(activity.logsAdapter.getCount() - MAX_LOG_ITEMS);
                    }
                    break;
                case Logcat.REMOVE_LOGS:
                    activity.logsAdapter.clear();
                    break;
                default:
                    break;
            }
        }

    }

}
