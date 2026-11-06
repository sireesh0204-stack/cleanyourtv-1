package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * A translucent glass slab: soft sheen fill, bright top specular, crisp rim and a faint outer glow.
 * One instance floats over the grid and is moved with spring animations between tiles.
 */
public class GlassView extends View {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint specular = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private float radius;
    /** Glow colour as 0xRRGGBB: white normally, a festival's accent (diya gold) while a festive skin is on. */
    private int glowRgb = 0xFFFFFF;
    /** Glow strength: the white glow stays faint, a coloured one needs more alpha to read as warm light. */
    private int glowAlpha = 0x30;
    /** glass_style rim or halo: no sheen and no fill, only the crisp rim (and, for halo, a lighter outer glow). */
    private boolean flat;
    private boolean halo;
    private final boolean spotlight;
    private boolean libraryOutline;
    private final Paint spotlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ValueAnimator breathing;
    private boolean loopRequested, loopAllowed;

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        spotlight = ThemeSettings.enabled(context);
        density = getResources().getDisplayMetrics().density;
        radius = 20f * density;
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(1.5f * density);
        rim.setColor(0xCCFFFFFF);
        glow.setStyle(Paint.Style.STROKE);
        // Cache the slab as a texture. The spring only changes translation, so the GPU can
        // move it without replaying clipPath and the glow on every frame. That path is
        // software-rendered on the MT5895 and is what makes the highlight crawl.
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /** Tints the rim and the outer glow; 0 restores the plain white glass. */
    void setAccent(int argb) {
        if (argb == 0) {
            glowRgb = 0xFFFFFF;
            glowAlpha = 0x30;
            rim.setColor(0xCCFFFFFF);
        } else {
            glowRgb = argb & 0xFFFFFF;
            glowAlpha = 0x70;
            // The rim is the accent lifted towards white, so the edge still reads as glass, not paint.
            int r = (((glowRgb >> 16) & 0xFF) + 255 * 2) / 3, g = (((glowRgb >> 8) & 0xFF) + 255 * 2) / 3,
                    b = ((glowRgb & 0xFF) + 255 * 2) / 3;
            rim.setColor(0xE6000000 | (r << 16) | (g << 8) | b);
        }
        if (spotlight && getWidth() > 0 && getHeight() > 0) {
            spotlightPaint.setShader(spotlightShader(getWidth(), getHeight(), 6 * density, glowRgb));
        }
        invalidate();
    }

    /** One of Config's GLASS_* styles. */
    void setStyle(String style) {
        flat = Config.GLASS_RIM.equals(style) || Config.GLASS_HALO.equals(style);
        halo = Config.GLASS_HALO.equals(style);
        rim.setStrokeWidth((flat ? 2f : 1.5f) * density);
        invalidate();
    }

    void setRadius(float px) {
        radius = px;
        invalidate();
    }

    /** Library-only treatment; Home keeps its existing radial focus. */
    void setLibraryOutline() {
        libraryOutline = true;
        updateLoop();
        invalidate();
    }

    void setLoopAllowed(boolean allowed) { loopAllowed = allowed; updateLoop(); }
    void setLooping(boolean requested) { loopRequested = requested; updateLoop(); }
    private void updateLoop() {
        boolean run = spotlight && !libraryOutline && loopAllowed && loopRequested
                && isAttachedToWindow() && getWindowVisibility() == VISIBLE && isShown()
                && hasWindowFocus() && ValueAnimator.areAnimatorsEnabled();
        if (!run) {
            if (breathing != null) { breathing.cancel(); breathing = null; }
            setScaleX(1f); setScaleY(1f);
        } else if (breathing == null) {
            breathing = ValueAnimator.ofFloat(1f, 1.04f);
            breathing.setDuration(2400);
            breathing.setRepeatCount(ValueAnimator.INFINITE);
            breathing.setRepeatMode(ValueAnimator.REVERSE);
            breathing.setInterpolator(new AccelerateDecelerateInterpolator());
            breathing.addUpdateListener(a -> { float scale = (float) a.getAnimatedValue(); setScaleX(scale); setScaleY(scale); });
            breathing.start();
        }
    }
    @Override protected void onAttachedToWindow() { super.onAttachedToWindow(); updateLoop(); }
    @Override protected void onDetachedFromWindow() { setLooping(false); super.onDetachedFromWindow(); }
    @Override public void onWindowFocusChanged(boolean focused) { super.onWindowFocusChanged(focused); updateLoop(); }
    @Override protected void onWindowVisibilityChanged(int visibility) { super.onWindowVisibilityChanged(visibility); updateLoop(); }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fill.setShader(new LinearGradient(0, 0, 0, h, 0x3DFFFFFF, 0x12FFFFFF, Shader.TileMode.CLAMP));
        specular.setShader(new LinearGradient(0, 0, 0, h * 0.45f, 0x66FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
        if (spotlight && w > 0 && h > 0) {
            spotlightPaint.setShader(spotlightShader(w, h, 6 * density, glowRgb));
        }
    }

    private static Shader spotlightShader(int width, int height, float inset, int color) {
        RadialGradient gradient = new RadialGradient(0, 0, 1,
                new int[]{0x8A000000 | color, 0x48000000 | color, color},
                new float[]{0f, 0.4f, 1f}, Shader.TileMode.CLAMP);
        android.graphics.Matrix transform = new android.graphics.Matrix();
        transform.setScale(Math.max(1, width / 2f - inset), Math.max(1, height / 2f - inset));
        transform.postTranslate(width / 2f, height / 2f);
        gradient.setLocalMatrix(transform);
        return gradient;
    }

    /** A separate instance per top control so hover and remote focus never share drawable state. */
    static Drawable focusBackground(float density) {
        return new Drawable() {
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private boolean highlighted;
            private final int extra = Math.round(8 * density);

            @Override protected void onBoundsChange(Rect bounds) {
                paint.setShader(spotlightShader(bounds.width() + 2 * extra, bounds.height() + 2 * extra, 0, 0xFFFFFF));
            }

            @Override public boolean isStateful() { return true; }

            @Override protected boolean onStateChange(int[] states) {
                boolean next = false;
                for (int state : states) {
                    if (state == android.R.attr.state_focused || state == android.R.attr.state_hovered
                            || state == android.R.attr.state_pressed) next = true;
                }
                if (next == highlighted) return false;
                highlighted = next;
                invalidateSelf();
                return true;
            }

            @Override public void draw(Canvas canvas) {
                if (!highlighted) return;
                Rect bounds = getBounds();
                canvas.save();
                canvas.translate(bounds.left - extra, bounds.top - extra);
                canvas.drawOval(0, 0, bounds.width() + 2 * extra, bounds.height() + 2 * extra, paint);
                canvas.restore();
            }

            @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
            @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
            @SuppressWarnings("deprecation")
            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = 6f * density;   // room for the glow
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);
        if (libraryOutline) {
            fill.setShader(null);
            fill.setColor(0x18FFFFFF);
            canvas.drawRoundRect(rect, radius, radius, fill);
            rim.setColor(0xFFFFFFFF);
            rim.setStrokeWidth(2 * density);
            canvas.drawRoundRect(rect, radius, radius, rim);
            return;
        }
        if (spotlight) {
            canvas.drawOval(rect, spotlightPaint);
            return;
        }

        // Outer glow: thin rings across the inset, each a little fainter, so it fades out smoothly. Three wide
        // strokes left visible bands once the glow took an app's colour at a stronger alpha. The hardware layer
        // caches the result, so the rings cost one draw, not one per frame of the spring.
        if (!flat || halo) {
            int rings = 12;
            float step = inset / rings;
            int alpha = flat ? glowAlpha / 2 : glowAlpha;
            glow.setStrokeWidth(step + 0.5f);
            for (int i = 0; i < rings; i++) {
                float o = step * (i + 0.5f);
                float t = 1f - (float) i / rings;
                glow.setColor(Math.round(alpha * t) << 24 | glowRgb);
                canvas.drawRoundRect(rect.left - o, rect.top - o, rect.right + o, rect.bottom + o, radius + o, radius + o, glow);
            }
        }
        if (flat) {
            canvas.drawRoundRect(rect, radius, radius, rim);
            return;
        }

        canvas.drawRoundRect(rect, radius, radius, fill);

        // Sheen across the top 45%: clipRect stays on the GPU (clipPath did not on the MT5895) and
        // the round rect under it keeps the top corners rounded with a flat bottom edge.
        canvas.save();
        canvas.clipRect(rect.left, rect.top, rect.right, rect.top + rect.height() * 0.45f);
        canvas.drawRoundRect(rect, radius, radius, specular);
        canvas.restore();

        canvas.drawRoundRect(rect, radius, radius, rim);
    }
}
