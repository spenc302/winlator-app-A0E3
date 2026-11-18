#!/usr/bin/env bash
set -euo pipefail

# Build the same redistributable payload Winetricks installs into a 64-bit
# Wine prefix, then package its 64-bit and 32-bit DLLs as a Winlator component.
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install -y --no-install-recommends wine wine64 wine32:i386 winetricks cabextract xvfb zstd

export WINEPREFIX="$RUNNER_TEMP/winlator-vcrun2022-prefix"
export WINEARCH=win64
export WINEDEBUG=-all
mkdir -p "$WINEPREFIX"

xvfb-run -a winetricks -q vcrun2022

WINDOWS_DIR="$WINEPREFIX/drive_c/windows"
STAGE_DIR="$RUNNER_TEMP/winlator-vcrun2022-component"
ASSET="app/src/main/assets/wincomponents/vcrun2022.tzst"
mkdir -p "$STAGE_DIR/system32" "$STAGE_DIR/syswow64"

dlls=(
  concrt140.dll
  mfc140.dll mfc140chs.dll mfc140cht.dll mfc140deu.dll mfc140enu.dll
  mfc140esn.dll mfc140fra.dll mfc140ita.dll mfc140jpn.dll mfc140kor.dll
  mfc140rus.dll mfc140u.dll mfcm140.dll mfcm140u.dll
  msvcp140.dll msvcp140_1.dll msvcp140_2.dll
  msvcp140_atomic_wait.dll msvcp140_codecvt_ids.dll
  vcamp140.dll vccorlib140.dll vcomp140.dll
  vcruntime140.dll vcruntime140_1.dll
)

for arch_dir in system32 syswow64; do
  source_dir="$WINDOWS_DIR/$arch_dir"
  target_dir="$STAGE_DIR/$arch_dir"
  for dll in "${dlls[@]}"; do
    if [ -f "$source_dir/$dll" ]; then
      install -m 0644 "$source_dir/$dll" "$target_dir/$dll"
    fi
  done
done

# These are the core files 64-bit applications need. Fail the build rather than
# shipping a component that only contains an installer or incomplete payload.
for dll in vcruntime140.dll vcruntime140_1.dll msvcp140.dll; do
  test -s "$STAGE_DIR/system32/$dll"
done
test -s "$STAGE_DIR/syswow64/vcruntime140.dll"
test -s "$STAGE_DIR/syswow64/msvcp140.dll"

mkdir -p "$(dirname "$ASSET")"
tar --zstd -cf "$ASSET" -C "$STAGE_DIR" .
test -s "$ASSET"
echo "Generated VC++ 2015-2022 Winlator component: $(du -h "$ASSET" | cut -f1)"
