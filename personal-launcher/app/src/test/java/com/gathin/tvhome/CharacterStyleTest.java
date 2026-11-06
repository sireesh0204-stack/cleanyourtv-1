package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterStyleTest {
    @Test public void installedLocalSkinsUseTheirOwnEffects() {
        assertEquals(CharacterStyle.ZENITSU, CharacterStyle.forTheme("local:zenitsu"));
        assertEquals(CharacterStyle.SAITAMA, CharacterStyle.forTheme("local:saitama"));
        assertEquals(CharacterStyle.ITACHI, CharacterStyle.forTheme("local:itachi"));
        assertEquals(CharacterStyle.BATMAN, CharacterStyle.forTheme("local:batman"));
        for(String name:new String[]{"hogwarts","library","greenhouse"})
            assertEquals(CharacterStyle.HOGWARTS,CharacterStyle.forTheme("local:"+name));
        for(String name:new String[]{"doom","doom-color"})
            assertEquals(CharacterStyle.DOOM,CharacterStyle.forTheme("local:"+name));
        assertEquals(CharacterStyle.NONE,CharacterStyle.forTheme("original"));
        assertEquals(CharacterStyle.NONE,CharacterStyle.forTheme("local:unknown"));
        assertEquals(CharacterStyle.NONE,CharacterStyle.forTheme(null));
    }
    @Test public void interruptedFocusAndRevealStayBounded() {
        assertEquals(0,CharacterStyle.phase(100,200,220),0);
        assertEquals(.5f,CharacterStyle.phase(310,200,220),0);
        assertEquals(1,CharacterStyle.phase(5000,200,1200),0);
        assertEquals(0,CharacterStyle.phase(5000,5000,220),0);
    }
    @Test public void goldAndBlueWhiteLightningRemainDistinct() {
        assertNotEquals(CharacterStyle.ZENITSU.primary,CharacterStyle.ZENITSU.secondary);
        assertEquals(0,DomainView.eyeCoordinates("original").length);
        assertEquals(0,DomainView.eyeCoordinates("local:zenitsu").length);
    }
}
