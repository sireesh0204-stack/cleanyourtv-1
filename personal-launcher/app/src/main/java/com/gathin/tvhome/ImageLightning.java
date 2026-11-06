package com.gathin.tvhome;

import android.graphics.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

/** Animates the wallpaper's existing bolts, not newly drawn geometric paths. */
final class ImageLightning {
    private volatile Bitmap ready;
    private boolean closed;
    private Bitmap mask;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix imageMatrix = new Matrix(), sweepMatrix = new Matrix();
    private final LinearGradient sweep = new LinearGradient(0, 0, 1, .18f,
            new int[]{0x00FFC448, 0x88FFD36A, 0xFFBFEFFF, 0x88FFD36A, 0x00FFC448},
            new float[]{0, .30f, .5f, .70f, 1}, Shader.TileMode.CLAMP);
    private BitmapShader stencil;
    private Shader energy;

    ImageLightning(Bitmap source, Executor executor) {
        // Share the decoded wallpaper. Only one alpha texture and one scanline are allocated.
        executor.execute(() -> {
            int w = source.getWidth(), h = source.getHeight();
            Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
            int stride = result.getRowBytes();
            byte[] alpha = new byte[stride * h];
            int[] row = new int[w];
            for (int y = 0; y < h; y++) {
                synchronized (this) { if (closed) { result.recycle(); return; } }
                source.getPixels(row, 0, w, 0, y, w, 1);
                for (int x = 0; x < w; x++)
                    alpha[y * stride + x] = (byte) LightningMask.alpha(row[x], x / (float) w, y / (float) h);
            }
            result.copyPixelsFromBuffer(ByteBuffer.wrap(alpha));
            synchronized (this) {
                if (closed) result.recycle();
                else ready = result;
            }
        });
    }

    void draw(Canvas canvas, int w, int h, long now, float wakeProgress) {
        Bitmap available = ready;
        if (available == null) return;
        if (mask == null) {
            mask = available;
            stencil = new BitmapShader(mask, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            // SRC_IN keeps the traveling gradient strictly inside the extracted bolt alpha.
            energy = new ComposeShader(stencil, sweep, PorterDuff.Mode.SRC_IN);
        }
        float scale = Math.max(w / (float) mask.getWidth(), h / (float) mask.getHeight());
        imageMatrix.setScale(scale, scale);
        imageMatrix.postTranslate((w - mask.getWidth() * scale) * .5f,
                (h - mask.getHeight() * scale) * .5f);
        stencil.setLocalMatrix(imageMatrix);

        float breath = (float) (.5 + .5 * Math.sin(now / 1200.0));
        paint.setShader(null);
        paint.setColor(Color.BLACK);
        paint.setAlpha((int) (155 - 100 * breath));
        canvas.drawBitmap(mask, imageMatrix, paint);
        paint.setColor(0xFFFFC448);
        paint.setAlpha((int) (35 + 80 * breath));
        canvas.drawBitmap(mask, imageMatrix, paint);

        float phase = wakeProgress < 1 ? wakeProgress : (now % 4800) / 4800f;
        sweepMatrix.setScale(w * .48f, h * 1.6f);
        sweepMatrix.postTranslate(w * (-.65f + phase * 2.2f), -h * .3f);
        sweep.setLocalMatrix(sweepMatrix);
        paint.setShader(energy);
        paint.setAlpha(235);
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
    }

    synchronized void close() {
        closed = true;
        // Displayed textures may still be referenced by RenderThread; let their owner be GC'd.
        ready = null;
        mask = null;
        energy = null;
        stencil = null;
    }
}
