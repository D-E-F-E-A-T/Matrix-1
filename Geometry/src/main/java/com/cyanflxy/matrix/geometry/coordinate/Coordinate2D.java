package com.cyanflxy.matrix.geometry.coordinate;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.cyanflxy.matrix.geometry.util.BaseUtils;

/**
 * 二维坐标系
 * <p/>
 * Created by XiaYuqiang on 2016/5/10.
 */
public class Coordinate2D extends View implements View.OnTouchListener {

    private final int INDICATOR_LENGTH;//指示器长度

    // 单位长度指示
    private float unit; // 当前一个单位值的大小
    private float unitLength;// 一个单位值的长度
    private final int UNIT_INDICATOR_LEFT;
    private final int UNIT_INDICATOR_BOTTOM;
    private static final int ARROW_ANGLE = 40;//箭头的角度

    private int width;
    private int height;

    private PointF original;

    private boolean drawDashGrid = true;// 是否绘制网格虚线

    private Paint coordinatePaint;
    private Paint gridLinePaint;
    private int textHeight;

    private GestureDetector gestureDetector;

    // Fling动画
    private Scroller scroller;
    protected ObjectAnimator animator;

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
        coordinatePaint.setColor(Color.RED);
        coordinatePaint.setAntiAlias(true);
        coordinatePaint.setTextSize(30);
        coordinatePaint.setStrokeWidth(1);

        Rect rect = new Rect();
        coordinatePaint.getTextBounds("1", 0, 1, rect);
        textHeight = rect.height();

        gridLinePaint = new Paint(coordinatePaint);
        gridLinePaint.setAlpha(128);
        gridLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        scroller = new Scroller(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        if (original == null) {
            original = new PointF(w / 2f, h / 2f);
        } else {
            original.set(w / 2f, h / 2f);
        }

    }

    /**
     * 是否显示网格虚线
     */
    public void setDashGridVisible(boolean show) {
        drawDashGrid = show;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        drawCoordinate(canvas);
    }

    private void drawCoordinate(Canvas canvas) {
        if (width == 0 || height == 0) {
            return;
        }

        drawOriginal(canvas);
        drawXAxis(canvas);
        drawYAxis(canvas);
        drawUnitSign(canvas);

    }

    private void drawOriginal(Canvas canvas) {

        float ox = original.x;
        float oy = original.y;
        float textSize = coordinatePaint.getTextSize();

        if (ox < textSize || ox > width || oy < 1 || oy > height - textSize) {
            return;
        }

        canvas.drawText("O", ox - textSize, oy + textSize, coordinatePaint);
    }

    // 绘制x轴
    private void drawXAxis(Canvas canvas) {

        float ox = original.x;
        float oy = original.y;

        float axisYLoc; // x轴高度
        float indicatorYLoc; // 指示符原理x轴的位置
        float stringYLoc;// 指示符文字标记位置

        float textSize = coordinatePaint.getTextSize();
        float decent = coordinatePaint.descent();

        if (oy < INDICATOR_LENGTH) {
            if (oy < 1) {
                axisYLoc = 1;
            } else {
                axisYLoc = oy;
            }

            indicatorYLoc = INDICATOR_LENGTH;
            stringYLoc = indicatorYLoc + textSize;
        } else if (oy > height - textSize) {
            if (oy > height) {
                axisYLoc = height;
            } else {
                axisYLoc = oy;
            }

            indicatorYLoc = axisYLoc;
            stringYLoc = axisYLoc - INDICATOR_LENGTH - decent;

        } else {
            axisYLoc = oy;
            indicatorYLoc = axisYLoc;
            stringYLoc = axisYLoc + textSize;
        }

        canvas.drawLine(0, axisYLoc, width, axisYLoc, coordinatePaint);
        canvas.drawText("X", width - textSize, (oy > height - textSize) ? stringYLoc : (axisYLoc + textSize), coordinatePaint);

        //箭头
        canvas.save();
        canvas.translate(width, axisYLoc);
        canvas.rotate(ARROW_ANGLE / 2);
        canvas.drawLine(-INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.rotate(-ARROW_ANGLE);
        canvas.drawLine(-INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.restore();

        // 指示标记
        int min = (int) (-ox / unitLength);
        int max = (int) ((width - ox) / unitLength);
        float left = ox + unitLength * min;

        for (int i = min; i <= max; i++) {
            canvas.drawLine(left, indicatorYLoc - INDICATOR_LENGTH, left, indicatorYLoc, coordinatePaint);

            if (i != 0) {
                String indicator = "" + i;
                float indicatorWidth = coordinatePaint.measureText(indicator);
                canvas.drawText(indicator, left - indicatorWidth / 2, stringYLoc, coordinatePaint);

                // 虚线格
                if (drawDashGrid) {
                    canvas.drawLine(left, 0, left, height, gridLinePaint);
                }
            }
            left += unitLength;
        }

    }

    // 绘制Y轴
    private void drawYAxis(Canvas canvas) {

        float ox = original.x;
        float oy = original.y;

        float axisXLoc; // y轴宽度
        float indicatorXLoc; // 指示符原理x轴的位置
        float stringXLoc;// 指示符文字标记位置

        int min = (int) ((oy - height) / unitLength);
        int max = (int) (oy / unitLength);

        int large = Math.max(Math.abs(min), Math.abs(max));
        float largeWidth = coordinatePaint.measureText("" + large);
        final int text_padding = 5;

        if (ox < largeWidth) {
            if (ox < 1) {
                axisXLoc = 1;
            } else {
                axisXLoc = ox;
            }
            indicatorXLoc = axisXLoc + INDICATOR_LENGTH;
            stringXLoc = indicatorXLoc + text_padding;//文字在其右边
        } else if (ox > width - INDICATOR_LENGTH) {
            if (ox > width) {
                axisXLoc = width;
            } else {
                axisXLoc = ox;
            }

            indicatorXLoc = width;
            stringXLoc = indicatorXLoc - INDICATOR_LENGTH - text_padding;//文字在其左边
            coordinatePaint.setTextAlign(Paint.Align.RIGHT);
        } else {
            axisXLoc = ox;
            indicatorXLoc = axisXLoc + INDICATOR_LENGTH;
            stringXLoc = axisXLoc - text_padding;//文字在其左边
            coordinatePaint.setTextAlign(Paint.Align.RIGHT);
        }

        float textSize = coordinatePaint.getTextSize();

        canvas.drawLine(axisXLoc, 0, axisXLoc, height, coordinatePaint);
        canvas.drawText("Y", (ox < largeWidth) ? (axisXLoc + text_padding) : (axisXLoc - text_padding), textSize, coordinatePaint);

        //箭头
        canvas.save();
        canvas.translate(axisXLoc, 0);
        canvas.rotate(-(90 - ARROW_ANGLE / 2));
        canvas.drawLine(-INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.rotate((90 - ARROW_ANGLE / 2) * 2);
        canvas.drawLine(INDICATOR_LENGTH, 0, 0, 0, coordinatePaint);
        canvas.restore();

        // 指示标记
        float top = oy - unitLength * max;

        for (int i = max; i >= min; i--) {
            canvas.drawLine(indicatorXLoc - INDICATOR_LENGTH, top, indicatorXLoc, top, coordinatePaint);

            if (i != 0) {
                String indicator = "" + i;

                canvas.drawText(indicator, stringXLoc, top + textHeight / 2, coordinatePaint);

                // 虚线格
                if (drawDashGrid) {
                    canvas.drawLine(0, top, width, top, gridLinePaint);
                }
            }

            top += unitLength;
        }

        coordinatePaint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawUnitSign(Canvas canvas) {
        float decent = coordinatePaint.descent();

        // 单位长度标记
        float unitIndicatorHeight = height - UNIT_INDICATOR_BOTTOM;
        canvas.drawLine(UNIT_INDICATOR_LEFT, unitIndicatorHeight, UNIT_INDICATOR_LEFT + unitLength, unitIndicatorHeight, coordinatePaint);
        canvas.drawLine(UNIT_INDICATOR_LEFT, unitIndicatorHeight, UNIT_INDICATOR_LEFT, unitIndicatorHeight - INDICATOR_LENGTH, coordinatePaint);
        canvas.drawLine(UNIT_INDICATOR_LEFT + unitLength, unitIndicatorHeight, UNIT_INDICATOR_LEFT + unitLength, unitIndicatorHeight - INDICATOR_LENGTH, coordinatePaint);

        String indicatorString = "" + unit;
        float indicatorWidth = coordinatePaint.measureText(indicatorString);
        canvas.drawText(indicatorString, UNIT_INDICATOR_LEFT + (unitLength - indicatorWidth) / 2, unitIndicatorHeight - decent, coordinatePaint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener
            = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            stopFling();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            original.offset(-distanceX, -distanceY);
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            scroller.fling((int) original.x, (int) original.y, (int) velocityX, (int) velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

            animator = ObjectAnimator.ofInt(Coordinate2D.this, "fling", 0, Integer.MAX_VALUE);
            animator.setDuration(10000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.start();

            return true;
        }
    };

    /**
     * {@link #simpleOnGestureListener} 属性动画反射使用
     */
    @SuppressWarnings("unused")
    private void setFling(int x) {
        if (scroller.computeScrollOffset()) {
            original.set(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        } else {
            stopFling();
        }
    }

    private void stopFling() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
    }
}
