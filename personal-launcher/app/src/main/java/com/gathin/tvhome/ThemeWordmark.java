package com.gathin.tvhome;

import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/** Lightweight white Home marks; All apps keeps the installed apps' original icons. */
final class ThemeWordmark extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path triangle = new Path();
    private final Path hotstarMark = new Path();
    private final Path storeMark = new Path();
    private final Path flickyMark = new Path();
    private final boolean youtube;
    private final boolean alder;
    private final boolean hotstar;
    private final boolean jiotv;
    private final boolean browser, files, shevery, aurora, flicky;
    private int alpha = 255;
    private final int focusedColor;
    private int foreground = 0xFFFFFF;
    private final Bitmap iconMask;
    private final RectF iconBounds = new RectF(-32, -32, 32, 32);

    ThemeWordmark(String pkg, int accent, Drawable icon) {
        int r = Math.round(255 * 0.75f + ((accent >> 16) & 255) * 0.25f);
        int g = Math.round(255 * 0.75f + ((accent >> 8) & 255) * 0.25f);
        int b = Math.round(255 * 0.75f + (accent & 255) * 0.25f);
        focusedColor = (r << 16) | (g << 8) | b;
        youtube = "com.jio.yt".equals(pkg) || "com.google.android.youtube.tv".equals(pkg);
        alder = "dev.alder".equals(pkg);
        hotstar = "in.startv.hotstar".equals(pkg);
        jiotv = "com.jio.media.jiotvplus".equals(pkg);
        browser = "com.phlox.tvwebbrowser".equals(pkg);
        files = "me.zhanghai.android.files".equals(pkg);
        shevery = "com.hamondev.shevery".equals(pkg);
        aurora = "com.aurora.store".equals(pkg);
        flicky = "app.flicky".equals(pkg);
        iconMask = hasLogo() || icon == null ? null : monochromeMask(icon);
        if (iconMask != null) {
            float fit = 64f / Math.max(iconMask.getWidth(), iconMask.getHeight());
            float w = iconMask.getWidth() * fit / 2, h = iconMask.getHeight() * fit / 2;
            iconBounds.set(-w, -h, w, h);
        }
        paint.setColor(0xFFFFFFFF);
        triangle.moveTo(-5, -9);
        triangle.lineTo(11, 0);
        triangle.lineTo(-5, 9);
        triangle.close();
        storeMark.moveTo(0, -23);
        storeMark.lineTo(26, 23);
        storeMark.lineTo(-26, 23);
        storeMark.close();
        // Flicky's installed monochrome vector, kept as curves instead of a small raster mask.
        for (int side : new int[]{1, -1}) {
            flickyMark.moveTo(512 + side * 14.15668f, 469.58869f);
            flickyMark.rCubicTo(0, 0, side * 84.94009f, -212.35022f, side * 127.41013f, -96.73732f);
            flickyMark.rCubicTo(side * 42.47005f, 115.6129f, side * -92.01843f, 172.23963f, side * -92.01843f, 172.23963f);
            flickyMark.rCubicTo(0, 0, side * 153.36406f, 51.90783f, side * 108.53456f, 113.25345f);
            flickyMark.rCubicTo(side * -44.82949f, 61.34562f, side * -153.36405f, 0, side * -153.36405f, 0);
            flickyMark.lineTo(512, 530.93432f);
            flickyMark.close();
        }
        Matrix markTransform = new Matrix();
        markTransform.setScale(0.19f, 0.19f);
        markTransform.postTranslate(-512 * 0.19f, -512 * 0.19f);
        flickyMark.transform(markTransform);
        // Silhouette from the owner's supplied Hotstar mark, normalized from its 640px reference.
        float[] points = {548,113, 403,294, 481,374, 353,348, 304,522, 280,358,
                167,389, 256,322, 91,246, 266,275, 227,206, 299,261, 336,135, 353,263};
        for (int i = 0; i < points.length; i += 2) {
            float x = (points[i] - 320) * 0.15f, y = (points[i + 1] - 320) * 0.15f;
            if (i == 0) hotstarMark.moveTo(x, y); else hotstarMark.lineTo(x, y);
        }
        hotstarMark.close();
    }

    boolean hasLogo() {
        return youtube || alder || hotstar || jiotv || browser || files || shevery || aurora || flicky;
    }

    private static Bitmap monochromeMask(Drawable icon) {
        final int size = 96;
        Bitmap image = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Rect previous = new Rect(icon.getBounds());
        try {
            icon.setBounds(0, 0, size, size);
            icon.draw(new Canvas(image));
        } finally {
            icon.setBounds(previous);
        }
        int[] pixels = new int[size * size];
        image.getPixels(pixels, 0, size, 0, 0, size, size);
        int[] edges = {pixels[4 * size + size / 2], pixels[(size - 5) * size + size / 2],
                pixels[(size / 2) * size + 4], pixels[(size / 2) * size + size - 5]};
        int background = 0;
        boolean removePlate = false;
        for (int candidate : edges) {
            if ((candidate >>> 24) < 240) continue;
            int matches = 0;
            for (int edge : edges) {
                if ((edge >>> 24) >= 240 && colorDistance(candidate, edge) < 3600) matches++;
            }
            if (matches >= 3) { background = candidate; removePlate = true; break; }
        }
        long originalAlpha = 0, retainedAlpha = 0;
        for (int pixel : pixels) {
            int opacity = pixel >>> 24;
            originalAlpha += opacity;
            if (colorDistance(pixel, background) >= 3600) retainedAlpha += opacity;
        }
        // A flat glyph touching the sampled edges must not be mistaken for its plate.
        removePlate &= retainedAlpha >= originalAlpha * 0.05f;
        int left = size, top = size, right = -1, bottom = -1;
        // Strip a flat icon plate; keep the foreground's alpha and transparent cutouts.
        for (int i = 0; i < pixels.length; i++) {
            if (removePlate && colorDistance(pixels[i], background) < 3600) pixels[i] = 0;
            if ((pixels[i] >>> 24) != 0) {
                left = Math.min(left, i % size); right = Math.max(right, i % size);
                top = Math.min(top, i / size); bottom = Math.max(bottom, i / size);
            }
        }
        image.setPixels(pixels, 0, size, 0, 0, size, size);
        Bitmap mask = image.extractAlpha();
        image.recycle();
        if (right >= left && bottom >= top) {
            Bitmap cropped = Bitmap.createBitmap(mask, left, top, right - left + 1, bottom - top + 1);
            if (cropped != mask) mask.recycle();
            return cropped;
        }
        return mask;
    }

    private static int colorDistance(int a, int b) {
        int r = ((a >> 16) & 255) - ((b >> 16) & 255);
        int g = ((a >> 8) & 255) - ((b >> 8) & 255);
        int blue = (a & 255) - (b & 255);
        return r * r + g * g + blue * blue;
    }

    void setFocused(boolean focused) {
        int color = focused ? focusedColor : 0xFFFFFF;
        if (foreground == color) return;
        foreground = color;
        invalidateSelf();
    }

    @Override public void draw(Canvas canvas) {
        android.graphics.Rect b = getBounds();
        canvas.save();
        canvas.translate(b.exactCenterX(), b.exactCenterY());
        float scale = Math.min(b.width() / 200f, b.height() / 110f);
        canvas.scale(scale, scale);
        if (youtube) {
            paint.setColor((alpha << 24) | foreground);
            canvas.drawRoundRect(-32, -22, 32, 22, 9, 9, paint);
            paint.setColor((alpha << 24) | 0x050810);
            canvas.drawPath(triangle, paint);
        } else if (hotstar) {
            paint.setColor((alpha << 24) | foreground);
            canvas.drawPath(hotstarMark, paint);
        } else if (alder) {
            paint.setColor((alpha << 24) | foreground);
            canvas.save();
            canvas.rotate(45);
            canvas.drawRoundRect(-25, -25, 25, 25, 4, 4, paint);
            canvas.restore();
            paint.setColor((alpha << 24) | 0x050810);
            canvas.drawPath(triangle, paint);
        } else if (jiotv) {
            paint.setColor((alpha << 24) | foreground);
            canvas.drawRoundRect(-34, -26, 34, 19, 11, 11, paint);
            canvas.drawRoundRect(-14, 23, 14, 30, 3.5f, 3.5f, paint);
            paint.setColor((alpha << 24) | 0x050810);
            canvas.save();
            canvas.translate(0, -3);
            canvas.drawPath(triangle, paint);
            canvas.restore();
        } else if (browser) {
            paint.setColor((alpha << 24) | foreground);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.5f);
            canvas.drawCircle(0, 0, 26, paint);
            canvas.drawOval(-13, -26, 13, 26, paint);
            canvas.drawLine(-26, 0, 26, 0, paint);
            canvas.drawLine(-22, -13, 22, -13, paint);
            canvas.drawLine(-22, 13, 22, 13, paint);
            paint.setStyle(Paint.Style.FILL);
        } else if (files) {
            paint.setColor((alpha << 24) | foreground);
            canvas.drawRoundRect(-30, -16, 30, 24, 4, 4, paint);
            canvas.drawRoundRect(-30, -24, -2, -10, 4, 4, paint);
        } else if (shevery) {
            paint.setColor((alpha << 24) | foreground);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5f);
            canvas.drawCircle(0, 0, 19, paint);
            for (int i = 0; i < 8; i++) {
                canvas.drawLine(0, -19, 0, -28, paint);
                canvas.rotate(45);
            }
            paint.setStrokeWidth(3f);
            canvas.drawCircle(0, 0, 6, paint);
            paint.setStyle(Paint.Style.FILL);
        } else if (aurora) {
            paint.setColor((alpha << 24) | foreground);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(18f);
            canvas.drawPath(storeMark, paint);
            paint.setStyle(Paint.Style.FILL);
        } else if (flicky) {
            paint.setColor((alpha << 24) | foreground);
            canvas.drawPath(flickyMark, paint);
        } else if (iconMask != null) {
            paint.setColor((alpha << 24) | foreground);
            paint.setFilterBitmap(true);
            canvas.drawBitmap(iconMask, null, iconBounds, paint);
        }
        canvas.restore();
    }

    @Override public void setAlpha(int alpha) { this.alpha = alpha; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); invalidateSelf(); }
    @SuppressWarnings("deprecation")
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
