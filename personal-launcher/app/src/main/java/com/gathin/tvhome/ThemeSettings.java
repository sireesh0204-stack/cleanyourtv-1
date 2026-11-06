package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

/** Gallery choices share Home's revision without touching its application order. */
final class ThemeSettings {
    static final String THEME = "character_theme", SOUND = "character_sound", MOTION = "character_motion";
    private final SharedPreferences prefs;
    ThemeSettings(Context context) { prefs = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE); }
    String selected() { return prefs.getString(THEME, "gojo"); }
    boolean sound() { return prefs.getBoolean(SOUND, false); }
    boolean motion() { return prefs.getBoolean(MOTION, true); }
    static boolean enabled(Context context) {
        return new ThemeSettings(context).prefs.getBoolean("character_enabled", context.getResources().getBoolean(R.bool.itachi_theme));
    }
    void select(String id) { change(prefs.edit().putString(THEME, id).putBoolean("character_enabled", true)); }
    void sound(boolean value) { change(prefs.edit().putBoolean(SOUND, value)); }
    void motion(boolean value) { change(prefs.edit().putBoolean(MOTION, value)); }
    private void change(SharedPreferences.Editor editor) {
        editor.putLong(Config.SETTINGS_REVISION_KEY, prefs.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).apply();
    }
}
