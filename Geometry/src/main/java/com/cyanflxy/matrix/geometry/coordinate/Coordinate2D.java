package com.cyanflxy.matrix.geometry.coordinate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.cyanflxy.matrix.geometry.util.BaseUtils;

/**
 * 二维坐标系
 * <p/>
 * Created by XiaYuqiang on 2016/5/10.
 */
public class Coordinate2D extends View {

    private final int INDICATOR_LENGTH;//指示器长度

    // 单位长度指示
    private float unit; // 当前一个单位值的大小
    private float unitLength;// 一个单位值的长度
    private final int UNIT_INDICATOR_LEFT;
    private final int UNIT_INDICATOR_BOTTOM;

    private Paint coordinatePaint;
    private Paint gridLinePaint;

    public Coordinate2D(Context context) {
        this(context, null);
    }

    public Coordinate2D(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Coordinate2D(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 网格虚线不能使用硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        INDICATOR_LENGTH = BaseUtils.dp2px(context, 4);
        unit = 1;
        unitLength = BaseUtils.dp2px(context, 35);
        UNIT_INDICATOR_LEFT = BaseUtils.dp2px(context, 15);
        UNIT_INDICATOR_BOTTOM = BaseUtils.dp2px(context, 15);

        coordinatePaint = new Paint();
        coordinatePaint.setColor(Color.BLACK);
        coordinatePaint.setAntiAlias(true);
        coordinatePaint.setTextSize(30);

        gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.BLACK);
        gridLinePaint.setAlpha(128);
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setStrokeWidth(1);
        PathEffect effects = new DashPathEffect(new float[]{5, 5}, 0);
        gridLinePaint.setPathEffect(effects);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        drawCoordinate(canvas);
    }

    private void drawCoordinate(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        canvas.drawLine(0, halfHeight, width, halfHeight, coordinatePaint);// x轴
        canvas.drawLine(halfWidth, 0, halfWidth, height, coordinatePaint); // y轴

        float textSize = coordinatePaint.getTextSize();

        // 原点与坐标轴标记
        canvas.drawText("O", halfWidth - textSize, halfHeight + textSize, coordinatePaint);
        canvas.drawText("X", width - textSize, halfHeight + textSize, coordinatePaint);
        canvas.drawText("Y", halfWidth - textSize, textSize, coordinatePaint);


        final int arrowAngle = 40;
        //x轴箭头
        canvas.save();
        canvas.translate(width, halfHeight);
        canvas.rotate(arrowAngle / 2);
        canvas.drawLine(-INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.rotate(-arrowAngle);
        canvas.drawLine(-INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.restore();

        //y轴箭头
        canvas.save();
        canvas.translate(halfWidth, 0);
        canvas.rotate(-(90 - arrowAngle / 2));
        canvas.drawLine(-INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.rotate((90 - arrowAngle / 2) * 2);
        canvas.drawLine(INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.restore();

        // x轴指示标记
        int xMax = (int) ((halfWidth - unitLength / 2) / unitLength);
        float xLeft = halfWidth - unitLength * xMax;

        for (int i = -xMax; i <= xMax; i++) {
            canvas.drawLine(xLeft, halfHeight, xLeft, halfHeight - INDICATOR_LENGTH, coordinatePaint);

            if (i != 0) {
                String indicator = "" + i;
                float indicatorWidth = coordinatePaint.measureText(indicator);
                canvas.drawText(indicator, xLeft - indicatorWidth / 2, halfHeight + textSize, coordinatePaint);

                // 虚线格
                canvas.drawLine(xLeft, 0, xLeft, height, gridLinePaint);
            }
            xLeft += unitLength;
        }

        // y轴指示标记
        int yMax = (int) ((halfHeight - unitLength / 2) / unitLength);
        float yTop = halfHeight - unitLength * yMax;
        float decent = coordinatePaint.descent();

        for (int i = yMax; i >= -yMax; i--) {
            canvas.drawLine(halfWidth, yTop, halfWidth + INDICATOR_LENGTH, yTop, coordinatePaint);

            if (i != 0) {
                String indicator = "" + i;
                canvas.drawText(indicator, halfWidth - textSize, yTop + (textSize - decent) / 2, coordinatePaint);

                // 虚线格
                canvas.drawLine(0, yTop, width, yTop, gridLinePaint);
            }
            yTop += unitLength;
        }

        // 单位长度标记
        float unitIndicatorHeight = height - UNIT_INDICATOR_BOTTOM;
        canvas.drawLine(UNIT_INDICATOR_LEFT, unitIndicatorHeight, UNIT_INDICATOR_LEFT + unitLength, unitIndicatorHeight, coordinatePaint);
        canvas.drawLine(UNIT_INDICATOR_LEFT, unitIndicatorHeight, UNIT_INDICATOR_LEFT, unitIndicatorHeight - INDICATOR_LENGTH, coordinatePaint);
        canvas.drawLine(UNIT_INDICATOR_LEFT + unitLength, unitIndicatorHeight, UNIT_INDICATOR_LEFT + unitLength, unitIndicatorHeight - INDICATOR_LENGTH, coordinatePaint);

        String indicatorString = "" + unit;
        float indicatorWidth = coordinatePaint.measureText(indicatorString);
        canvas.drawText(indicatorString, UNIT_INDICATOR_LEFT + (unitLength - indicatorWidth) / 2, unitIndicatorHeight - decent, coordinatePaint);

    }

}
