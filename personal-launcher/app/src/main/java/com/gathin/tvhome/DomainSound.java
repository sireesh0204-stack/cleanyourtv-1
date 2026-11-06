package com.gathin.tvhome;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

/** Original short synthesized chord, no recordings or background player. */
final class DomainSound {
    private AudioTrack track;
    void play(boolean shrine) {
        stop();
        int rate = 22050, count = rate * 6 / 5;
        short[] samples = new short[count];
        double base = shrine ? 110 : 220;
        for (int i = 0; i < count; i++) {
            double t = i / (double) rate, envelope = Math.sin(Math.PI * i / count) * Math.exp(-2.8 * t);
            samples[i] = (short)(2200 * envelope * (Math.sin(2 * Math.PI * base * t) + .4 * Math.sin(2 * Math.PI * base * 1.5 * t)));
        }
        try {
            track = new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    .setAudioFormat(new AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(count * 2).setTransferMode(AudioTrack.MODE_STATIC).build();
            track.write(samples, 0, count); track.play();
        } catch (RuntimeException unavailable) { stop(); }
    }
    void stop() { if (track != null) { try { track.stop(); } catch (IllegalStateException ignored) {} track.release(); track = null; } }
}
