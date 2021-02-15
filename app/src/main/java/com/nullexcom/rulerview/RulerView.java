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

public class RulerView extends View {

    private static final int DEFAULT_COLOR = Color.parseColor("#0094CC");
    private int spacing = 10;
    private Paint shortLinePaint = new Paint();
    private Paint longLinePaint = new Paint();
    private Paint indicatorPaint = new Paint();
    private Path indicatorPath = new Path();
    private TextPaint textPaint = new TextPaint();

    private int min = 100;
    private int max = 190;
    private int currentValue = 160;
    private int color = DEFAULT_COLOR;
    private int indicatorColor = DEFAULT_COLOR;

    private OnValueChangedListener onValueChangedListener;
    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int d = (int) Math.ceil(Math.abs(distanceX) / spacing);
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

    public RulerView(Context context) {
        super(context);
        init();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RulerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(getContext(), mListener);
        shortLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        shortLinePaint.setStrokeCap(Paint.Cap.SQUARE);
        shortLinePaint.setStrokeWidth(dp(1));
        shortLinePaint.setColor(color);
        longLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        longLinePaint.setStrokeCap(Paint.Cap.SQUARE);
        longLinePaint.setColor(color);
        longLinePaint.setStrokeWidth(dp(2));
        textPaint.setTextSize(dp(14));
        textPaint.setColor(color);

        indicatorPaint.setColor(indicatorColor);
        indicatorPaint.setStyle(Paint.Style.FILL);

        spacing = dp(10);
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = getHeight();

        int delta = calculateTranslateDelta();
        canvas.translate(delta, 0);
        for (int i = 0; i <= max - min; i++) {
            if (i % 5 == 0) {
                int x = spacing * i;
                int y = h / 2;
                canvas.drawLine(x, 0, x, y, longLinePaint);
                String text = String.valueOf(i + min);
                float textWidth = textPaint.measureText(text);
                canvas.drawText(text, x - (textWidth / 2), h / 2f + 40, textPaint);
            } else {
                int x = spacing * i;
                int y = h / 3;
                canvas.drawLine(x, 0, x, y, shortLinePaint);
            }
        }
        canvas.translate(-delta, 0);
        drawIndicator(canvas);
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

    private int calculateTranslateDelta() {
        int i = currentValue - min;
        int centerX = getWidth() / 2;
        return centerX - (spacing * i);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            int height = dp(100);
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
