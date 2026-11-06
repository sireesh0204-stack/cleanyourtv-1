package com.gathin.tvhome;

/** Independent motion of the wallpaper's extracted connected bolt branches. */
final class LightningMotion {
    static void transform(float[] out, int w, int h, long now, int branch) {
        double t = now / 1000.0, phase = branch * 1.73;
        out[0] = (float) Math.sin(t * 3.8 + phase) * w * .0115f;
        out[1] = (float) Math.sin(t * 4.6 + phase * .7) * h * .012f;
        out[2] = (float) Math.sin(t * 2.1 + phase) * 1.6f;
    }
}
