package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.view.View;
import android.view.animation.PathInterpolator;

/** Small eye overlays registered to bg_itachi's 1672 x 941 CENTER_CROP artwork. */
final class SharinganView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean motion;
    private ValueAnimator animator;
    private ValueAnimator blinkAnimator;
    private ValueAnimator wakeAnimator;
    private boolean active;
    private float gazeX, gazeY;
    private float blink, wake = 1f;
    private boolean pendingWake = true;
    private final Path irisClip = new Path();
    private final Path nearLid = new Path();
    private final Path farLid = new Path();
    private final Runnable blinkLater = this::blink;
    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                pendingWake = true;
                stop();
            } else if (isShown() && getWindowVisibility() == VISIBLE) {
                start();
            }
        }
    };

    SharinganView(Context context, boolean motion) {
        super(context);
        this.motion = motion;
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        nearLid.moveTo(1033, 328);
        nearLid.quadTo(1075, 278, 1143, 274);
        nearLid.quadTo(1145, 322, 1050, 331);
        nearLid.close();
        farLid.moveTo(852, 353);
        farLid.quadTo(878, 333, 915, 367);
        farLid.quadTo(883, 391, 861, 383);
        farLid.close();
    }

    void start() {
        if (active) return;
        active = true;
        if (!motion || !ValueAnimator.areAnimatorsEnabled()) { pendingWake = false; return; }
        postDelayed(blinkLater, 4500);
        if (pendingWake) {
            pendingWake = false;
            wakeAnimator = ValueAnimator.ofFloat(0f, 1f);
            wakeAnimator.setDuration(650);
            wakeAnimator.addUpdateListener(value -> {
                wake = (float) value.getAnimatedValue();
                invalidate();
            });
            wakeAnimator.start();
        }
    }

    void stop() {
        active = false;
        removeCallbacks(blinkLater);
        if (animator != null) animator.cancel();
        if (blinkAnimator != null) blinkAnimator.cancel();
        if (wakeAnimator != null) wakeAnimator.cancel();
        blink = 0f;
        wake = 1f;
        invalidate();
    }

    private void blink() {
        if (!active || !motion || !ValueAnimator.areAnimatorsEnabled()) return;
        blinkAnimator = ValueAnimator.ofFloat(0f, 1f, 0f);
        blinkAnimator.setDuration(160);
        blinkAnimator.addUpdateListener(value -> {
            blink = (float) value.getAnimatedValue();
            invalidate();
        });
        blinkAnimator.start();
        postDelayed(blinkLater, 5000 + (long) (Math.random() * 5000));
    }

    void lookAt(float x, float y) {
        if (!active || !motion || !ValueAnimator.areAnimatorsEnabled() || getWidth() == 0 || getHeight() == 0) return;
        float nextX = Math.max(-1f, Math.min(1f, x / getWidth() * 2f - 1f));
        float nextY = Math.max(-1f, Math.min(1f, y / getHeight() * 2f - 1f));
        if (Math.abs(nextX - gazeX) + Math.abs(nextY - gazeY) < 0.025f) return;
        if (animator != null) animator.cancel();
        float fromX = gazeX, fromY = gazeY;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(220);
        animator.setInterpolator(new PathInterpolator(0.23f, 1f, 0.32f, 1f));
        animator.addUpdateListener(value -> {
            float t = (float) value.getAnimatedValue();
            gazeX = fromX + (nextX - fromX) * t;
            gazeY = fromY + (nextY - fromY) * t;
            invalidate();
        });
        animator.start();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float scale = Math.max(getWidth() / 1672f, getHeight() / 941f);
        canvas.save();
        canvas.translate((getWidth() - 1672 * scale) / 2f, (getHeight() - 941 * scale) / 2f);
        canvas.scale(scale, scale);
        eye(canvas, 875f, 366f, 13f);
        eye(canvas, 1069f, 306f, 21f);
        if (blink > 0f) {
            lid(canvas, nearLid, 274, 331);
            lid(canvas, farLid, 333, 391);
        }
        canvas.restore();
        if (wake < 1f) {
            canvas.save();
            canvas.translate(getWidth() / 2f, getHeight() / 2f);
            float size = Math.min(getWidth(), getHeight()) * 0.085f;
            canvas.scale(size / 21f, size / 21f);
            canvas.rotate(wake * 240f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor((Math.round((1f - wake) * 210) << 24) | 0xCC1941);
            canvas.drawCircle(0, 0, 21, paint);
            paint.setColor((Math.round((1f - wake) * 255) << 24) | 0x080910);
            canvas.drawCircle(0, 0, 5, paint);
            for (int i = 0; i < 3; i++) {
                canvas.drawCircle(0, -13, 4, paint);
                canvas.rotate(120);
            }
            canvas.restore();
        }
    }

    private void eye(Canvas canvas, float x, float y, float radius) {
        canvas.save();
        irisClip.reset();
        irisClip.addCircle(x, y, radius, Path.Direction.CW);
        canvas.clipPath(irisClip);
        if (radius > 20) canvas.clipOutRect(1041, y - 30, 1053, y + 30);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFBF1238);
        canvas.drawCircle(x, y, radius, paint);
        canvas.translate(x + gazeX * radius * 0.18f, y + gazeY * radius * 0.12f);
        canvas.scale(radius / 24f, radius / 24f);
        canvas.rotate(gazeX * 18f);
        paint.setColor(0xFF280A17);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f);
        canvas.drawCircle(0, 0, 15f, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, 6f, paint);
        for (int i = 0; i < 3; i++) {
            canvas.drawCircle(0, -15f, 4.2f, paint);
            canvas.drawOval(-4.2f, -20f, -0.8f, -13f, paint);
            canvas.rotate(120f);
        }
        canvas.restore();
    }

    private void lid(Canvas canvas, Path shape, float top, float bottom) {
        canvas.save();
        canvas.clipPath(shape);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF454F66);
        float mid = (top + bottom) / 2;
        canvas.drawRect(830, top, 1150, top + (mid - top) * blink, paint);
        canvas.drawRect(830, bottom - (bottom - mid) * blink, 1150, bottom, paint);
        canvas.restore();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        if (Build.VERSION.SDK_INT >= 33) getContext().registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else getContext().registerReceiver(screenReceiver, filter);
    }

    @Override protected void onDetachedFromWindow() {
        stop();
        getContext().unregisterReceiver(screenReceiver);
        super.onDetachedFromWindow();
    }
}
