package com.gathin.tvhome;

/** Screen-inset bounds shared by the bottom pill's upward menus. */
final class ControlBarGeometry {
    static int popupLeft(int anchorLeft, int anchorRight, int width, int screenWidth, int inset, boolean alignEnd) {
        return Math.max(inset, Math.min(alignEnd ? anchorRight - width : anchorLeft, screenWidth - inset - width));
    }
    static int popupTop(int anchorTop, int height, int gap, int inset) {
        return Math.max(inset, anchorTop - gap - height);
    }
}
