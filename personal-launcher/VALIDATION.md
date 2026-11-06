# Device validation

Tracking: THEME-001 / THEME-003 / THEME-004 / THEME-005 / THEME-006. The gallery must keep imported themes discoverable and animated while preserving the existing Home layout and preferences.

Tested on Jio JHSD200, Android 11 / API 30, firmware STB-JHSD200-11.3.46. The separate JHSC200 installation and source were not modified.

- The installed version 3.9 gallery lists 12 choices in four columns, including Original. Gojo and Sukuna artwork is bundled; the other nine artwork skins stay private. Native effects cover eight families, including Itachi, Batman, Hogwarts, Doom, Zenitsu and Saitama. Settled frame pairs and private device recordings confirm motion outside the clock region. Original shows card/banner tiles and a plain dark backdrop; switching back restores the personal presentation and visible app order.
- Both built-in idle presentations ran for 66 seconds. Waking restored the Home clock, date, seconds, icons and footer controls without the old frosted clock shader.
- All Apps uses a frozen blurred Home backdrop and a white outlined focus target. Home retains colored glow, white idle icons, 25% focused tint and the sliding label. Alder's play cutout remains transparent.
- The Settings menu opens both JioOS Settings and Android Settings. JioTV+, ETV Win and Jio account/SSO components remain enabled. Content playback and account entitlements were not re-tested as part of the visual checks.
- Physical Home was confirmed by the owner earlier in this setup. Subsequent navigation used injected D-pad/Home input; a new physical Menu-key check remains unverified.
- Gfxinfo during the Sukuna idle/wake run: 3,058 rendered frames, 0.56% janky, median 5 ms, 95th percentile 7 ms, 99th percentile 10 ms. Process PSS was approximately 232 MiB. Gojo's earlier run: 0.26% janky, median 6 ms, 95th percentile 8 ms, approximately 231 MiB PSS. These are observed render costs, not an A/B performance improvement claim. Character and idle callbacks target a 33 ms interval in the final source.
- Sound is synthesized locally and defaults off; the device was left with sound off. Audio perception, every blink frame, physical screen-power wake and a full reboot remain separate manual checks.

- The expanded build also passed a 66-second Zenitsu idle/wake check. Gfxinfo: 3,155 rendered frames, 0.22% janky, median 6 ms, 95th percentile 10 ms and 99th percentile 13 ms; PSS approximately 234 MiB. This is measured render cost, not a frame-rate or A/B improvement claim.
- With character motion off, two settled Zenitsu captures had zero changed samples in the artwork region outside the clock and controls (every fourth pixel sampled, RGB difference threshold 5). Motion was restored afterward. Final All Apps still shows the frozen frost and white focus border. All measured results come from the actual device, not a mockup.

The 51 focused style, lifecycle, catalog, blur, library, order and motion tests pass. Release assembly succeeds; lint reports zero errors and 144 warnings. Running all 259 Java tests reports one inherited failure, `TileFitTest.oneRowUnderATitledDockFits`; its assertions and implementation were not weakened.

The separate upstream static-theme contribution passes its 208 theme tests (61 skipped) and 24 README-count tests. The complete upstream Python run reports 2,935 tests, 101 failures, two errors and 433 skips on this Windows host. Failures include unchanged PowerShell subprocess tests (`StandardOutputEncoding is only supported when standard output is redirected`) and bash/WSL invocation errors. No tools, safety checks or tests were modified to hide them.

An independent two-round source review found and fixed duplicate frame callbacks and a Hebrew Settings-view cast. The second review approved the revised code.

A further two-round review of the expanded effects found and fixed decoration invalidation for white accents and a UTF-8 text regression. It approved the final effect code. Device review additionally removed hard edges from fog and aura rendering.


The footer revision uses a centered capsule with equal 44 dp targets and 8 dp gaps. Actual remote-style D-pad input confirms Settings opens upward, Down returns to Settings, Up reopens the panel and Back closes it. The Home selector is now a restrained breathing glow without sketch strokes. Original and reduced-motion presentation remain static.

The owner rejected the initial brightness/color-only lightning effect as visually insufficient. Zenitsu now separates actual bolt/halo pixels and a cached repaired forest layer, then independently shifts and rotates cropped connected branches. The first full-screen mesh prototype showed edge artifacts and a p95 of 38 ms, so it was replaced rather than shipped as the final renderer. A cached original character cutout is drawn last to keep the figure still. New tests cover preservation of original bolt pixels, removal of the static core beneath them, protected character pixels, visible branch displacement and continuity across the two-minute mark. The native recording is checked for position changes rather than brightness differences. Private wallpaper, extracted layers and device recordings remain excluded from the repository.

The stock Jio screensaver packages were reversibly disabled on the owner's device and Android dream activation was turned off. After more than six minutes without input, the launcher remained foreground and neither Jio screensaver returned. This is a private device configuration, not a change to public debloat lists; the launcher's own idle presentation remains available.

The previous brightness-only 3.8 build’s settled 18-second sample without screen recording rendered 1,082 frames: zero janky frames, median 6 ms, p95 7 ms and p99 9 ms; total process PSS was approximately 136 MiB. The earlier capture session was slower while Android screen recording was active; these figures are reported separately rather than attributing recorder overhead to normal Home playback.


For THEME-006, the final renderer animates eight main connected branches; tiny fragments stay in the static layer. The final device recording shows the main branch centroid spanning approximately 39 horizontal and 16 vertical pixels at 720p. This measures actual position changes, not brightness. The cached character cutout removes the costly per-frame path clip. Final settled sample: 1,012 rendered frames, median 17 ms, p95/p99 18 ms, no slow UI-thread frames or missed frame deadlines, total process PSS approximately 178 MiB. Android's 60 Hz jank counter was 97.83%; this is not a claim of locked 60 fps. The sample stays within the requested approximately 30 fps render budget. Earlier mesh and many-fragment prototypes were slower and are not the final implementation.

The final 51 focused tests, release build and lint pass (zero errors, 144 existing warnings). The full 259-test run has 258 passes and the same inherited TileFitTest failure. Independent review approved branch extraction, static-fragment restoration, continuous timing, cached character compositing and unpublished-texture cleanup. Actual source and installed data were backed up before this revision; the separate Itachi box remains untouched.
