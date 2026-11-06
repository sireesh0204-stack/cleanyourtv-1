package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import org.json.JSONObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Offline catalog. Private artwork stays in app-owned external storage. */
final class ThemeCatalog {
    static final class Theme {
        final String id, name;
        final int accent;
        final File wallpaper;
        Theme(String id, String name, int accent, File wallpaper) {
            this.id = id; this.name = name; this.accent = accent; this.wallpaper = wallpaper;
        }
        boolean builtin() { return wallpaper == null; }
        Drawable background(Context context) {
            return background(context, 1920, 1080);
        }
        Drawable background(Context context, int width, int height) {
            if (wallpaper != null) {
                Bitmap bitmap = decode(wallpaper, width, height);
                if (bitmap != null) return new BitmapDrawable(context.getResources(), bitmap);
            }
            return context.getDrawable(id.equals("sukuna") ? R.drawable.jjk_sukuna_background : R.drawable.jjk_gojo_background);
        }
        int character(Context context) {
            return builtin() ? (id.equals("sukuna") ? R.drawable.jjk_sukuna_character : R.drawable.jjk_gojo_character) : 0;
        }
    }
    static List<Theme> list(Context context) {
        List<Theme> themes = new ArrayList<>();
        themes.add(new Theme("gojo", "Gojo · Infinite Void", 0xFF74DCFF, null));
        themes.add(new Theme("sukuna", "Sukuna · Malevolent Shrine", 0xFFFF5266, null));
        File directory = context.getExternalFilesDir("themes");
        File[] folders = directory == null ? null : directory.listFiles(File::isDirectory);
        if (folders == null) return themes;
        Arrays.sort(folders);
        for (File folder : folders) {
            if (!folder.getName().matches("[a-zA-Z0-9_-]{1,64}")) continue;
            File metadata = new File(folder, "theme.json");
            try {
                if (metadata.length() > 16384) continue;
                JSONObject data = new JSONObject(new String(Files.readAllBytes(metadata.toPath()), StandardCharsets.UTF_8));
                String filename = data.optString("wallpaper", "wallpaper.jpg");
                File wallpaper = new File(folder, filename).getCanonicalFile();
                if (!wallpaper.getParentFile().equals(folder.getCanonicalFile()) || !wallpaper.isFile()) continue;
                BitmapFactory.Options bounds = new BitmapFactory.Options(); bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(wallpaper.getPath(), bounds);
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) continue;
                themes.add(new Theme("local:" + folder.getName(), data.optString("name", data.optString("title", folder.getName())), 0xFFBFCAD8, wallpaper));
            } catch (Exception ignored) { /* A malformed local folder cannot prevent Home opening. */ }
        }
        return themes;
    }
    static Theme selected(Context context) {
        String id = new ThemeSettings(context).selected();
        List<Theme> themes = list(context);
        for (Theme theme : themes) if (theme.id.equals(id)) return theme;
        return themes.get(0);
    }
    static Bitmap decode(File file, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options(); options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), options);
        options.inSampleSize = sampleSize(options.outWidth, options.outHeight, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }
    static int sampleSize(int width, int height, int targetWidth, int targetHeight) {
        int sample = 1;
        while (width / sample > targetWidth * 2 || height / sample > targetHeight * 2) sample *= 2;
        return sample;
    }
}
