package com.zkt.testtoll.logcat;

public enum Buffer{
	
	MAIN("main"),
	SYSTEM("system"),
	RADIO("radio"),
	EVENTS("events");
	
	private final String mTitle;
	
	Buffer(String title){
		this.mTitle = title;
	}
	
	public String getTitle(){
		return mTitle;
	}
}
