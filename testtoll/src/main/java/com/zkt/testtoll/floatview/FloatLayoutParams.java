package com.zkt.testtoll.floatview;

import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

public class FloatLayoutParams extends WindowManager.LayoutParams {
    private int[] screenSizes;

    /**
     * Special value for x position that represents the right of the screen.
     */
    public static final int RIGHT = Integer.MAX_VALUE;
    /**
     * Special value for y position that represents the bottom of the
     * screen.
     */
    public static final int BOTTOM = Integer.MAX_VALUE;
    /**
     * Special value for x or y position that represents the center of the
     * screen.
     */
    public static final int CENTER = Integer.MIN_VALUE;
    /**
     * Special value for x or y position which requests that the system
     * determine the position.
     */
    public static final int AUTO_POSITION = Integer.MIN_VALUE + 1;

    //判断是滑动还是点击
    public int threshold;

    /**
     * Optional constraints of the window.
     */
    public int minWidth, minHeight, maxWidth, maxHeight;


    public FloatLayoutParams(int[] screenSizes) {
        super(200, 200, TYPE_PHONE, FloatLayoutParams.FLAG_NOT_TOUCH_MODAL | FloatLayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);

        flags = flags | FloatLayoutParams.FLAG_NOT_FOCUSABLE;
        this.screenSizes = screenSizes;
        x = getX(width);
        y = getY(height);

        gravity = Gravity.TOP | Gravity.LEFT;

        threshold = 10;
        minWidth = minHeight = 0;
        maxWidth = maxHeight = Integer.MAX_VALUE;
    }

    /**
     * @param w  The width of the window.
     * @param h  The height of the window.
     */
    public FloatLayoutParams(int[] screenSizes, int w, int h) {
        this(screenSizes);
        width = w;
        height = h;
    }

    /**
     * @param w    The width of the window.
     * @param h    The height of the window.
     * @param xpos The x position of the window.
     * @param ypos The y position of the window.
     */
    public FloatLayoutParams(int[] screenSizes, int w, int h, int xpos, int ypos) {
        this(screenSizes, w, h);

        if (xpos != AUTO_POSITION) {
            x = xpos;
        }
        if (ypos != AUTO_POSITION) {
            y = ypos;
        }

        int width = screenSizes[0];
        int height = screenSizes[1];

        if (x == RIGHT) {
            x = width - w;
        } else if (x == CENTER) {
            x = (width - w) / 2;
        }

        if (y == BOTTOM) {
            y = height - h;
        } else if (y == CENTER) {
            y = (height - h) / 2;
        }
    }

    /**
     * @param w         The width of the window.
     * @param h         The height of the window.
     * @param xpos      The x position of the window.
     * @param ypos      The y position of the window.
     * @param minWidth  The minimum width of the window.
     * @param minHeight The mininum height of the window.
     */
    public FloatLayoutParams(int []screenSizes, int w, int h, int xpos, int ypos,
                             int minWidth, int minHeight) {
        this(screenSizes, w, h, xpos, ypos);

        this.minWidth = minWidth;
        this.minHeight = minHeight;
    }


    // helper to create cascading windows
    private int getX(int width) {
        int displayWidth = screenSizes[0];


        int initialX = 100;
        int variableX = 100;
        int rawX = initialX + variableX;

        return rawX % (displayWidth - width);
    }

    // helper to create cascading windows
    private int getY(int height) {
        int displayWidth = screenSizes[0];
        int displayHeight = screenSizes[1];


        int initialY = 100;
        int variableY = x + 200 * 100 / (displayWidth - width);

        int rawY = initialY + variableY;

        return rawY % (displayHeight - height);
    }

}
