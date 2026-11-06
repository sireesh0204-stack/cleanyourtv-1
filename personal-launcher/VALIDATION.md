# Device validation

Tracking: THEME-001. The gallery must keep imported themes discoverable while preserving the existing Home layout and preferences.

Tested on Jio JHSD200, Android 11 / API 30, firmware STB-JHSD200-11.3.46. The separate JHSC200 installation and source were not modified.

- The installed gallery lists 11 choices in four columns. Only Gojo and Sukuna are bundled; the other nine are private local artwork skins. Local skins do not include animated character overlays.
- Both built-in idle presentations ran for 66 seconds. Waking restored the Home clock, date, seconds, icons and top controls without the old frosted clock shader.
- All Apps uses a frozen blurred Home backdrop and a white outlined focus target. Home retains colored glow, white idle icons, 25% focused tint and the sliding label. Alder's play cutout remains transparent.
- The Settings menu opens both JioOS Settings and Android Settings. JioTV+, ETV Win and Jio account/SSO components remain enabled. Content playback and account entitlements were not re-tested as part of the visual checks.
- Physical Home was confirmed by the owner earlier in this setup. Subsequent navigation used injected D-pad/Home input; a new physical Menu-key check remains unverified.
- Gfxinfo during the Sukuna idle/wake run: 3,058 rendered frames, 0.56% janky, median 5 ms, 95th percentile 7 ms, 99th percentile 10 ms. Process PSS was approximately 232 MiB. Gojo's earlier run: 0.26% janky, median 6 ms, 95th percentile 8 ms, approximately 231 MiB PSS. These are observed render costs, not an A/B performance improvement claim. Character and idle callbacks target a 33 ms interval in the final source.
- Sound is synthesized locally and defaults off; the device was left with sound off. Audio perception, every blink frame, physical screen-power wake and a full reboot remain separate manual checks.

The 39 focused lifecycle, catalog, blur, library, order and motion tests pass. Release assembly succeeds; lint reports zero errors and 145 inherited warnings. Running all 247 Java tests reports one inherited failure, `TileFitTest.oneRowUnderATitledDockFits`; its assertions and implementation were not weakened.

The separate upstream static-theme contribution passes its 208 theme tests (61 skipped) and 24 README-count tests. The complete upstream Python run reports 2,935 tests, 101 failures, two errors and 433 skips on this Windows host. Failures include unchanged PowerShell subprocess tests (`StandardOutputEncoding is only supported when standard output is redirected`) and bash/WSL invocation errors. No tools, safety checks or tests were modified to hide them.

An independent two-round source review found and fixed duplicate frame callbacks and a Hebrew Settings-view cast. The second review approved the revised code.
