package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.List;

/** Compact remote-friendly gallery; themes never change the Home app list. */
public final class ThemeGalleryActivity extends Activity {
    private ThemeSettings settings;
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        settings = new ThemeSettings(this);
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(56), dp(20), dp(56), dp(20));
        page.setBackgroundColor(0xFF080D16);
        scroll.addView(page); setContentView(scroll);
        page.addView(text("Theme Gallery", 28));
        List<ThemeCatalog.Theme> themes = ThemeCatalog.list(this);
        TextView note = text(themes.size() + " themes · Choose with your remote · Local artwork stays on this TV", 14);
        note.setTextColor(0xFFADB9CB); page.addView(note);
        GridLayout grid = new GridLayout(this); grid.setColumnCount(4);
        grid.setClipChildren(false); grid.setClipToPadding(false);
        page.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        View selected = null, first = null;
        int index = 0;
        for (ThemeCatalog.Theme theme : themes) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER);
            card.setPadding(dp(6), dp(5), dp(6), dp(4));
            card.setFocusable(true); card.setClickable(true); card.setContentDescription(theme.name);
            card.setBackground(plate(false));
            ImageView preview = new ImageView(this);
            preview.setImageDrawable(theme.id.equals("original") ? getDrawable(R.drawable.original_preview) : theme.background(this, dp(192), dp(66)));
            preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(preview, new LinearLayout.LayoutParams(-1, dp(66)));
            TextView label = text((settings.selected().equals(theme.id) ? "✓  " : "") + theme.name, 14);
            label.setGravity(Gravity.CENTER); label.setSingleLine(true);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);
            card.addView(label, new LinearLayout.LayoutParams(-1, dp(28)));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.columnSpec = GridLayout.spec(index % 4, 1f); lp.rowSpec = GridLayout.spec(index / 4);
            lp.width = 0; lp.height = dp(104); lp.setMargins(dp(4), dp(5), dp(4), dp(5));
            grid.addView(card, lp);
            card.setOnFocusChangeListener((view, focused) -> view.setBackground(plate(focused)));
            card.setOnHoverListener((view, event) -> {
                if (event.getActionMasked() == android.view.MotionEvent.ACTION_HOVER_ENTER) view.requestFocus();
                return false;
            });
            card.setOnClickListener(view -> { settings.select(theme.id); finish(); });
            if (first == null) first = card;
            if (settings.selected().equals(theme.id)) selected = card;
            index++;
        }
        LinearLayout toggles = new LinearLayout(this); toggles.setOrientation(LinearLayout.HORIZONTAL);
        page.addView(toggles, new LinearLayout.LayoutParams(-1, dp(56)));
        Switch motion = new Switch(this); motion.setText("Character motion / wake reveal");
        motion.setTextColor(Color.WHITE); motion.setChecked(settings.motion());
        motion.setOnCheckedChangeListener((view, checked) -> settings.motion(checked));
        toggles.addView(motion, new LinearLayout.LayoutParams(0, -1, 1));
        Switch sound = new Switch(this); sound.setText("Domain sound");
        sound.setTextColor(Color.WHITE); sound.setChecked(settings.sound());
        sound.setOnCheckedChangeListener((view, checked) -> settings.sound(checked));
        toggles.addView(sound, new LinearLayout.LayoutParams(0, -1, 1));
        View focus = selected != null ? selected : first;
        if (focus != null) focus.requestFocus();
    }
    private TextView text(String value, int size) {
        TextView result = new TextView(this); result.setText(value); result.setTextSize(size);
        result.setTextColor(Color.WHITE); result.setTypeface(UiFont.get(this, R.string.font_regular));
        return result;
    }
    private GradientDrawable plate(boolean focused) {
        GradientDrawable result = new GradientDrawable(); result.setCornerRadius(dp(10));
        result.setColor(focused ? 0x26FFFFFF : 0x10FFFFFF);
        result.setStroke(dp(focused ? 2 : 1), focused ? Color.WHITE : 0x28FFFFFF);
        return result;
    }
}
