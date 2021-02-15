package com.nullexcom.rulerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class WeightView extends View {

    private static final int SPACING = 2;
    private static final int DEFAULT_COLOR = Color.parseColor("#0094CC");
    private static final int ARC_START_ANGLE = -40;
    private static final int ARC_SWEEP_ANGLE = -100;

    private Paint backgroundPaint = new Paint();
    private Paint shortLinePaint = new Paint();
    private Paint longLinePaint = new Paint();
    private Paint indicatorPaint = new Paint();
    private Path indicatorPath = new Path();
    private TextPaint textPaint = new TextPaint();
    private Paint paint = new Paint();

    private int color = DEFAULT_COLOR;
    private int indicatorColor = DEFAULT_COLOR;
    private int height = 100;
    private int min = 10;
    private int max = 120;
    private int currentValue = 50;

    private OnValueChangedListener onValueChangedListener;
    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int w = getWidth();
            int r = w / 2;
            double delta = 2 * Math.PI * r / 180;
            int d = (int) Math.ceil(Math.abs(distanceX) / delta);
            int newValue = distanceX > 0 ? (currentValue + d) : (currentValue - d);
            invalidate();
            if (newValue < min || newValue > max) {
                return false;
            }
            currentValue = newValue;
            if (onValueChangedListener != null) {
                onValueChangedListener.onValueChanged(currentValue);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    };

    public WeightView(Context context) {
        super(context);
        init();
    }

    public WeightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        height = dp(50);
        mGestureDetector = new GestureDetector(getContext(), mListener);

        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(dp(100));
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setAntiAlias(true);

        shortLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        shortLinePaint.setStrokeWidth(dp(1));
        shortLinePaint.setColor(color);
        shortLinePaint.setAntiAlias(true);

        longLinePaint.setStyle(Paint.Style.STROKE);
        longLinePaint.setColor(color);
        longLinePaint.setStrokeWidth(dp(2));
        longLinePaint.setAntiAlias(true);

        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setAntiAlias(true);

        textPaint.setTextSize(dp(14));
        textPaint.setColor(color);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(100));
        backgroundPaint.setAntiAlias(true);
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();

        float centerX = w / 2f;
        float centerY = w / 2f + dp(50);

        int center = min + 50 / SPACING;

        int d = currentValue - center;
        int visibleMin = min + d;
        int visibleMax = visibleMin + 100 / SPACING;

        canvas.drawArc(0, height, w, w + height, ARC_START_ANGLE, ARC_SWEEP_ANGLE, false, backgroundPaint);
        canvas.save();

        if (d < 0) {
            canvas.rotate(-d * SPACING, centerX, centerY);
        }
        canvas.rotate(-50, centerX, centerY);

        for (int i = 0; i <= max - min; i++) {
            int actual = i + min;
            if (actual < visibleMin || actual > visibleMax) continue;
            if (actual % 5 == 0) {
                canvas.drawLine(centerX, 0, centerX, height, longLinePaint);
                String text = String.valueOf(actual);
                float textWidth = textPaint.measureText(text);
                if (actual >= visibleMin + 2 && actual <= visibleMax - 2) {
                    canvas.drawText(text, centerX - (textWidth / 2), height + 40, textPaint);
                }
            } else {
                canvas.drawLine(centerX, 0, centerX, 0.6f * height, shortLinePaint);
            }
            canvas.rotate(SPACING, centerX, centerY);
        }

        canvas.restore();
        drawIndicator(canvas);


        canvas.save();


    }

    private void drawIndicator(Canvas canvas) {
        int centerX = getWidth() / 2;
        indicatorPath.reset();
        indicatorPath.setFillType(Path.FillType.EVEN_ODD);
        indicatorPath.moveTo(centerX - dp(5), 0);
        indicatorPath.lineTo(centerX, 20);
        indicatorPath.lineTo(centerX + dp(5), 0);
        indicatorPath.lineTo(centerX - dp(5), 0);
        indicatorPath.close();
        canvas.drawPath(indicatorPath, indicatorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            int width = getMeasuredWidth();
            int height = width / 2 - dp(50);
            setMeasuredDimension(getMeasuredWidth(), height);
        }
    }

    private int dp(int dp) {
        Context context = getContext();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * metrics.densityDpi / 160;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
