package com.gathin.tvhome;

import android.graphics.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/** Moves the wallpaper's own bolt branches, with a cached clean patch behind them. */
final class ImageLightning {
    private static final class Branch {
        final Bitmap bitmap;
        final int left, top;
        Branch(Bitmap bitmap, int left, int top) { this.bitmap = bitmap; this.left = left; this.top = top; }
    }
    private static final class Layers {
        final Bitmap repair, steady;
        final List<Branch> branches;
        Layers(Bitmap repair, Bitmap steady, List<Branch> branches) { this.repair = repair; this.steady = steady; this.branches = branches; }
        void recycleUnused() { repair.recycle(); steady.recycle(); for (Branch b : branches) b.bitmap.recycle(); }
    }
    private volatile Layers ready;
    private volatile boolean closed;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix imageMatrix = new Matrix();
    private final float[] movement = new float[3];

    ImageLightning(Bitmap source, Executor executor) {
        int w = source.getWidth(), h = source.getHeight();
        executor.execute(() -> {
            if (closed) return;
            int[] original = new int[w * h];
            source.getPixels(original, 0, w, 0, 0, w, h);
            LightningPixels pixels = LightningPixels.split(original, w, h);
            if (closed) return;
            List<Branch> moving = branches(pixels.bolts, pixels.repair, original, w, h);
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++)
                if (!LightningMask.protectedArtwork(x / (float) w, y / (float) h)) original[y * w + x] = 0;
            Layers result = new Layers(Bitmap.createBitmap(pixels.repair, w, h, Bitmap.Config.ARGB_8888),
                    Bitmap.createBitmap(original, w, h, Bitmap.Config.ARGB_8888), moving);
            synchronized (this) {
                if (closed) result.recycleUnused();
                else ready = result;
            }
        });
    }

    private List<Branch> branches(int[] pixels, int[] repair, int[] original, int w, int h) {
        List<Branch> result = new ArrayList<>();
        boolean[] seen = new boolean[pixels.length];
        int[] queue = new int[pixels.length];
        for (int start = 0; start < pixels.length; start++) {
            if (seen[start] || pixels[start] >>> 24 < 8) continue;
            int head = 0, tail = 1, left = start % w, right = left, top = start / w, bottom = top;
            queue[0] = start; seen[start] = true;
            while (head < tail) {
                int i = queue[head++], x = i % w, y = i / w;
                left = Math.min(left, x); right = Math.max(right, x); top = Math.min(top, y); bottom = Math.max(bottom, y);
                for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                    int nx = x + dx, ny = y + dy;
                    if (nx < 0 || nx >= w || ny < 0 || ny >= h) continue;
                    int n = ny * w + nx;
                    if (!seen[n] && pixels[n] >>> 24 >= 8) { seen[n] = true; queue[tail++] = n; }
                }
            }
            if (tail < 500) {
                // Preserve tiny fragments in the static repair layer instead of issuing dozens of draws.
                for (int i = 0; i < tail; i++) repair[queue[i]] = original[queue[i]];
                continue;
            }
            int bw = right - left + 1, bh = bottom - top + 1;
            int[] crop = new int[bw * bh];
            for (int i = 0; i < tail; i++) {
                int point = queue[i]; crop[(point / w - top) * bw + point % w - left] = pixels[point];
            }
            result.add(new Branch(Bitmap.createBitmap(crop, bw, bh, Bitmap.Config.ARGB_8888), left, top));
        }
        return result;
    }

    void draw(Canvas canvas, int w, int h, long now, float wakeProgress) {
        Layers layers = ready;
        if (layers == null) return;
        int bw = layers.repair.getWidth(), bh = layers.repair.getHeight();
        float scale = Math.max(w / (float) bw, h / (float) bh);
        imageMatrix.setScale(scale, scale);
        imageMatrix.postTranslate((w - bw * scale) * .5f, (h - bh * scale) * .5f);
        int save = canvas.save();
        canvas.concat(imageMatrix);
        canvas.drawBitmap(layers.repair, 0, 0, paint);
        for (int i = 0; i < layers.branches.size(); i++) {
            Branch branch = layers.branches.get(i);
            LightningMotion.transform(movement, bw, bh, now, i);
            int local = canvas.save();
            canvas.translate(branch.left + movement[0], branch.top + movement[1]);
            canvas.rotate(movement[2], branch.bitmap.getWidth() * .5f, branch.bitmap.getHeight() * .5f);
            canvas.drawBitmap(branch.bitmap, 0, 0, paint);
            canvas.restoreToCount(local);
        }
        // A cached character cutout avoids rasterizing a complex destination clip every frame.
        canvas.drawBitmap(layers.steady, 0, 0, paint);
        canvas.restoreToCount(save);
    }

    synchronized void close() {
        closed = true;
        // RenderThread may still reference displayed textures; only unpublished results are recycled.
        ready = null;
    }
}
