package com.zkt.testtool.logcat;

/**
 * 描述：日志实体
 */
public class Log {

    private String mMessage;
    private Level mLevel;
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Log(String line) {
        this.mMessage = line;
    }

    public String getMessage() {
        return mMessage;
    }

    public Level getLevel() {
        return mLevel;
    }

    public void setLevel(Level level) {
        this.mLevel = level;
    }

}
