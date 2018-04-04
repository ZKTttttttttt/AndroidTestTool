package com.zkt.testtoll.logcat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.zkt.testtoll.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：日志adapter
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {


    private final List<Log> mLogList;

    public boolean isCanSelect() {
        return canSelect;
    }

    private boolean canSelect;

    public LogAdapter() {
        this.mLogList = new ArrayList<>();
    }

    public void setSelectState(boolean i) {
        canSelect = i;
        notifyDataSetChanged();
    }

    @Override
    public LogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_log_item, null, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
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
    }

    @Override
    public int getItemCount() {
        return mLogList.size();
    }

    public Log getItem(int position) {
        return mLogList.get(position);
    }

    public void addAll(int previousCount, List<Log> logList) {
        mLogList.addAll(logList);
        notifyItemRangeInserted(previousCount + 1, logList.size());
    }

    public void removeFirstItems(int count) {
        for (int i = 0; i < count; i++) mLogList.remove(0);
        notifyItemRangeRemoved(0, count);
    }

    public void clear() {
        mLogList.clear();
        notifyDataSetChanged();
    }

    public List<Log> getmLogList() {
        return mLogList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView tvSelect;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.lv_log_tv);
            tvSelect = (ImageView) v.findViewById(R.id.btn_select);
        }
    }
}
