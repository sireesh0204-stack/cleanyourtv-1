package com.gathin.tvhome;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public final class ControlCenterActivity extends Activity {

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (getWindow() != null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.dimAmount = 0.75f;
            getWindow().setAttributes(lp);
        }
        setContentView(R.layout.activity_control_center);

        findViewById(R.id.scrim).setOnClickListener(v -> finish());
        LinearLayout rows = findViewById(R.id.rows);

        TvInputManager inputs = (TvInputManager) getSystemService(TV_INPUT_SERVICE);
        if (inputs != null) {
            for (TvInputInfo input : inputs.getTvInputList()) {
                if (!input.isPassthroughInput() || input.isHidden(this)) continue;
                String label = InputNames.tidy(input.loadCustomLabel(this));
                if (label == null) label = InputNames.tidy(input.loadLabel(this));
                if (label == null) label = getString(R.string.inputs);
                addRow(rows, R.drawable.ic_hdmi_input, label, getString(R.string.inputs),
                        () -> openInput(input.getId()));
            }
        }
        addRow(rows, R.drawable.ic_settings, "JioOS Settings", "System",
                () -> openSettings("com.rjil.jiostbsetting"));
        addRow(rows, R.drawable.ic_settings, "Android Settings", "System",
                () -> openSettings("com.android.tv.settings"));
        addRow(rows, R.drawable.ic_apps, "Theme Gallery", "Home screen",
                () -> startActivity(new Intent(this, ThemeGalleryActivity.class)));
        addRow(rows, R.drawable.ic_ram_chip, "Clear Memory", "Clean RAM", this::clearMemory);

        if (rows.getChildCount() > 0) {
            rows.getChildAt(0).requestFocus();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void openSettings(String pkg) {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS).setPackage(pkg));
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, R.string.not_available, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
        overridePendingTransition(0, 0);
    }

    private void openInput(String inputId) {
        Intent view = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(TvContract.buildChannelUriForPassthroughInput(inputId),
                        "vnd.android.cursor.item/channel")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(view);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, "Input not available", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void clearMemory() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        long beforeMb = 0;
        long afterMb = 0;
        if (am != null) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            beforeMb = mi.availMem / (1024 * 1024);

            List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
            if (procs != null) {
                for (ActivityManager.RunningAppProcessInfo proc : procs) {
                    if (!proc.processName.equals(getPackageName())
                            && !proc.processName.contains("system")
                            && !proc.processName.contains("inputmethod")) {
                        if (proc.pkgList != null) {
                            for (String pkg : proc.pkgList) am.killBackgroundProcesses(pkg);
                        } else {
                            am.killBackgroundProcesses(proc.processName);
                        }
                    }
                }
            }
            System.gc();
            am.getMemoryInfo(mi);
            afterMb = mi.availMem / (1024 * 1024);
        }
        long freed = afterMb - beforeMb;
        if (freed > 0) {
            Toast.makeText(this, "Memory Cleared (" + freed + " MB freed)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Memory Cleared", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void addRow(LinearLayout rows, int icon, String title, String status, Runnable action) {
        View row = getLayoutInflater().inflate(R.layout.item_control_center, rows, false);
        ((ImageView) row.findViewById(R.id.icon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.title)).setText(title);
        TextView hint = row.findViewById(R.id.status);
        if (status != null && !status.isEmpty()) {
            hint.setText(status);
        } else {
            hint.setVisibility(View.GONE);
        }
        row.setOnClickListener(v -> action.run());
        rows.addView(row);
    }
}
