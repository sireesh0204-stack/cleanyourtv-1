package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.view.View;

/** A single native layer: selected cutout, registered gaze, blink and a bounded wake reveal. */
final class DomainView extends View {
    private final ThemeCatalog.Theme theme;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap character;
    private final float[][] eyes;
    private final CharacterEffects effects;
    private final CharacterStyle style;
    private final Bitmap wallpaper;
    private final boolean motion, sound;
    private final java.util.concurrent.Executor executor;
    private final Runnable frame = this::invalidate;
    private boolean running, receiverRegistered;
    private final DomainLifecycle lifecycle = new DomainLifecycle();
    private long revealStarted;
    private float gazeX, gazeY;
    private DomainSound audio;
    private final BroadcastReceiver wake = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) { lifecycle.screenOff(); stopFrames(); }
            else { lifecycle.screenOn(); beginFrames(); }
        }
    };
    DomainView(Context context, ThemeCatalog.Theme theme, boolean motion, boolean sound, Drawable backdrop, java.util.concurrent.Executor executor) {
        super(context); this.executor = executor; this.theme = theme; this.motion = motion; this.sound = sound;
        int resource = theme.character(context);
        character = resource == 0 ? null : BitmapFactory.decodeResource(getResources(), resource);
        eyes = eyeCoordinates(theme.id);
        effects = new CharacterEffects(theme.id); style = theme.style();
        wallpaper = backdrop instanceof BitmapDrawable ? ((BitmapDrawable) backdrop).getBitmap() : null;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setFocusable(false);
    }
    void start() { lifecycle.resume(); beginFrames(); }
    private void beginFrames() {
        if (!lifecycle.active() || running) return;
        running = true;
        if (lifecycle.consumeReveal()) {
            revealStarted = SystemClock.uptimeMillis();
            if (sound && style != CharacterStyle.NONE) { audio = new DomainSound(); audio.play(theme.id.equals("sukuna")); }
        }
        invalidate();
    }
    void stop() { lifecycle.pause(); stopFrames(); }
    private void stopFrames() { running = false; removeCallbacks(frame); if (audio != null) { audio.stop(); audio = null; } }
    void lookAt(float x, float y) { gazeX = x; gazeY = y; if (running) invalidate(); }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) { effects.size(w,h); }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON); filter.addAction(Intent.ACTION_SCREEN_OFF);
        getContext().registerReceiver(wake, filter); receiverRegistered = true;
    }
    @Override protected void onDetachedFromWindow() {
        stop(); effects.close(); if (receiverRegistered) { getContext().unregisterReceiver(wake); receiverRegistered = false; }
        super.onDetachedFromWindow();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (character == null && style == CharacterStyle.NONE) return;
        long now = SystemClock.uptimeMillis();
        boolean animate = running && motion && ValueAnimator.areAnimatorsEnabled();
        float progress = animate ? Math.min(1f, (now - revealStarted) / 1200f) : 1f;
        if (animate && style != CharacterStyle.SUKUNA) effects.ambient(canvas, now, wallpaper, gazeX / Math.max(1,getWidth()) * 2 - 1, gazeY / Math.max(1,getHeight()) * 2 - 1, progress, executor);
        if (character != null) {
        float scale = getHeight() * 0.84f / character.getHeight();
        float left = getWidth() * 0.97f - character.getWidth() * scale;
        float top = getHeight() * 0.14f;
        int save = canvas.save();
        // The artwork, gaze and reveal use the same transform so the eyes cannot drift off its face.
        if (progress < 1) canvas.clipRect(0, getHeight() * (1 - progress), getWidth(), getHeight());
        canvas.translate(left, top); canvas.scale(scale, scale);
        paint.setColor(Color.WHITE); paint.setAlpha(255);
        canvas.drawBitmap(character, 0, 0, paint);
        // Move only the hair silhouette, less than a source pixel, while keeping the face registered.
        if (animate && theme.id.equals("gojo")) {
            int hair = canvas.save(); canvas.clipRect(0, 0, character.getWidth(), 165);
            canvas.translate((float)Math.sin(now / 1900.0) * 0.65f, 0);
            canvas.drawBitmap(character, 0, 0, paint); canvas.restoreToCount(hair);
        }
        float blink = animate ? blink(now) : 1f;
        for (float[] eye : eyes) {
            float dx = Math.max(-1.6f, Math.min(1.6f, (gazeX - left - eye[0] * scale) / Math.max(1, getWidth()) * 2));
            float dy = Math.max(-0.8f, Math.min(0.8f, (gazeY - top - eye[1] * scale) / Math.max(1, getHeight())));
            paint.setColor(theme.accent); paint.setAlpha(110);
            canvas.drawOval(eye[0] - 6, eye[1] - 2.5f * blink, eye[0] + 6, eye[1] + 2.5f * blink, paint);
            paint.setColor(Color.WHITE); paint.setAlpha(190);
            canvas.drawOval(eye[0] + dx - 1.5f, eye[1] + dy - 2 * blink, eye[0] + dx + 1.5f, eye[1] + dy + 2 * blink, paint);
            if (blink < 1f) {
                paint.setColor(character.getPixel((int)eye[0], (int)eye[1] + 13));
                paint.setAlpha(Math.round((1 - blink) * 255));
                canvas.drawOval(eye[0] - 21, eye[1] - 7, eye[0] + 21, eye[1] + 7, paint);
                paint.setColor(0xFF302834); paint.setAlpha(Math.round((1 - blink) * 210)); paint.setStrokeWidth(1.3f);
                canvas.drawLine(eye[0] - 18, eye[1], eye[0] + 18, eye[1], paint);
            }
        }
        if (animate && theme.id.equals("sukuna")) {
            // Trace the forehead tattoo lightly; the body artwork remains untouched.
            paint.setColor(theme.accent); paint.setAlpha(12 + (int)(10 * (1 + Math.sin(now / 1700.0))));
            paint.setStrokeWidth(2f);
            canvas.drawLine(383, 234, 385, 252, paint);
            canvas.drawLine(385, 252, 392, 255, paint);
            canvas.drawLine(402, 228, 414, 241, paint);
            canvas.drawLine(414, 241, 406, 247, paint);
        }
        canvas.restoreToCount(save);
        }
        if (animate) {
            if (style == CharacterStyle.SUKUNA) effects.ambient(canvas, now, wallpaper, 0, 0, progress, executor);
            effects.reveal(canvas, progress);
        }
        removeCallbacks(frame);
        if (animate) postDelayed(frame, 33);
    }
    static float blink(long uptime) { long phase = uptime % 5300; return phase < 160 ? Math.abs(phase - 80) / 80f : 1f; }
    static float[][] eyeCoordinates(String theme) {
        if (theme.equals("gojo")) return new float[][]{{526,211},{609,235}};
        if (theme.equals("sukuna")) return new float[][]{{376,297},{455,267},{369,322},{469,289}};
        return new float[0][];
    }
}
