package com.cyanflxy.matrix.mathematics.lines;

import android.graphics.PointF;

/**
 * 翻页几何数据。
 * <p/>
 * 输入：手势位置
 * <br>
 * 输出：翻页起点坐标与目标坐标
 * <p/>
 * 定义
 * <ul>
 * <li>翻页角：翻页时翻起来的角。</li>
 * <li>活动圆：翻页角的最大活动范围。</li>
 * </ul>
 * Created by XiaYuqiang on 2015/12/14.
 */
public class TurnerGeometry {
    // 活动圆的X轴坐标在宽度上的比例, 其y坐标依据翻页角坐标而定，0或者height
    private static final float ACTIVE_RANGE_CENTER = 0.25f;// 0.25 * width

    // 翻页范围宽度
    private static float width;
    // 翻页范围高度
    private static float height;

    private static float actCircleCenterX;
    private static float actCircleRadius;

    public static void init(float w, float h) {
        width = w;
        height = h;

        actCircleCenterX = ACTIVE_RANGE_CENTER * width;
        actCircleRadius = (1 - ACTIVE_RANGE_CENTER) * width;
    }

    private static float getCenterY(float startY, float currentY) {

        if (startY < currentY) {
            return 0;
        } else {
            return height;
        }
    }

    /**
     * 对目标点做修正。
     * <p/>
     * start点和current点的直线定义为line1。
     * line1与活动圆的切线定义line1的最大斜率，如果斜率过大，则重定义start点坐标
     */
    public static void correctStart(PointF start, PointF current) {
        if (start.y == current.y) {
            return;
        }

        float centerY = getCenterY(start.y, current.y);

        // 求current到活动圆的切线

        // 先求切线斜率的二次方程akk+bk+c=0
        // a = (x0 - xp + R)(x0 - xp - R)
        float a = (actCircleCenterX - current.x + actCircleRadius) * (actCircleCenterX - current.x - actCircleRadius);
        // b = -2(x0 - xp)(y0 - yp)
        float b = -2 * (actCircleCenterX - current.x) * (centerY - current.y);
        // c = (y0 - yp + R)(y0 - yp - R)
        float c = (centerY - current.y + actCircleRadius) * (centerY - current.y - actCircleRadius);

        float k1;
        float k2;

        if (Float.compare(a, 0) != 0) {
            float bbm4ac = (float) Math.sqrt(b * b - 4 * a * c);
            if (Float.isNaN(bbm4ac)) {
                // 点在目标圆内部则不处理
                return;
            }

            // 两条切线的斜率
            k1 = (-b + bbm4ac) / a / 2;
            k2 = (-b - bbm4ac) / a / 2;
        } else if (Float.compare(b, 0) != 0) {
            // 当一条切线是垂直的时候，a==0，此时k2=NaN,此时不考虑这条垂直的切线
            k1 = k2 = -c / b;
        } else {
            // 按照公式，如果a==0,b==0, 那么该触摸点应该在角上，而该点正好在圆上，所以不处理
            return;
        }

        if (centerY == height) {
            k = Math.min(k1, k2);

            float angle = (float) (Math.PI / 2 - (Math.PI / 2 + Math.atan(k)) / 2);
            float deltaY = (float) ((width - current.x) / Math.tan(angle));
            float target = current.y + deltaY;
            if (start.y > target) {
                start.y = target;
            }
        } else {
            k = Math.max(k1, k2);

            float angle = (float) (Math.PI / 2 - (Math.PI / 2 - Math.atan(k)) / 2);
            float deltaY = (float) ((width - current.x) / Math.tan(angle));
            float target = current.y - deltaY;
            if (start.y < target) {
                start.y = target;
            }
        }

    }

    public static float k;
}
