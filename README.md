# A0E3

A game-focused Android launcher built on the Winlator 11.2 application and runtime.

The initial target is **Age of Empires III: Definitive Edition** on compatible Android devices. A0E3 reuses Winlator's existing Wine, Box64, graphics-driver, container, and input systems rather than introducing a separate Windows compatibility runtime.

## Project status

- Winlator 11.2 application source is the runtime foundation.
- The A0E3 game-library home screen is integrated into the Android app.
- Containers, graphics settings, and input controls route to existing Winlator screens.
- The user can select an existing game folder through Android's Storage Access Framework.
- The importer copies the selected folder into a chosen Winlator container and scans Windows executables for 64-bit PE architecture.
- The detected executable is passed to Winlator's existing launch activity.
- This is an initial import/launch path. It does not yet verify all game dependencies, Steam authentication, graphics compatibility, or successful gameplay.
- No proprietary game files are included or downloaded by this project.
- Age of Empires III: Definitive Edition compatibility and performance have not yet been verified on-device.

## Build

The Android Gradle project is in `app/`. See the upstream Winlator build documentation and preserve all third-party license notices when modifying or redistributing this source.

## Runtime acknowledgements

A0E3 builds on Winlator and its upstream components. See the upstream project for full credits and third-party license information:
https://github.com/brunodev85/winlator
