# Octo86 integration status

This repository is the working Android application for Octo86, based on the Winlator 11.2 app source.

## Current integration

- The existing Winlator application and native runtime remain the runtime foundation.
- The Octo86 home screen is implemented in `app/src/main/java/com/winlator/Aoe3HomeFragment.java`.
- `MainActivity` opens the Octo86 home screen by default and routes Containers, Graphics and Controls to the existing Winlator screens.
- The app display name is Octo86.
- The game-folder picker requests a Storage Access Framework directory and persists its URI permission.

## Important current limitation

Selecting a folder currently saves its document-tree URI. It does **not** yet copy the game into a Winlator container, detect the actual AoE3 DE executable, create a launch shortcut, or prove compatibility. The launcher must remain unavailable until those steps are implemented and validated.

No Age of Empires III game files are included. The user must supply a legally obtained game installation.

## Next implementation steps

1. Build a SAF-aware import service with progress, cancellation, free-space checks and safe path handling.
2. Select or create the target Winlator container and copy the user's game files into it.
3. Inspect the imported executable and dependencies instead of assuming a fixed executable name.
4. Add an AoE3 DE profile using existing Winlator Wine, Box64, DXVK and graphics-driver controls.
5. Create a shortcut through Winlator's existing shortcut and launch path.
6. Test on the REDMAGIC 8S Pro (Snapdragon 8 Gen 2 / Adreno 740). Treat all compatibility and performance expectations as unverified until tested.

## Build

The manual GitHub Actions workflow is `.github/workflows/octo86-android-debug.yml`. It is intentionally manual; do not run it until the UI and integration work is ready for a first APK test.
