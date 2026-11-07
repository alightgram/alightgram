package org.telegram.owpengram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

/**
 * Lightweight Telegram-styled slider: title on the left, live value on the
 * right, thin rounded track and a circular thumb. Works standalone and inside
 * ScrollView (it requests parent not to intercept while dragging).
 */
public class CustomSliderView extends View {

    public interface Callback {
        void onValueChanged(CustomSliderView view, int value, boolean fromUser, boolean finalValue);
    }

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final android.text.TextPaint textPaint = new android.text.TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private String title = "";
    private String valueText = "";
    private int value = 50; // 0..100
    private boolean pressed;
    private Callback callback;
    private float density = 1.0f;

    private int trackColor = 0x00000000;
    private int fillColor = 0xff3390ec;
    private int thumbColor = 0xffffffff;
    private int titleColor = 0xff000000;
    private int valueColor = 0xff3390ec;

    private float titleTextSize = 16.0f;
    private float valueTextSize = 15.0f;

    public CustomSliderView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        trackPaint.setStyle(Paint.Style.FILL);
        fillPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbStrokePaint.setStyle(Paint.Style.STROKE);
        thumbStrokePaint.setStrokeWidth(dp(2));
        setOnTouchListener(null);
        setClickable(true);
        setFocusable(true);
    }

    private float dp(float v) {
        return v * density;
    }

    public void setColors(int track, int fill, int thumb, int title, int value) {
        this.trackColor = track;
        this.fillColor = fill;
        this.thumbColor = thumb;
        this.titleColor = title;
        this.valueColor = value;
        invalidate();
    }

    public void setTitle(String t) {
        title = t == null ? "" : t;
        invalidate();
    }

    public void setValueText(String t) {
        valueText = t == null ? "" : t;
        invalidate();
    }

    public void setValue(int v, boolean notify) {
        if (v < 0) v = 0;
        if (v > 100) v = 100;
        value = v;
        if (notify && callback != null) {
            callback.onValueChanged(this, value, false, true);
        }
        invalidate();
    }

    public int getValue() {
        return value;
    }

    public void setCallback(Callback c) {
        callback = c;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, (int) dp(78));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                pressed = true;
                getParent().requestDisallowInterceptTouchEvent(true);
                setPressed(true);
                applyPosition(x, false, false);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                applyPosition(x, false, false);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                pressed = false;
                setPressed(false);
                getParent().requestDisallowInterceptTouchEvent(false);
                applyPosition(x, true, event.getActionMasked() == MotionEvent.ACTION_UP);
                break;
            }
        }
        return true;
    }

    private void applyPosition(float x, boolean allowHaptic, boolean isUp) {
        int w = getWidth() - (int) dp(44);
        if (w <= 0) {
            return;
        }
        int newValue = Math.round((x - dp(22)) / w * 100.0f);
        if (newValue < 0) newValue = 0;
        if (newValue > 100) newValue = 100;
        boolean changed = newValue != value;
        value = newValue;
        if (callback != null) {
            callback.onValueChanged(this, value, true, isUp);
        }
        if (changed && allowHaptic) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int w = getWidth();
        final int h = getHeight();

        // Title & value
        textPaint.setTextSize(dp(titleTextSize));
        textPaint.setColor(pressed ? fillColor : titleColor);
        textPaint.setFakeBoldText(true);
        float textY = dp(30);
        canvas.drawText(title, dp(22), textY, textPaint);
        if (valueText.length() > 0) {
            textPaint.setTextSize(dp(valueTextSize));
            textPaint.setFakeBoldText(false);
            textPaint.setColor(valueColor);
            float tw = textPaint.measureText(valueText);
            canvas.drawText(valueText, w - dp(22) - tw, textY, textPaint);
        }

        // Track
        float trackY = dp(56);
        float trackH = dp(6);
        float trackLeft = dp(22);
        float trackRight = w - dp(22);
        float radius = trackH / 2.0f;
        rect.set(trackLeft, trackY - trackH / 2.0f, trackRight, trackY + trackH / 2.0f);
        trackPaint.setColor(trackColor);
        canvas.drawRoundRect(rect, radius, radius, trackPaint);

        // Fill
        float fraction = value / 100.0f;
        float fillRight = trackLeft + (trackRight - trackLeft) * fraction;
        if (fillRight > fillLeft()) {
            rect.set(trackLeft, trackY - trackH / 2.0f, Math.max(fillRight, trackLeft + radius), trackY + trackH / 2.0f);
            fillPaint.setColor(fillColor);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
        }

        // Thumb
        float thumbX = Math.max(trackLeft + radius / 2, Math.min(fillRight, trackRight - radius / 2));
        float thumbR = dp(10);
        thumbPaint.setColor(thumbColor);
        canvas.drawCircle(thumbX, trackY, thumbR, thumbPaint);
        thumbStrokePaint.setColor(fillColor);
        canvas.drawCircle(thumbX, trackY, thumbR - dp(2), thumbStrokePaint);
    }

    private float fillLeft() {
        return dp(22);
    }
}
