package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class FrostBlurTest {
    @Test public void constantImageRemainsConstantAcrossBothAxes() {
        int[] source = {0xFF6382A1,0xFF6382A1,0xFF6382A1,0xFF6382A1};
        int[] result = HeroArt.boxBlur(HeroArt.boxBlur(source, 2, 2, 2, true), 2, 2, 2, false);
        assertArrayEquals(source, result);
    }
    @Test public void blurClampsEdgesAndDoesNotMutateInput() {
        int[] source = {0xFFFFFFFF, 0xFF000000, 0xFF000000};
        int[] result = HeroArt.boxBlur(source, 3, 1, 1, true);
        assertArrayEquals(new int[]{0xFFAAAAAA,0xFF555555,0xFF000000}, result);
        assertEquals(0xFFFFFFFF, source[0]);
    }
}
