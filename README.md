# A0E3

A game-focused Android launcher built on the Winlator 11.2 application and runtime.

The initial target is **Age of Empires III: Definitive Edition** on compatible Android devices. A0E3 reuses Winlator's existing Wine, Box64, graphics-driver, container, and input systems rather than introducing a separate Windows compatibility runtime.

## Project status

- Winlator 11.2 application source is the runtime foundation.
- The A0E3 game-library home screen is integrated into the Android app.
- Containers, graphics settings, and input controls route to existing Winlator screens.
- Game-folder selection currently stores a user-selected folder URI. A complete import pipeline, executable validation, and tested launch profile are still pending.
- No proprietary game files are included or downloaded by this project.
- Age of Empires III: Definitive Edition compatibility and performance have not yet been verified on-device.

## Build

The Android Gradle project is in `app/`. See the upstream Winlator build documentation and preserve all third-party license notices when modifying or redistributing this source.

## Runtime acknowledgements

A0E3 builds on Winlator and its upstream components. See the source history and upstream project for the full credits and third-party license information:
https://github.com/brunodev85/winlator
