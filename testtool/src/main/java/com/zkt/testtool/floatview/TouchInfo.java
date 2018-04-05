package com.zkt.testtool.floatview;

import java.util.Locale;

public class TouchInfo {

    public int firstX, firstY, lastX, lastY;

    public float ratio;

    public boolean moving;

    @Override
    public String toString() {
        return String.format(Locale.US,
                "WindowTouchInfo { firstX=%d, firstY=%d,lastX=%d, lastY=%d }",
                firstX, firstY, lastX, lastY);
    }
}
