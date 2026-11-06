# Updating and restoring

This module uses the existing `com.gathin.tvhome` application ID. Keep the original signing key. A different developer's debug key cannot update an existing installation.

Before installation, keep a private copy of the installed APK, affected source, exported launcher configuration and local `themes/` directory. Backups and private theme assets do not belong in Git.

Build from this directory with JDK 17 and Android SDK 34:

```powershell
./gradlew.bat --no-daemon -PcharacterThemeEnabled=true lintRelease assembleRelease
```

Use an update installation (`adb install -r app/build/outputs/apk/release/app-release.apk`) on the intended device. Do not uninstall the launcher or clear its storage. Theme choices use the existing settings revision and do not replace app order or hidden-app preferences.

To return to a previous look, select it in Launcher Settings → Theme Gallery. The private local artwork folders must still exist. To restore older launcher code, build the backed-up source with a version code at least as high as the installed version, sign it with the original key and install it as an update. Android may reject a direct APK downgrade even with `-d`; do not uninstall to work around that if preserving data matters.

If Home cannot open, enable the original launcher and select it as default Home before changing the custom installation. Use the model-specific backup's recorded original Home component; do not copy another TV's package list. This launcher change requires no firmware flash or root.
