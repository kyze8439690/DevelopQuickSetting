package me.yugy.github.developquicksetting;

import android.os.Build;

public class Property {

    public static final String DEBUG_LAYOUT_PROPERTY = "debug.layout";
    private static final String DEBUG_OVERDRAW_PROPERTY_KITKAT = "debug.hwui.overdraw";
    private static final String DEBUG_OVERDRAW_PROPERTY_JB_MR1 = "debug.hwui.show_overdraw";
    public static final String PROFILE_PROPERTY = "debug.hwui.profile";

    public static String getDebugOverdrawPropertyKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return DEBUG_OVERDRAW_PROPERTY_KITKAT;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return DEBUG_OVERDRAW_PROPERTY_JB_MR1;
        } else {
            return "";
        }
    }

    public static String getDebugOverdrawPropertyEnabledValue() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return "show";
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return "true";
        } else {
            return "";
        }
    }

}
