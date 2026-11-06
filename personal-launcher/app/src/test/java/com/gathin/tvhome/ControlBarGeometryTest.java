package com.gathin.tvhome;

import org.junit.Test;
import static org.junit.Assert.*;

public class ControlBarGeometryTest {
    @Test public void menuLiftsAboveItsAnchor() {
        assertEquals(326, ControlBarGeometry.popupTop(936,590,20,32));
        assertEquals(32, ControlBarGeometry.popupTop(200,590,20,32));
    }
    @Test public void leftAndRightEdgesStayInsideScreen() {
        assertEquals(32, ControlBarGeometry.popupLeft(40,128,580,1920,32,true));
        assertEquals(1308, ControlBarGeometry.popupLeft(1800,1888,580,1920,32,false));
        assertEquals(700, ControlBarGeometry.popupLeft(1192,1280,580,1920,32,true));
    }
}
