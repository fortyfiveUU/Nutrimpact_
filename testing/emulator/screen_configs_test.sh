#!/bin/zsh
# Module: EEE521 - Final Year Project
# Project: Nutrimpact
# File: screen_configs_test.sh
# Description: Runs the instrumented (Espresso) test suite under different screen
# sizes and text sizes by temporarily changing the emulator's display settings,
# and takes screenshots of the diary and search screens in each configuration.
# The emulator's display is reset to normal at the end.
#
# Requirements: an emulator connected (adb devices). Takes around 15 minutes.

ADB=~/Library/Android/sdk/platform-tools/adb
ROOT=${0:A:h}/../..
OUT=${0:A:h}/output/screens; mkdir -p $OUT
PKG=com.example.nutrimpact
RESULTS=$ROOT/app/build/outputs/androidTest-results/connected/debug
# Gradle 8.2 needs JDK 17 (newer JDKs fail to start it).
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo $HOME/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home)

reset_display() { $ADB shell wm size reset; $ADB shell wm density reset; $ADB shell settings put system font_scale 1.0; }

# name | size | density | font scale
configs=(
  "small-phone|720x1280|320|1.0"
  "tablet|1600x2560|320|1.0"
  "large-text|reset|reset|1.3"
)

for config in $configs; do
  IFS='|' read -r name size density font <<< "$config"
  reset_display
  [[ $size != reset ]] && $ADB shell wm size $size
  [[ $density != reset ]] && $ADB shell wm density $density
  $ADB shell settings put system font_scale $font
  sleep 3
  echo "######## $name (size $size, density $density, font x$font)"

  rm -rf $RESULTS   # make sure old results can't be mistaken for this run's
  (cd $ROOT && ./gradlew connectedDebugAndroidTest -q >/dev/null 2>&1) || echo "  (Gradle reported failing tests or a build error)"
  [[ -d $RESULTS ]] || echo "  NO RESULTS: the tests did not run"
  python3 -I - $RESULTS <<'EOF'
import glob, sys, xml.etree.ElementTree as ET
for f in glob.glob(sys.argv[1] + "/*.xml"):
    r = ET.parse(f).getroot()
    print("  instrumented tests:", r.get("tests"), "run,", r.get("failures"), "failures,", r.get("errors"), "errors")
    for tc in r.iter("testcase"):
        if tc.find("failure") is not None:
            print("  FAIL", tc.get("classname").split(".")[-1] + "." + tc.get("name"))
EOF

  # The test runner uninstalls the app afterwards, so reinstall it for the screenshots.
  $ADB install -r $ROOT/app/build/outputs/apk/debug/app-debug.apk >/dev/null
  # Screenshots: the (empty) diary and the search screen.
  $ADB shell am force-stop $PKG
  $ADB shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 4
  $ADB exec-out screencap -p > $OUT/$name-diary.png
  b=(${=$($ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; $ADB shell cat /sdcard/ui.xml | tr '>' '\n' | grep 'id/fabAddFood' | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' ')})
  if [[ ${#b} -ge 4 ]]; then
    $ADB shell input tap $(( (b[1]+b[3])/2 )) $(( (b[2]+b[4])/2 )); sleep 3
    $ADB exec-out screencap -p > $OUT/$name-search.png
  fi
done

reset_display
echo "Screenshots saved in $OUT"
