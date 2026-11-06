package com.gathin.tvhome;

/** One-time separation of the existing bolts and their halo from the registered wallpaper. */
final class LightningPixels {
    final int[] repair, bolts;
    private LightningPixels(int count) { repair = new int[count]; bolts = new int[count]; }

    static LightningPixels split(int[] original, int w, int h) {
        int radius = Math.max(3, Math.min(24, Math.round(w * .011f)));
        int feather = Math.max(2, radius * 3 / 4);
        byte[] distance = new byte[w * h];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++)
            distance[y * w + x] = (byte) (LightningMask.alpha(original[y * w + x], x / (float) w, y / (float) h) > 80 ? 0 : 100);
        // Distance to the actual bright cores widens the extraction to include their painted halo.
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int i = y * w + x, d = distance[i];
            if (x > 0) d = Math.min(d, distance[i - 1] + 1);
            if (y > 0) d = Math.min(d, distance[i - w] + 1);
            distance[i] = (byte) d;
        }
        for (int y = h - 1; y >= 0; y--) for (int x = w - 1; x >= 0; x--) {
            int i = y * w + x, d = distance[i];
            if (x + 1 < w) d = Math.min(d, distance[i + 1] + 1);
            if (y + 1 < h) d = Math.min(d, distance[i + w] + 1);
            distance[i] = (byte) d;
        }
        Forest forest = new Forest(original, distance, w, h, radius);
        LightningPixels result = new LightningPixels(w * h);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int i = y * w + x;
            if (distance[i] >= radius || LightningMask.protectedArtwork(x / (float) w, y / (float) h)) continue;
            int alpha = Math.min(255, (radius - distance[i]) * 255 / feather);
            int background = forest.sample(x, y, radius * 3);
            float edge = Math.min(1, LightningMask.distanceFromBody(x, y, w, h) / Math.max(3f, w * .011f));
            int repairAlpha = Math.round(alpha * edge * edge * (3 - 2 * edge));
            result.repair[i] = (repairAlpha << 24) | background;
            int color = original[i], r = (color >> 16) & 255, g = (color >> 8) & 255, b = color & 255;
            int emission = r >= 244 && g >= 240 && b >= 225 ? 255
                    : r >= g && g >= b + 6 ? Math.min(255, Math.max(0, (r - b - 20) * 3)) : 0;
            result.bolts[i] = (alpha * emission / 255 << 24) | (color & 0xFFFFFF);
        }
        return result;
    }

    /** Normalized local blur excludes bolts, warm halo and the character from its samples. */
    private static final class Forest {
        final int w, h, stride;
        final int[][] sum;
        Forest(int[] original, byte[] distance, int w, int h, int radius) {
            this.w = w; this.h = h; stride = w + 1;
            sum = new int[4][stride * (h + 1)];
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                int p = original[y * w + x], r = (p >> 16) & 255, g = (p >> 8) & 255, b = p & 255;
                boolean valid = distance[y * w + x] >= radius + 2
                        && !(r > 60 && r > g + 15 && r > b + 20)
                        && !LightningMask.protectedArtwork(x / (float) w, y / (float) h);
                int i = (y + 1) * stride + x + 1;
                for (int channel = 0; channel < 4; channel++) {
                    int value = valid ? (channel == 0 ? r : channel == 1 ? g : channel == 2 ? b : 1) : 0;
                    sum[channel][i] = value + sum[channel][i - 1] + sum[channel][i - stride] - sum[channel][i - stride - 1];
                }
            }
        }
        int sample(int x, int y, int radius) {
            int left = Math.max(0, x - radius), right = Math.min(w, x + radius + 1);
            int top = Math.max(0, y - radius), bottom = Math.min(h, y + radius + 1);
            int count = area(3, left, top, right, bottom);
            if (count == 0) return 0x101A20;
            return (area(0, left, top, right, bottom) / count << 16)
                    | (area(1, left, top, right, bottom) / count << 8)
                    | area(2, left, top, right, bottom) / count;
        }
        int area(int channel, int l, int t, int r, int b) {
            int[] s = sum[channel];
            return s[b * stride + r] - s[t * stride + r] - s[b * stride + l] + s[t * stride + l];
        }
    }
}
