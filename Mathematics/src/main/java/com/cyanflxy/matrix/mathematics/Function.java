package com.cyanflxy.matrix.mathematics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * 函数
 * Created by XiaYuqiang on 2016/5/15.
 */
public abstract class Function {
    /**
     * 在Canvas上绘制该函数
     *
     * @param canvas     画布
     * @param scope      绘图区间
     * @param unitLength 单位1长度
     * @param grid       取点最小间隔
     * @param paint      画笔
     */
    public abstract void onDraw(Canvas canvas, RectF scope, float unitLength, float grid, Paint paint);

    protected void drawLine(Canvas canvas, float x1, float y1, float x2, float y2, float unitLength, Paint paint) {
        canvas.drawLine(x1*unitLength, y1*unitLength, x2*unitLength, y2*unitLength, paint);
    }
}
