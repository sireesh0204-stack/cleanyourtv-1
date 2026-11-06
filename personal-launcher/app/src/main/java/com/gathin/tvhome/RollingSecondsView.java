package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.PathInterpolator;

import java.util.Locale;

/** A clipped two-digit dial; only a changed digit rolls, without triggering layout. */
public final class RollingSecondsView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean motion;
    private String previous = "00", current = "00";
    private int displayed = -1;
    private float progress = 1f;
    private ValueAnimator animator;
    private final Runnable tick = this::tick;

    public RollingSecondsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        paint.setTypeface(UiFont.get(context, R.string.font_light));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(18 * getResources().getDisplayMetrics().scaledDensity);
        paint.setColor(0xFFFFFFFF);
    }

    void setMotionEnabled(boolean enabled) { motion = enabled; }

    private void tick() {
        removeCallbacks(tick);
        if (!isAttachedToWindow() || getWindowVisibility() != VISIBLE || !isShown()) return;
        long now = System.currentTimeMillis();
        int seconds = (int) ((now / 1000) % 60);
        if (seconds != displayed) {
            if (animator != null) animator.cancel();
            previous = current;
            current = String.format(Locale.ROOT, "%02d", seconds);
            progress = 1f;
            if (displayed >= 0 && motion && ValueAnimator.areAnimatorsEnabled()) {
                animator = ValueAnimator.ofFloat(0f, 1f);
                animator.setDuration(280);
                animator.setInterpolator(new PathInterpolator(0.23f, 1f, 0.32f, 1f));
                animator.addUpdateListener(value -> { progress = (float) value.getAnimatedValue(); invalidate(); });
                animator.start();
            }
            displayed = seconds;
            invalidate();
        }
        postDelayed(tick, 1000 - System.currentTimeMillis() % 1000);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float baseline = (getHeight() - paint.ascent() - paint.descent()) / 2f;
        float travel = -paint.ascent() + paint.descent();
        canvas.save();
        canvas.clipRect(0, baseline + paint.ascent() - 1, getWidth(), baseline + paint.descent() + 1);
        for (int i = 0; i < 2; i++) {
            float x = getWidth() * (i == 0 ? 0.29f : 0.71f);
            if (previous.charAt(i) == current.charAt(i) || progress >= 1f) {
                paint.setAlpha(255);
                canvas.drawText(current, i, i + 1, x, baseline, paint);
            } else {
                paint.setAlpha(Math.round(255 * (1f - progress)));
                canvas.drawText(previous, i, i + 1, x, baseline - travel * progress, paint);
                paint.setAlpha(Math.round(255 * progress));
                canvas.drawText(current, i, i + 1, x, baseline + travel * (1f - progress), paint);
            }
        }
        paint.setAlpha(255);
        canvas.restore();
    }

    private void pause() {
        removeCallbacks(tick);
        if (animator != null) animator.cancel();
        progress = 1f;
    }

    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); tick(); }
    @Override protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (tick == null) return;
        pause();
        if (isShown()) post(tick);
    }
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (tick == null) return;
        pause();
        if (visibility == VISIBLE) post(tick);
    }
    @Override protected void onDetachedFromWindow() { pause(); super.onDetachedFromWindow(); }
}
