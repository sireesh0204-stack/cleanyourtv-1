package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class ThemeCatalogTest {
    @Test public void largeLocalWallpaperIsSampledBeforeDecode() {
        assertEquals(4, ThemeCatalog.sampleSize(12000, 8000, 1920, 1080));
        assertEquals(1, ThemeCatalog.sampleSize(1920, 1080, 1920, 1080));
    }
    @Test public void eyeRegistrationUsesAllFourSukunaEyes() {
        assertEquals(4, DomainView.eyeCoordinates("sukuna").length);
        assertEquals(2, DomainView.eyeCoordinates("gojo").length);
        assertEquals(1f, DomainView.blink(1000), 0);
        assertEquals(0f, DomainView.blink(80), 0);
    }
}
