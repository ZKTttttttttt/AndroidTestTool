package com.zkt.testtoll.logcat;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.zkt.testtoll.R;
import com.zkt.testtoll.util.PrefUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：日志adapter
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

    private final List<Log> mLogList;

    public LogAdapter() {
        this.mLogList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.layout_log_item, null, false);
        ViewHolder vh = new ViewHolder(v);
        vh.mTextView.setTextSize(PrefUtils.getTextSize(parent.getContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            vh.mTextView.setTypeface(PrefUtils.getTextFont(parent.getContext()));
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextView.setText(getItem(position).getMessage());
		holder.mTextView.setTextColor(getItem(position).getLevel().getColor());
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
		for (int i=0; i<count; i++) mLogList.remove(0);
		notifyItemRangeRemoved(0, count);
	}
	
	public void clear() {
		mLogList.clear();
		notifyDataSetChanged();
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.listitem_log_textview);
        }
    }
}
