#!/bin/zsh
# Module: EEE521 - Final Year Project
# Project: Nutrimpact
# File: 01_search_test.sh
# Description: Sprint 3 - food search screen: input validation, results, no-results, offline, rapid searches, rotation, dark mode.
#
# End-to-end check script for a running Android emulator. It drives the app with
# adb (taps, typing, swipes, rotation) and reads the screen with uiautomator,
# printing PASS/FAIL for each check and a total at the end.
#
# Requirements: one emulator or device connected (adb devices), internet access,
# and a debug build: ./gradlew assembleDebug. Run from anywhere: zsh 01_search_test.sh
# Screenshots and database copies are written to testing/emulator/output/.
# Note: some checks expect particular Open Food Facts products (e.g. "Oats so simple")
# to appear in search results, so they can fail if that live data changes.
ADB=~/Library/Android/sdk/platform-tools/adb
S=${0:A:h}/output; mkdir -p $S
PKG=com.example.nutrimpact
PASS=0; FAIL=0

ui() { $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; $ADB shell cat /sdcard/ui.xml; }
has() { ui | grep -q -- "$1"; }
check() { # name, expected text, [negate]
  if [[ "$3" == "not" ]]; then
    if has "$2"; then echo "FAIL  $1 (found: $2)"; FAIL=$((FAIL+1)); else echo "PASS  $1"; PASS=$((PASS+1)); fi
  else
    if has "$2"; then echo "PASS  $1"; PASS=$((PASS+1)); else echo "FAIL  $1 (missing: $2)"; FAIL=$((FAIL+1)); fi
  fi
}
count_cards() { ui | grep -o 'resource-id="com.example.nutrimpact:id/textProductName"' | wc -l | tr -d ' '; }
clear_field() { $ADB shell input tap 400 452; sleep 0.5; $ADB shell input keyevent KEYCODE_MOVE_END; for i in $(seq 1 12); do $ADB shell input keyevent KEYCODE_DEL; done; }
type_q() { clear_field; [[ -n "$1" ]] && $ADB shell input text "$1"; }
tap_glass() { $ADB shell input tap 969 452; }
wait_done() { for i in $(seq 1 20); do sleep 1; ui | grep -q 'id/progressSearch' || return 0; done; }

$ADB shell settings put system accelerometer_rotation 0; $ADB shell settings put system user_rotation 0
$ADB shell cmd uimode night no >/dev/null
$ADB install -r ${0:A:h}/../../app/build/outputs/apk/debug/app-debug.apk >/dev/null
$ADB shell am start -S -n $PKG/.DiaryActivity >/dev/null; sleep 4
fab=(${=$($ADB shell uiautomator dump /sdcard/ui.xml >/dev/null; $ADB shell cat /sdcard/ui.xml | tr ">" "\n" | grep 'text="Add food"' | head -1 | grep -o 'bounds="[^"]*"' | grep -o "[0-9]*" | tr "\n" " ")})
$ADB shell input tap $(( (fab[1]+fab[3])/2 )) $(( (fab[2]+fab[4])/2 )); sleep 3; $ADB logcat -c

echo "== Input and validation"
check "Start message shown on launch" "Your search results will appear here."
check "Prompt text shown" "then tap the magnifying glass"
tap_glass; sleep 1
check "Empty search shows error" "Please enter a food or brand to search"
check "Magnifying glass still present with error" 'content-desc="Search"'
type_q "%s%s"; tap_glass; sleep 1
check "Spaces-only search shows error" "Please enter a food or brand to search"
$ADB shell input text "o"; sleep 1
check "Typing clears the error" "Please enter a food or brand to search" not

echo "== Results"
$ADB emu network delay gprs >/dev/null; $ADB emu network speed gsm >/dev/null
type_q "oats"; tap_glass; sleep 0.5
check "Spinner shows while loading (slow network)" "id/progressSearch"
$ADB emu network delay none >/dev/null; $ADB emu network speed full >/dev/null
wait_done
check "Spinner hidden after loading" "id/progressSearch" not
n=$(count_cards); [[ $n -ge 3 ]] && { echo "PASS  Oats returns result cards ($n on screen)"; PASS=$((PASS+1)); } || { echo "FAIL  Oats result cards ($n)"; FAIL=$((FAIL+1)); }
check "Card shows kcal per 100g" "kcal per 100g"
check "Card shows macros" "Protein .*g · Carbs"
check "Brands joined with comma" "Quaker, Quaker Oats"
check "Start message hidden with results" "Your search results will appear here." not
$ADB exec-out screencap -p > $S/v_results.png

echo "== Keyboard search key"
type_q "heinz"; $ADB shell input keyevent KEYCODE_ENTER; wait_done; sleep 1
check "Hardware Enter key runs search" "No nutrition data available"
type_q "milk"; $ADB shell input tap 99 1117; wait_done; sleep 1
check "On-screen keyboard search key runs search" '[Mm]ilk'

echo "== No-nutrition products"
type_q "heinz"; tap_glass; wait_done; sleep 1
check "Missing nutrition shows message, not 0 kcal" "No nutrition data available"
check "No '0 kcal' for missing data" '"0 kcal per 100g"' not

echo "== No results"
type_q "qzxvkwpj"; tap_glass; wait_done; sleep 1
check "No-results message names the query" 'No results found for "qzxvkwpj"'
[[ $(count_cards) -eq 0 ]] && { echo "PASS  No cards shown for no results"; PASS=$((PASS+1)); } || { echo "FAIL  Cards shown for no results"; FAIL=$((FAIL+1)); }

echo "== Offline and recovery"
$ADB shell cmd connectivity airplane-mode enable; sleep 3
type_q "oats"; tap_glass; wait_done; sleep 1
check "Offline shows connection message" "check your internet connection"
$ADB shell cmd connectivity airplane-mode disable; sleep 6
tap_glass; wait_done; sleep 1
check "Search works again once back online" "kcal per 100g"

echo "== Rapid searches (latest wins)"
type_q "heinz"; tap_glass; sleep 0.2; type_q "oats"; tap_glass; wait_done; sleep 2
check "Latest search's results shown" "Quaker, Quaker Oats"
check "Earlier search didn't overwrite" "No nutrition data available" not

echo "== Rotation"
$ADB shell input swipe 540 1800 540 1100 400; sleep 1
$ADB shell settings put system user_rotation 1; sleep 3
check "Results kept in landscape" "kcal per 100g"
check "Search text kept in landscape" 'text="oats"'
$ADB exec-out screencap -p > $S/v_landscape.png
$ADB shell settings put system user_rotation 0; sleep 3
check "Results kept back in portrait" "kcal per 100g"
type_q "heinz"; tap_glass; $ADB shell settings put system user_rotation 1; wait_done; sleep 2
check "Search finishes after rotating mid-load" "No nutrition data available"
$ADB shell settings put system user_rotation 0; sleep 3
type_q "qzxvkwpj"; tap_glass; wait_done; $ADB shell settings put system user_rotation 1; sleep 3
check "No-results message kept after rotation" "No results found"
$ADB shell settings put system user_rotation 0; sleep 3

echo "== Dark mode"
$ADB shell cmd uimode night yes >/dev/null; sleep 3
type_q "oats"; tap_glass; wait_done; sleep 1
check "Results shown in dark mode" "kcal per 100g"
$ADB exec-out screencap -p > $S/v_dark.png
$ADB shell cmd uimode night no >/dev/null

echo "== Crashes"
if $ADB logcat -d -b crash | grep -q "$PKG"; then echo "FAIL  App crashed"; FAIL=$((FAIL+1)); $ADB logcat -d -b crash | tail -20; else echo "PASS  No crashes during the run"; PASS=$((PASS+1)); fi

$ADB shell settings put system accelerometer_rotation 1
echo "== $PASS passed, $FAIL failed"
