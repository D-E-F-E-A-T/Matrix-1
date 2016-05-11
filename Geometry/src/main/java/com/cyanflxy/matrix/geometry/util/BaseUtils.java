package com.cyanflxy.matrix.geometry.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * 基本工具方法
 * <p/>
 * Created by XiaYuqiang on 2016/1/29.
 */
public class BaseUtils {

    public static int dp2px(Context context, float value) {
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
        return Math.round(v + 0.5f);
    }

}
