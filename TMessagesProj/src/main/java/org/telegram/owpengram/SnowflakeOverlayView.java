package org.telegram.owpengram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Fullscreen non-interactive overlay that draws falling white dots ("snowflakes")
 * on top of the whole application window.
 *
 * The view never consumes touch events, never allocates in the draw loop and
 * automatically pauses when detached or when the window is not visible.
 */
public class SnowflakeOverlayView extends View implements Choreographer.FrameCallback {

    private static final int MIN_FLAKES = 18;
    private static final int MAX_FLAKES = 46;
    private static final long MAX_FRAME_DELTA_MS = 64;

    private static final class Flake {
        float x, y;          // px
        float radius;        // px (already includes user size factor)
        float baseRadius;    // px without user size factor
        float speed;         // px/s (without speed factor)
        float swayAmplitude; // px
        float swayFrequency; // rad/s
        float phase;         // rad
        float alpha;
        float drift;         // px/s constant wind
    }

    private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Flake[] flakes = new Flake[0];
    private long lastFrameTimeMs;
    private float frameDeltaSec = 1.0f / 60.0f;
    private long timeMs;
    private boolean running;
    private boolean windowVisible = true;
    private float density = 1.0f;
    private int lastWidth;
    private int lastHeight;

    public SnowflakeOverlayView(Context context) {
        super(context);
        setWillNotDraw(false);
        setClickable(false);
        setLongClickable(false);
        setFocusable(false);
        setFocusableInTouchMode(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        corePaint.setColor(0xFFFFFFFF);
        glowPaint.setColor(0xFFFFFFFF);
        density = context.getResources().getDisplayMetrics().density;
    }

    private int desiredFlakeCount() {
        int area = Math.max(1, lastWidth * lastHeight);
        int count = Math.round(area / 62000.0f);
        if (count < MIN_FLAKES) count = MIN_FLAKES;
        if (count > MAX_FLAKES) count = MAX_FLAKES;
        return count;
    }

    private void ensureFlakes() {
        int count = desiredFlakeCount();
        if (count == flakes.length && lastWidth == getWidth() && lastHeight == getHeight()) {
            return;
        }
        lastWidth = getWidth();
        lastHeight = getHeight();
        Flake[] old = flakes;
        Flake[] next = new Flake[count];
        for (int i = 0; i < count; i++) {
            Flake f;
            if (i < old.length) {
                f = old[i];
            } else {
                f = new Flake();
                f.baseRadius = dp(1.4f + (float) Math.random() * 2.4f);
                f.alpha = 0.35f + (float) Math.random() * 0.5f;
                f.speed = dp(28.0f + (float) Math.random() * 62.0f);
                f.swayAmplitude = dp(6.0f + (float) Math.random() * 18.0f);
                f.swayFrequency = 0.6f + (float) Math.random() * 1.2f;
                f.phase = (float) (Math.random() * Math.PI * 2.0);
                f.drift = dp(-8.0f + (float) Math.random() * 16.0f);
                f.x = (float) Math.random() * Math.max(1, lastWidth);
                f.y = (float) Math.random() * Math.max(1, lastHeight);
            }
            f.radius = f.baseRadius * CustomFeatures.snowSize;
            next[i] = f;
        }
        flakes = next;
    }

    private float dp(float v) {
        return v * density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && (w != lastWidth || h != lastHeight)) {
            ensureFlakes();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        windowVisible = visibility == VISIBLE;
        if (windowVisible && CustomFeatures.snowEnabled) {
            start();
        } else {
            stop();
        }
    }

    /** Called whenever the user changes snow settings. */
    public void onSettingsChanged() {
        boolean enabled = CustomFeatures.snowEnabled;
        setVisibility(enabled ? VISIBLE : GONE);
        if (enabled && isAttachedToWindow() && windowVisible) {
            // Re-apply size factor immediately
            for (Flake f : flakes) {
                f.radius = f.baseRadius * CustomFeatures.snowSize;
            }
            start();
        } else {
            stop();
        }
    }

    private void start() {
        if (!running && getVisibility() == VISIBLE && windowVisible && CustomFeatures.snowEnabled) {
            running = true;
            lastFrameTimeMs = SystemClock.uptimeMillis();
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    private void stop() {
        if (running) {
            running = false;
            Choreographer.getInstance().removeFrameCallback(this);
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!running) {
            return;
        }
        if (!isAttachedToWindow() || !windowVisible || !CustomFeatures.snowEnabled || getVisibility() != VISIBLE) {
            running = false;
            return;
        }
        Choreographer.getInstance().postFrameCallback(this);
        long now = SystemClock.uptimeMillis();
        long delta = now - lastFrameTimeMs;
        lastFrameTimeMs = now;
        if (delta > MAX_FRAME_DELTA_MS) {
            delta = MAX_FRAME_DELTA_MS;
        }
        if (delta < 1) {
            delta = 1;
        }
        frameDeltaSec = delta / 1000.0f;
        timeMs += delta;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!CustomFeatures.snowEnabled) {
            return;
        }
        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        if (flakes.length == 0) {
            ensureFlakes();
        }
        final float dt = frameDeltaSec; // real frame time (works on 60/90/120 Hz)
        final float t = timeMs / 1000.0f;
        final float speedFactor = CustomFeatures.snowSpeed;
        final Flake[] arr = flakes;
        for (int i = 0; i < arr.length; i++) {
            Flake f = arr[i];
            if (f == null) {
                continue;
            }
            f.y += f.speed * speedFactor * dt;
            float sway = (float) Math.sin(t * f.swayFrequency + f.phase) * f.swayAmplitude * dt * 6.0f;
            f.x += f.drift * dt + sway;
            if (f.y - f.radius > h) {
                f.y = -f.radius - (float) Math.random() * h * 0.15f;
                f.x = (float) Math.random() * w;
            }
            if (f.x < -f.radius * 3) {
                f.x = w + f.radius * 2;
            } else if (f.x > w + f.radius * 3) {
                f.x = -f.radius * 2;
            }
            glowPaint.setAlpha(Math.round(f.alpha * 0.35f * 255.0f));
            canvas.drawCircle(f.x, f.y, f.radius * 1.7f, glowPaint);
            corePaint.setAlpha(Math.round(f.alpha * 255.0f));
            canvas.drawCircle(f.x, f.y, f.radius, corePaint);
        }
    }

    /**
     * Installs the overlay on top of the activity's window content.
     * Safe to call multiple times; never throws.
     */
    public static void attachToActivity(android.app.Activity activity) {
        try {
            if (activity == null || activity.isFinishing()) {
                return;
            }
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            ViewGroup decor = (ViewGroup) window.getDecorView();
            if (decor == null || decor.getChildCount() == 0) {
                return;
            }
            for (int i = 0; i < decor.getChildCount(); i++) {
                if (decor.getChildAt(i) instanceof SnowflakeOverlayView) {
                    return;
                }
            }
            CustomFeatures.init(activity);
            SnowflakeOverlayView view = new SnowflakeOverlayView(activity);
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setVisibility(CustomFeatures.snowEnabled ? VISIBLE : GONE);
            decor.addView(view, decor.getChildCount());
        } catch (Throwable ignore) {
            // Never crash the app because of a cosmetic overlay
        }
    }
}
