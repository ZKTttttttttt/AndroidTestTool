package com.zkt.testtool.logcat;



import com.zkt.testtool.R;

import java.util.regex.Pattern;

public enum Format{
	
	BRIEF("brief", Pattern.compile("^([VDIWEF])/"), R.string.brief_title);

	private String mTitle;
	private Pattern mPattern;
	private int mTitleId;
	
	Format(String title, Pattern pattern, int titleId){
		this.mTitle = title;
		this.mPattern = pattern;
		//== null ? Pattern.compile(pattern) : null;
		this.mTitleId = titleId;
	}
	
	public String getTitle(){
		return mTitle;
	}
	
	public Pattern getPattern(){
		return mPattern;
	}
	
	public int getTitleId(){
		return mTitleId;
	}
}
