package com.cyanflxy.matrix.mathematics.line;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.cyanflxy.matrix.mathematics.Function;

/**
 * 支线
 * <p/>
 * Created by CyanFlxy on 2015/12/15.
 */
public class Line extends Function {

    public static Line createByStandard(float a, float b, float c) {
        return new Line(a, b, c);
    }

    // 使用直线的一般表达式：ax+by+c=0;
    private float a;
    private float b;
    private float c;

    private Line(float a, float b, float c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * 获取x值对应的y值，如果直线垂直，返回Float.NaN
     */
    public float getY(float x) {
        if (isVertical()) {
            return Float.NaN;
        }
        return (-c - a * x) / b;
    }

    /**
     * 获取y值对应x值，如果直线水平，返回Float.NaN
     */
    public float getX(float y) {
        if (isHorizontal()) {
            return Float.NaN;
        }
        return (-c - b * y) / a;
    }

    /**
     * 直线是否水平
     */
    public boolean isHorizontal() {
        return Float.compare(a, 0) == 0;
    }

    /**
     * 直线是否垂直
     */
    public boolean isVertical() {
        return Float.compare(b, 0) == 0;
    }

    /**
     * 获取直线斜率
     */
    public double getSlopeDegree() {
        if (isVertical()) {
            return Math.PI / 2;
        } else {
            return Math.atan(-a / b);
        }
    }

    @Override
    public void onDraw(Canvas canvas, RectF scope, float unitLength, float grid, Paint paint) {
        if (isVertical()) {
            float x = getX(0);
            if (x >= scope.left && x <= scope.right) {
                drawLine(canvas, x, scope.top, x, scope.bottom, unitLength, paint);
            }
        } else if (isHorizontal()) {
            float y = getY(0);
            if (y >= scope.top && y <= scope.bottom) {
                drawLine(canvas, scope.left, y, scope.right, y, unitLength, paint);
            }
        } else {
            float y1 = scope.top;
            float x1 = getX(y1);
            if (x1 < scope.left) {
                x1 = scope.left;
                y1 = getY(x1);
            } else if (x1 > scope.right) {
                x1 = scope.right;
                y1 = getY(x1);
            }

            float y2 = scope.bottom;
            float x2 = getX(y2);
            if (x2 > scope.right) {
                x2 = scope.right;
                y2 = getY(x2);
            } else if (x2 < scope.left) {
                x2 = scope.left;
                y2 = getY(x2);
            }

            drawLine(canvas, x1, y1, x2, y2, unitLength, paint);
        }
    }


}
