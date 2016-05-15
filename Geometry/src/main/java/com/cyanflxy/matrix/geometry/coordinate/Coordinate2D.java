package com.cyanflxy.matrix.geometry.coordinate;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.cyanflxy.matrix.geometry.util.BaseUtils;
import com.cyanflxy.matrix.mathematics.Function;

import java.util.LinkedList;
import java.util.List;

/**
 * 二维坐标系
 * <p/>
 * Created by XiaYuqiang on 2016/5/10.
 */
public class Coordinate2D extends View implements View.OnTouchListener {

    private static final int MAX_ANIMATION_TIME = 600;// 动画最大用时，ms
    private static final int MIN_RESTORE_ORIGINAL_SPEED = 10;// 还原原点最小速度，px/ms
    private static final float SCALE_ANIMATION_SPEED = 0.2f;// 执行缩放动画的速度,times/ms
    private static final float MIN_SCALE_ANIMATION_TIMES = 1.4f;// 执行缩放动画的最小倍数

    private static final int MAX_UNIT_SCALE = 5;// 缩放最大倍数
    private static final int ARROW_ANGLE = 40;//坐标轴箭头的角度
    private final int STANDARD_UNIT_LENGTH;//标准单位长度定义
    private final int INDICATOR_LENGTH;//指示标记长度

    private final int UNIT_INDICATOR_LEFT; // 单位长度指示器左边距
    private final int UNIT_INDICATOR_BOTTOM;//单位长度指示器底边距

    // View属性
    private int width;
    private int height;

    // 坐标系绘图属性
    private int unitBase;//单位长度遵循 1,2,5,10 的规律增减，该值记录当前进度
    private int unitScale;// 单位长度的指数
    private float unitLength;// 一个单位值的长度
    private PointF original;//原点位置
    private boolean drawDashGrid = true;// 是否绘制网格虚线
    private boolean isCoordinateLock = false;//禁止触摸改变坐标系

    private OnScaleStateChangeListener onScaleStateChangeListener;

    // 手势
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private float lastPointDistance;// 缩放时，上次两指间距离
    private ObjectAnimator animator;// 坐标轴移动动画
    private float lastUniteLength;// 缩放动画中，上次的长度

    // 坐标系绘图
    private Paint coordinatePaint;
    private Paint gridLinePaint;
    private Paint functionPaint;
    private final int textHeight;
    private RectF scopeRect = new RectF();

    private List<Function> functionList;

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

        STANDARD_UNIT_LENGTH = BaseUtils.dp2px(context, 30);
        INDICATOR_LENGTH = BaseUtils.dp2px(context, 4);
        UNIT_INDICATOR_LEFT = BaseUtils.dp2px(context, 20);
        UNIT_INDICATOR_BOTTOM = BaseUtils.dp2px(context, 5);

        unitBase = 1;
        unitScale = 0;
        unitLength = STANDARD_UNIT_LENGTH;

        coordinatePaint = new Paint();
        coordinatePaint.setColor(Color.rgb(5, 39, 175));//科技蓝色
        coordinatePaint.setAntiAlias(true);
        coordinatePaint.setTextSize(30);
        coordinatePaint.setStrokeWidth(1);

        Rect rect = new Rect();
        coordinatePaint.getTextBounds("1", 0, 1, rect);
        textHeight = rect.height();

        gridLinePaint = new Paint(coordinatePaint);
        gridLinePaint.setAlpha(128);
        gridLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        functionPaint = new Paint(coordinatePaint);

        setOnTouchListener(this);
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        scroller = new Scroller(context);

        functionList = new LinkedList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        if (original == null) {
            original = new PointF(w / 2f, h / 2f);
        } else {
            // original数据来自 setCoordinateState
            original.set(width / 2 - original.x, height / 2 - original.y);
        }

    }

    public void setOnScaleStateChangeListener(OnScaleStateChangeListener l) {
        onScaleStateChangeListener = l;
    }

    /**
     * 锁定坐标系的触摸调整
     */
    public void setCoordinateLock(boolean lock) {
        isCoordinateLock = lock;
    }

    /**
     * 是否显示网格虚线
     */
    public void setDashGridVisible(boolean show) {
        drawDashGrid = show;
        invalidate();
    }

    /**
     * 能否放大
     */
    public boolean canScaleLarge() {
        return unitScale > -MAX_UNIT_SCALE;
    }

    /**
     * 能否缩小
     */
    public boolean canScaleSmall() {
        return unitScale < MAX_UNIT_SCALE;
    }

    /**
     * 进行下一级放大
     */
    public void setNextScaleLarge() {
        stopAnimator();
        if (unitBase == 5) {
            startScaleAnimation(STANDARD_UNIT_LENGTH * 2.5f / unitLength);
        } else {
            startScaleAnimation(STANDARD_UNIT_LENGTH * 2 / unitLength);
        }
    }

    /**
     * 进行下一级缩小
     */
    public void setNextScaleSmall() {
        stopAnimator();
        if (unitBase == 2) {
            startScaleAnimation(STANDARD_UNIT_LENGTH * 0.4f / unitLength);
        } else {
            startScaleAnimation(STANDARD_UNIT_LENGTH * 0.5f / unitLength);
        }
    }

    /**
     * 还原坐标缩放级别
     */
    public void restoreScale() {
        startScaleAnimation(STANDARD_UNIT_LENGTH / (float) (unitLength / unitBase / Math.pow(10, unitScale)));
    }

    /**
     * 以屏幕中心为基点，将坐标系缩放一定倍数
     *
     * @param scale 缩放倍数，
     */
    private void startScaleAnimation(float scale) {
        stopAnimator();

        float times = scale < 1 ? 1 / scale : scale;

        if (times < MIN_SCALE_ANIMATION_TIMES) {
            setScale(scale, width / 2f, height / 2f);
        } else {
            int time = MAX_ANIMATION_TIME;
            if (times < SCALE_ANIMATION_SPEED * MAX_ANIMATION_TIME) {
                time = (int) (times / SCALE_ANIMATION_SPEED);
            }

            lastUniteLength = unitLength;
            float target = unitLength * scale;

            animator = ObjectAnimator.ofFloat(this, "unitLength", unitLength, target);
            animator.setDuration(time);
            animator.start();

        }
    }

    /**
     * 属性动画，{@link #startScaleAnimation}
     */
    @SuppressWarnings("unused")
    private void setUnitLength(float length) {
        setScale(length / lastUniteLength, width / 2f, height / 2f);
        lastUniteLength = length;
    }

    /**
     * 还原原点坐标
     */
    public void restoreOriginal() {
        stopAnimator();

        float a = original.x - width / 2;
        float b = original.y - height / 2;
        float d = (float) Math.hypot(a, b);

        if (d < unitLength) {
            original.set(width / 2, height / 2);
            invalidate();
        } else {
            int time = MAX_ANIMATION_TIME;
            if (d < MAX_ANIMATION_TIME * MIN_RESTORE_ORIGINAL_SPEED) {
                time = (int) (d / MIN_RESTORE_ORIGINAL_SPEED);
            }

            final PointF start = new PointF(original.x, original.y);
            PointF end = new PointF(width / 2, height / 2);

            animator = ObjectAnimator.ofObject(this, "originalPoint", new TypeEvaluator<PointF>() {
                @Override
                public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
                    float x = startValue.x + (endValue.x - startValue.x) * fraction;
                    float y = startValue.y + (endValue.y - startValue.y) * fraction;
                    return new PointF(x, y);
                }
            }, start, end);

            animator.setDuration(time);
            animator.start();
        }

    }

    /**
     * 属性动画，{@link #restoreOriginal()}
     * 针对对象的属性方法，如果不是public的，不会被调用，这是个bug？
     */
    @SuppressWarnings("unused")
    public void setOriginalPoint(PointF p) {
        original.set(p.x, p.y);
        invalidate();
    }

    public void addFunction(Function f) {
        functionList.add(f);
    }

    public void removeFunction(Function f) {
        functionList.remove(f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        drawCoordinate(canvas);

        drawFunctions(canvas);
    }

    private void drawFunctions(Canvas canvas) {
        float ox = original.x;
        float oy = original.y;
        // 单位1的长度
        float unitLength = (float) (this.unitLength / (unitBase * Math.pow(10, unitScale)));

        float left = -ox / unitLength;
        float right = (width - ox) / unitLength;
        float top = oy / unitLength;
        float bottom = (oy - height) / unitLength;

        scopeRect.set(left, top, right, bottom);

        canvas.save();
        canvas.translate(original.x, original.y);
        canvas.scale(1, -1);

        for (Function f : functionList) {
            f.onDraw(canvas, scopeRect, unitLength, scopeRect.width() / width, functionPaint);
        }

        canvas.restore();
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

    // 绘制原点
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
                String indicator = getUnitString(i);
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
        float largeWidth = coordinatePaint.measureText("-" + large);
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
                String indicator = getUnitString(i);

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

        String indicatorString = getUnitString(1);
        float indicatorWidth = coordinatePaint.measureText(indicatorString);
        canvas.drawText(indicatorString, UNIT_INDICATOR_LEFT + (unitLength - indicatorWidth) / 2, unitIndicatorHeight - decent, coordinatePaint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isCoordinateLock) {
            return true;
        }

        int count = event.getPointerCount();

        if (count == 2) {
            processScaleEvent(event);
        } else if (count == 1) {
            gestureDetector.onTouchEvent(event);
        }

        return true;
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener
            = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            stopAnimator();
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
            stopAnimator();

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
            stopAnimator();
        }
    }

    private void stopAnimator() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
    }

    /**
     * 计算缩放手势中的缩放倍数
     */
    private void processScaleEvent(MotionEvent event) {

        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);
        float dx = x1 - x0;
        float dy = y1 - y0;
        float dis = (float) Math.sqrt(dx * dx + dy * dy);

        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            lastPointDistance = dis;
        } else if (action == MotionEvent.ACTION_MOVE) {

            float scale = (dis / lastPointDistance);
            lastPointDistance = dis;

            float cx = (x1 + x0) / 2;
            float cy = (y1 + y0) / 2;

            setScale(scale, cx, cy);
        }
    }

    /**
     * 以(cx,cy)为基点，缩放坐标系scale倍。
     * (cx,cy)中心在屏幕位置的坐标
     */
    private void setScale(float scale, float cx, float cy) {
        if (scale == 1
                || (scale < 1 && unitScale >= MAX_UNIT_SCALE)
                || (scale > 1 && unitScale <= -MAX_UNIT_SCALE)) {
            return;
        }

        float dx = cx - original.x;
        float dy = cy - original.y;
        float scaleToX = dx * scale;
        float scaleToY = dy * scale;
        float tx = cx - scaleToX;
        float ty = cy - scaleToY;
        original.set(tx, ty);

        unitLength = unitLength * scale;

        if (scale > 1) {
            if (onScaleStateChangeListener != null) {
                onScaleStateChangeListener.onScaleSmallStateChange(true);
            }

            float limit = unitBase == 5 ? 2.5f : 2;

            while (unitLength >= STANDARD_UNIT_LENGTH * limit * 0.75f) {

                unitLength /= limit;

                if (unitBase == 1) {
                    unitBase = 5;
                    unitScale--;
                } else if (unitBase == 2) {
                    unitBase = 1;
                } else if (unitBase == 5) {
                    unitBase = 2;
                }

                limit = unitBase == 5 ? 2.5f : 2;
            }

            if (unitScale <= -MAX_UNIT_SCALE && unitLength <= STANDARD_UNIT_LENGTH) {
                if (onScaleStateChangeListener != null) {
                    onScaleStateChangeListener.onScaleLargeStateChange(false);
                }
            }

        } else {
            if (onScaleStateChangeListener != null) {
                onScaleStateChangeListener.onScaleLargeStateChange(true);
            }

            while (unitLength <= STANDARD_UNIT_LENGTH * 0.75f) {

                if (unitBase == 1) {
                    unitLength *= 2;
                    unitBase = 2;
                } else if (unitBase == 2) {
                    unitLength *= 2.5f;
                    unitBase = 5;
                } else if (unitBase == 5) {
                    unitLength *= 2;
                    unitBase = 1;
                    unitScale++;
                }

            }

            if (unitScale >= MAX_UNIT_SCALE && unitLength >= STANDARD_UNIT_LENGTH) {
                if (onScaleStateChangeListener != null) {
                    onScaleStateChangeListener.onScaleSmallStateChange(false);
                }
            }
        }

        postInvalidate();
    }

    private String getUnitString(int value) {
        int sign = (int) Math.signum(value);
        int base = Math.abs(value) * unitBase;
        int scale = unitScale;

        while (base % 10 == 0) {
            base /= 10;
            scale++;
        }

        int log = 0;
        int tempBase = base;
        while (tempBase >= 10) {
            tempBase /= 10;
            log++;
        }

        if (Math.abs(scale + log) >= 4) {
            //10000以上的或小于0.001的数用科学计数法
            StringBuilder sb = new StringBuilder();
            sb.append(sign * base);
            sb.insert(1, '.');
            sb.append("e").append(log + scale);
            return sb.toString();

        } else if (scale >= 0) {// 单位长度是整数
            return "" + sign * base * (int) Math.pow(10, scale);

        } else {//单位长度不足1且大于等于0.001的小数,直接显示
            StringBuilder sb = new StringBuilder();
            int dotPosition = scale + log;
            if (dotPosition >= 0) {
                sb.append(base);
                sb.insert(dotPosition + 1, '.');
            } else {
                sb.append("0.");
                for (int i = -1; i > dotPosition; i--) {
                    sb.append('0');
                }
                sb.append(base);
            }

            if (sign < 0) {
                sb.insert(0, '-');
            }

            return sb.toString();
        }
    }

    public interface OnScaleStateChangeListener {
        void onScaleLargeStateChange(boolean enable);

        void onScaleSmallStateChange(boolean enable);
    }

    /**
     * 获取坐标系的状态信息
     */
    public Parcelable getCoordinateState() {
        return new CoordinateState(this);
    }

    /**
     * 恢复之前获取的坐标系状态信息
     */
    public void setCoordinateState(Parcelable o) {
        CoordinateState state = (CoordinateState) o;

        unitBase = state.unitBase;
        unitScale = state.unitScale;
        unitLength = state.unitLength;
        original = new PointF(state.original.x, state.original.y);

        invalidate();
    }

    private static class CoordinateState implements Parcelable {

        private int unitBase;
        private int unitScale;
        private float unitLength;
        private PointF original;

        public CoordinateState(Coordinate2D coordinate) {
            unitBase = coordinate.unitBase;
            unitScale = coordinate.unitScale;
            unitLength = coordinate.unitLength;

            if (coordinate.original != null) {
                PointF o = coordinate.original;
                float dx = coordinate.width / 2 - o.x;
                float dy = coordinate.height / 2 - o.y;

                original = new PointF(dx, dy);
            } else {
                original = new PointF(0, 0);
            }
        }

        protected CoordinateState(Parcel in) {
            unitBase = in.readInt();
            unitScale = in.readInt();
            unitLength = in.readFloat();
            original = in.readParcelable(PointF.class.getClassLoader());
        }

        public static final Creator<CoordinateState> CREATOR = new Creator<CoordinateState>() {
            @Override
            public CoordinateState createFromParcel(Parcel in) {
                return new CoordinateState(in);
            }

            @Override
            public CoordinateState[] newArray(int size) {
                return new CoordinateState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(unitBase);
            dest.writeInt(unitScale);
            dest.writeFloat(unitLength);
            dest.writeParcelable(original, flags);
        }
    }
}
