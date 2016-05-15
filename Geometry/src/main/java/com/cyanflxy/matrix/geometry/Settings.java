package com.cyanflxy.matrix.geometry;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 首选项
 * Created by XiaYuqiang on 2016/5/15.
 */
public class Settings {

    private static SharedPreferences pref;

    public static void init(Context c) {
        pref = PreferenceManager.getDefaultSharedPreferences(c);
    }

    // 是否显示网格虚线
    private static final String SHOW_DASH_GRID = "isShowDashGrid";

    public static boolean isShowDashGrid() {
        return pref.getBoolean(SHOW_DASH_GRID, true);
    }

    public static void setShowDashGrid(boolean show) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(SHOW_DASH_GRID, show);
        editor.apply();
    }

    private static final String COORDINATE_LOCK = "coordinateLock";

    public static boolean isCoordinateLock() {
        return pref.getBoolean(COORDINATE_LOCK, false);
    }

    public static void setCoordinateLock(boolean lock) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(COORDINATE_LOCK, lock);
        editor.apply();
    }

}
