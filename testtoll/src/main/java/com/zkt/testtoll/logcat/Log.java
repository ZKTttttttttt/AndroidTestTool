package com.zkt.testtoll.logcat;

/**
 * 描述：日志实体
 */
public class Log{
	
	private String mMessage;
	private Level mLevel;
	
	public Log(String line){
		this.mMessage = line;
	}
	
	public String getMessage(){
		return mMessage;
	}
	
	public Level getLevel(){
		return mLevel;
	}
	
	public void setLevel(Level level){
		this.mLevel = level;
	}
	
}
