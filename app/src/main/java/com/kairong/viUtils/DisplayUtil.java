package com.kairong.viUtils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

/**
 * Created by Kairong on 2015/6/3.
 */
public final class DisplayUtil {
    /**
     * 屏幕信息：像素密度，分辨率，状态栏高度
     */
    public static float scale = 0;
    public static float scaledDensity = 0;
    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static int stateBarHeight = 0;
    public static int activity_horizontal_margin = 0;
    public static int activity_vertical_margin = 0;
    public static int main_2_btn_size = 0;
    public static int main_1_btn_size = 0;
    public static final int CHINESE = 20152016;
    public static final int NUMBER_OR_CHARACTER = 20152014;

    /**
     * dp转成px
     * @param dipValue
     * @return
     */
    public static int dip2px(float dipValue) {
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转成dp
     * @param pxValue
     * @return
     */
    public static int px2dip(float pxValue) {
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转成px
     * @param spValue
     * @param type
     * @return
     */
    public static float sp2px(float spValue, int type) {
        switch (type) {
            case CHINESE:
                return spValue * scaledDensity;
            case NUMBER_OR_CHARACTER:
                return spValue * scaledDensity * 10.0f / 18.0f;
            default:
                return spValue * scaledDensity;
        }
    }
    /**
     * 返回当前屏幕是否为竖屏。
     * @param context
     * @return 当且仅当当前屏幕为竖屏时返回true,否则返回false。
     */
    public static boolean isScreenOriatationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
}
