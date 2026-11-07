package org.telegram.owpengram;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;

import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;

/**
 * Draws the account owner's avatar clipped to the currently selected
 * profile frame shape (circle by default). The corner radius is animated
 * when the user drags the shape slider.
 */
public class AvatarPreviewView extends View {

    private final ImageReceiver imageReceiver = new ImageReceiver();
    private final AvatarDrawable avatarDrawable = new AvatarDrawable();
    private final RectF bounds = new RectF();

    private int sizePx;
    private float radiusPx;      // current visual radius
    private boolean userSet;
    private TLRPC.User user;
    private android.animation.ValueAnimator animator;

    private final ViewOutlineProvider outlineProvider = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radiusPx);
        }
    };

    public AvatarPreviewView(android.content.Context context, int account) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        sizePx = (int) dp(120);
        setOutlineProvider(outlineProvider);
        setClipToOutline(true);
        try {
            user = org.telegram.messenger.UserConfig.getInstance(account).getCurrentUser();
            if (user != null) {
                avatarDrawable.setInfo(account, user);
            }
        } catch (Throwable ignore) {
            user = null;
        }
    }

    private final float density;

    private float dp(float v) {
        return v * density;
    }

    public void setShapePercent(int percent, boolean animate) {
        float target = sizePx / 2.0f * CustomFeatures.frameFactor();
        cancelAnimator();
        if (!animate) {
            radiusPx = target;
            applyRadius();
            return;
        }
        final float from = radiusPx;
        final float to = target;
        animator = android.animation.ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(180);
        animator.addUpdateListener(animation -> {
            float f = (Float) animation.getAnimatedValue();
            radiusPx = from + (to - from) * f;
            applyRadius();
        });
        animator.start();
    }

    private void cancelAnimator() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    private void applyRadius() {
        invalidateOutline();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(sizePx, sizePx);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        imageReceiver.setParentView(this);
        imageReceiver.onAttachedToWindow();
        bindImage();
        radiusPx = sizePx / 2.0f * CustomFeatures.frameFactor();
        applyRadius();
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnimator();
        imageReceiver.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    private void bindImage() {
        try {
            ImageLocation location = ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_BIG);
            imageReceiver.setImage(location, "120_120", (Drawable) avatarDrawable, null, user, 0);
        } catch (Throwable ignore) {
            // avatar stays as initials placeholder
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        imageReceiver.setImageCoords(0, 0, w, h);
        bounds.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        imageReceiver.draw(canvas);
    }
}
