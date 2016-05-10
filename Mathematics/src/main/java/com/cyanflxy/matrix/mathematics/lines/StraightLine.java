package com.cyanflxy.matrix.mathematics.lines;

import android.graphics.PointF;

/**
 * 直线
 *
 * Created by CyanFlxy on 2015/12/15.
 */
public class StraightLine extends Line {

    /**
     * 创建a0,b0两点的中线
     */
    public static Line createMiddleLine(PointF a0, PointF b0) {
        float a = 2 * (b0.x - a0.x);
        float b = 2 * (b0.y - a0.y);
        float c = a0.x * a0.x + a0.y * a0.y - b0.y * b0.y - b0.x * b0.x;
        return new StraightLine(a, b, c);
    }

    /**
     * 创建过两点p1,p2的直线
     */
    public static Line createLine(PointF p1, PointF p2) {
        return createLine(p1.x, p1.y, p2.x, p2.y);
    }

    public static Line createLine(float x1, float y1, float x2, float y2) {
        float a = y1 - y2;
        float b = x2 - x1;
        float c = x1 * (y2 - y1) - y1 * (x2 - x1);

        return new StraightLine(a, b, c);
    }

    /**
     * 创建经过o点垂直于oe的直线
     */
    public static Line createVerticalLine(PointF o, PointF e) {
        float a = 2 * (o.x - e.x);
        float b = 2 * (o.y - e.y);
        float c = -2 * (o.x * o.x + o.y * o.y - o.x * e.x - o.y * e.y);
        return new StraightLine(a, b, c);
    }

    /**
     * 创建垂直于X轴的直线
     *
     * @param x 垂线x坐标
     */
    public static Line createVerticalLine(float x) {
        return new StraightLine(1, 0, -x);
    }

    // 使用直线的一般表达式：ax+by+c=0;
    private double a;
    private double b;
    private double c;

    private StraightLine(float a, float b, float c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public double getY(float x) {
        if (isVertical()) {
            return Float.NaN;
        }
        return (-c - a * x) / b;
    }

    public double getX(float y) {
        if (isHorizontal()) {
            return Float.NaN;
        }
        return (-c - b * y) / a;
    }

    public boolean isHorizontal() {
        return Double.compare(a, 0) == 0;
    }

    public boolean isVertical() {
        return Double.compare(b, 0) == 0;
    }

    // 斜率
    public double getSlopeDegree() {
        if (isVertical()) {
            return Math.PI / 2;
        } else {
            return Math.atan(-a / b);
        }
    }
}
