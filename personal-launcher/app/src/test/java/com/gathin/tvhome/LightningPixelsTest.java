package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;

public class LightningPixelsTest {
    @Test public void movesOriginalBoltPixelsOntoACleanBackground() {
        int w = 40, h = 30, forest = 0xFF102830, gold = 0xFFFFC040;
        int[] source = new int[w * h]; Arrays.fill(source, forest);
        for (int y = 0; y < h; y++) source[y * w + 12] = gold;
        LightningPixels layers = LightningPixels.split(source, w, h);
        assertEquals(gold, layers.bolts[15 * w + 12]);
        assertEquals(forest, layers.repair[15 * w + 12]);
        assertEquals(0, layers.repair[15 * w + 2]);
        assertEquals(0, layers.bolts[15 * w + 11] >>> 24);
        assertEquals(gold, source[15 * w + 12]);
    }
    @Test public void protectsTheCharacterFromExtractionAndRepair() {
        int w = 40, h = 30; int[] source = new int[w * h];
        Arrays.fill(source, 0xFFFFC040);
        LightningPixels layers = LightningPixels.split(source, w, h);
        int character = 6 * w + 24;
        assertEquals(0, layers.bolts[character]);
        assertEquals(0, layers.repair[character]);
    }
    @Test public void boltBranchesMoveByVisibleDistancesWithoutUnboundedDrift() {
        float[] a = new float[3], b = new float[3];
        LightningMotion.transform(a, 1920, 1080, 1000, 1);
        LightningMotion.transform(b, 1920, 1080, 1400, 1);
        assertTrue(Math.hypot(a[0] - b[0], a[1] - b[1]) > 10);
        assertTrue(Math.abs(b[0]) < 23 && Math.abs(b[1]) < 13 && Math.abs(b[2]) < 2);
    }
    @Test public void motionRemainsContinuousAcrossTwoMinuteBoundary() {
        float[] a = new float[3], b = new float[3];
        LightningMotion.transform(a, 1920, 1080, 119999, 1);
        LightningMotion.transform(b, 1920, 1080, 120000, 1);
        for (int i = 0; i < a.length; i++) assertTrue(Math.abs(a[i] - b[i]) < .2f);
    }
}
