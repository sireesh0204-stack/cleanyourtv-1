package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** An idle-only layer: drifting typography and a sparse field of crimson embers. */
final class NightWatchView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean motion;
    private final ThemeCatalog.Theme theme;
    private final SimpleDateFormat clockFormat;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault());
    private final Runnable frame = this::invalidate;
    private boolean running;
    private long started, minute = -1;
    private String time = "", date = "";

    NightWatchView(Context context, boolean motion, ThemeCatalog.Theme theme) {
        super(context);
        this.motion = motion;
        this.theme = theme;
        clockFormat = new SimpleDateFormat(android.text.format.DateFormat.is24HourFormat(context) ? "HH:mm" : "h:mm", Locale.getDefault());
        paint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        setFocusable(false);
        setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setVisibility(GONE);
    }

    void start() {
        running = true;
        started = SystemClock.uptimeMillis();
        setVisibility(VISIBLE);
        setAlpha(0f);
        if (motion && ValueAnimator.areAnimatorsEnabled()) animate().alpha(1f).setDuration(300).start();
        else setAlpha(1f);
        invalidate();
    }

    void stop() {
        running = false;
        removeCallbacks(frame);
        animate().cancel();
        setVisibility(GONE);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!running) return;
        long now = System.currentTimeMillis();
        if (minute != now / 60_000) {
            minute = now / 60_000;
            Date value = new Date(now);
            time = clockFormat.format(value);
            date = dateFormat.format(value);
        }
        boolean animate = motion && ValueAnimator.areAnimatorsEnabled();
        float elapsed = (SystemClock.uptimeMillis() - started) / 1000f;
        float positionTime = animate ? elapsed : (now / 60_000) % 1200;
        float dp = getResources().getDisplayMetrics().density;
        paint.setColor(0x3801060D);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        for (int i = 0; i < (theme.builtin() ? 28 : 0); i++) {
            float seed = (i * 0.754878f + 0.173f) % 1f;
            float x = ((i * 0.381966f) % 1f) * getWidth()
                    + (float) Math.sin(positionTime * 0.12f + i) * 9 * dp;
            float rise = (positionTime * (0.008f + seed * 0.006f) + seed) % 1f;
            float y = (1f - rise) * getHeight();
            float visibility = (float) Math.sin(rise * Math.PI);
            paint.setColor((Math.round((45 + 55 * seed) * visibility) << 24) | (theme.accent & 0xFFFFFF));
            canvas.drawCircle(x, y, (0.6f + seed) * dp, paint);
        }
        if (theme.id.equals("sukuna")) {
            paint.setColor(0x08CC8790);
            for (int i = 0; i < 3; i++) {
                float offset = (float)Math.sin(positionTime / 28f + i) * getWidth() * .08f;
                float y = getHeight() * (.68f + i * .1f);
                canvas.drawOval(-getWidth() * .2f + offset, y, getWidth() * .8f + offset, y + 24 * dp, paint);
            }
        }
        if (theme.id.equals("gojo")) {
            paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(dp); paint.setColor(0x3274DCFF);
            for (int i = 0; i < 60; i++) {
                float a = i / 59f * (float)Math.PI * 2;
                float b = (i + 1) / 59f * (float)Math.PI * 2;
                float r = 60 * dp;
                canvas.drawLine(getWidth()*.30f + (float)Math.cos(a)*r, getHeight()*.36f + (float)Math.sin(2*a)*r*.5f,
                        getWidth()*.30f + (float)Math.cos(b)*r, getHeight()*.36f + (float)Math.sin(2*b)*r*.5f, paint);
            }
            paint.setStyle(Paint.Style.FILL);
        }
        // Clock placement stays clear of the character and drifts slowly to avoid a fixed bright patch.
        float x = getWidth() * (0.085f + 0.045f * (float) Math.sin(positionTime / 83f));
        float y = getHeight() * (0.70f + 0.065f * (float) Math.sin(positionTime / 109f));
        paint.setColor(0xC8E7EBF3);
        paint.setTextSize(66 * dp);
        canvas.drawText(time, x, y, paint);
        paint.setTextSize(13 * dp);
        paint.setColor(0x8DBCC6D8);
        canvas.drawText(date, x + 2 * dp, y + 25 * dp, paint);
        paint.setColor((0xAA000000 | (theme.accent & 0xFFFFFF)));
        canvas.drawRect(x + 2 * dp, y + 42 * dp, x + 30 * dp, y + 43 * dp, paint);
        removeCallbacks(frame);
        postDelayed(frame, animate ? 33 : 60_000 - now % 60_000);
    }

    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != VISIBLE) stop();
    }

    @Override protected void onDetachedFromWindow() { stop(); super.onDetachedFromWindow(); }
}
