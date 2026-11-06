package com.gathin.tvhome;

/** Presentation is selected without rewriting Home's application preferences. */
enum CharacterStyle {
    NONE(0xFFBFCAD8, 0xFFFFFFFF),
    GOJO(0xFF74DCFF, 0xFFC0A5FF),
    SUKUNA(0xFFE32C4D, 0xFF84263A),
    ZENITSU(0xFFFFCF55, 0xFFB1E9FF),
    SAITAMA(0xFFFFC467, 0xFFFFF0CA),
    ITACHI(0xFFE84258, 0xFFB9C8E8),
    BATMAN(0xFFCDD8E4, 0xFF818EA4),
    HOGWARTS(0xFFF4D18B, 0xFF9BD9E3),
    DOOM(0xFF73DD9E, 0xFFB0EDCB);

    final int primary, secondary;
    CharacterStyle(int primary, int secondary) { this.primary = primary; this.secondary = secondary; }
    static CharacterStyle forTheme(String id) {
        if (id == null) return NONE;
        switch (id) {
            case "gojo": return GOJO;
            case "sukuna": return SUKUNA;
            case "local:zenitsu": return ZENITSU;
            case "local:saitama": return SAITAMA;
            case "local:itachi": return ITACHI;
            case "local:batman": return BATMAN;
            case "local:hogwarts": case "local:greenhouse": case "local:library": return HOGWARTS;
            case "local:doom": case "local:doom-color": return DOOM;
            default: return NONE;
        }
    }
    static float phase(long now, long start, long duration) {
        return Math.max(0f, Math.min(1f, (now - start) / (float) duration));
    }
}
