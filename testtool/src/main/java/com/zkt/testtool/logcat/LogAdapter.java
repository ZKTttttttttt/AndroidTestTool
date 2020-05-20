package com.zkt.testtool.logcat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.zkt.testtool.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：日志adapter
 */
public class LogAdapter extends BaseAdapter {
    private final List<Log> mLogList;
    private Context context;
    public boolean isCanSelect() {
        return canSelect;
    }

    private boolean canSelect;

    public LogAdapter(Context context) {
        this.mLogList = new ArrayList<>();
        this.context=context;
    }

    public void setSelectState(boolean i) {
        canSelect = i;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mLogList.size();
    }

    @Override
    public Log getItem(int position) {
        return mLogList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        final ViewHolder holder;
        if (convertView == null){
            // inflate出子项布局，实例化其中的图片控件和文本控件
            view = LayoutInflater.from(context).inflate(R.layout.test_layout_log_item, null);

            holder = new ViewHolder(view);
            view.setTag(holder);
        }else{
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.mTextView.setText(getItem(position).getMessage());
        holder.mTextView.setTextColor(getItem(position).getLevel().getColor());
        holder.tvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.tvSelect.setSelected(!holder.tvSelect.isSelected());
                getItem(position).setSelected(holder.tvSelect.isSelected());
            }
        });
        holder.tvSelect.setVisibility(canSelect ? View.VISIBLE : View.GONE);
        if (!canSelect) {
            holder.tvSelect.setSelected(false);
        }

        return view;
    }

    public void addAll(int previousCount, List<Log> logList) {
        mLogList.addAll(logList);
        notifyDataSetChanged();
    }

    public void removeFirstItems(int count) {
        for (int i = 0; i < count; i++) {
            mLogList.remove(0);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mLogList.clear();
        notifyDataSetChanged();
    }

    public List<Log> getmLogList() {
        return mLogList;
    }

    public static class ViewHolder {
        public TextView mTextView;
        public ImageView tvSelect;

        public ViewHolder(View v) {
            mTextView = (TextView) v.findViewById(R.id.lv_log_tv);
            tvSelect = (ImageView) v.findViewById(R.id.btn_select);
        }
    }
}
