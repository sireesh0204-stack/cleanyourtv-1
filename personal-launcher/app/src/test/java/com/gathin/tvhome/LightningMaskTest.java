package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class LightningMaskTest {
    @Test public void keepsWarmBranchesAndWhiteCoresOutsideCharacter() {
        assertTrue(LightningMask.alpha(0xFFFFC040, .3f, .4f) > 0);
        assertEquals(255, LightningMask.alpha(0xFFFFF8EF, .3f, .4f));
    }
    @Test public void excludesCharacterEvenWhenClothesOrHairAreGold() {
        assertEquals(0, LightningMask.alpha(0xFFFFC040, .60f, .20f));
        assertEquals(0, LightningMask.alpha(0xFFFFF8EF, .70f, .50f));
    }
    @Test public void ignoresDarkForestBlueAndTransparentPixels() {
        assertEquals(0, LightningMask.alpha(0xFF68502A, .3f, .4f));
        assertEquals(0, LightningMask.alpha(0xFF63CFFF, .3f, .4f));
        assertEquals(0, LightningMask.alpha(0x00FFC040, .3f, .4f));
        assertEquals(0, LightningMask.alpha(0xFFFFC040, .98f, .8f));
    }
}
