#!/bin/zsh
# Module: EEE521 - Final Year Project
# Project: Nutrimpact
# File: 02_add_food_test.sh
# Description: Sprint 4 - Add Food screen: portion scaling, g/oz toggle, validation, rotation, saving to the Room database.
#
# End-to-end check script for a running Android emulator. It drives the app with
# adb (taps, typing, swipes, rotation) and reads the screen with uiautomator,
# printing PASS/FAIL for each check and a total at the end.
#
# Requirements: one emulator or device connected (adb devices), internet access,
# and a debug build: ./gradlew assembleDebug. Run from anywhere: zsh 02_add_food_test.sh
# Screenshots and database copies are written to testing/emulator/output/.
# Note: some checks expect particular Open Food Facts products (e.g. "Oats so simple")
# to appear in search results, so they can fail if that live data changes.
ADB=~/Library/Android/sdk/platform-tools/adb
S=${0:A:h}/output; mkdir -p $S
PKG=com.example.nutrimpact
APK=${0:A:h}/../../app/build/outputs/apk/debug/app-debug.apk
PASS=0; FAIL=0

ui() { $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; $ADB shell cat /sdcard/ui.xml; }
node() { ui | tr '>' '\n' | grep -- "$1" | head -1; }
has() { ui | grep -q -- "$1"; }
ok() { echo "PASS  $1"; PASS=$((PASS+1)); }
bad() { echo "FAIL  $1"; FAIL=$((FAIL+1)); }
check() { if [[ "$3" == "not" ]]; then has "$2" && bad "$1 (found: $2)" || ok "$1"; else has "$2" && ok "$1" || bad "$1 (missing: $2)"; fi; }
is_checked() { node "$1" | grep -q 'checked="true"'; }
attr() { node "$1" | grep -o "$2=\"[^\"]*\"" | head -1 | cut -d'"' -f2; }
# Tap 85% of the way across a node, clear of the emulator's floating keyboard toolbar on the left.
tap_node() {
  local a=(${=$(node "$1" | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' ')})
  [[ ${#a} -lt 4 ]] && { echo "  (could not find $1)"; return 1; }
  $ADB shell input tap $(( a[1] + (a[3]-a[1])*85/100 )) $(( (a[2]+a[4])/2 ))
}
set_amount() { tap_node 'id/editAmount'; sleep 0.3; $ADB shell input keyevent KEYCODE_MOVE_END; for i in $(seq 1 8); do $ADB shell input keyevent KEYCODE_DEL; done; [[ -n "$1" ]] && $ADB shell input text "$1"; sleep 0.6; }
search() { tap_node 'id/editSearch'; sleep 0.3; $ADB shell input keyevent KEYCODE_MOVE_END; for i in $(seq 1 12); do $ADB shell input keyevent KEYCODE_DEL; done; $ADB shell input text "$1"; sleep 0.3; tap_node 'content-desc="Search"'; for i in $(seq 1 15); do sleep 1; has 'id/progressSearch' || break; done; sleep 1; }
open_search() { tap_node 'text="Add food"'; sleep 2.5; }
preview() { attr 'id/textPortionCalories' text; }
rotate() { $ADB shell settings put system user_rotation $1; sleep 5; }

$ADB shell settings put system accelerometer_rotation 0; rotate 0
$ADB install -r $APK >/dev/null
$ADB shell pm clear $PKG >/dev/null   # start with an empty database
$ADB logcat -b crash -c
$ADB shell am start -n $PKG/.DiaryActivity >/dev/null; sleep 4
open_search

echo "== Open Add Food from a result"
search oats
tap_node 'text="Oats so simple'; sleep 2
check "Add Food screen opens" 'id/buttonAddToLog'
check "Product name shown" 'Oats so simple - Winter Vanilla Spice'
check "Brand shown" 'text="Quaker, Quaker Oats"'
check "Per-100g line shown" 'Per 100g: 379 kcal · Protein 8.9g · Carbs 68.0g · Fat 6.4g'
[[ "$(attr 'id/editAmount' text)" == "100" ]] && ok "Default portion is 100 g" || bad "Default portion is 100 g"
[[ "$(preview)" == "379 kcal" ]] && ok "Preview for 100 g = 379 kcal" || bad "Preview for 100 g ($(preview))"
n=$(ui | grep -o 'checked="true"' | wc -l | tr -d ' ')
[[ $n -ge 2 ]] && ok "A meal is pre-selected (plus the g button)" || bad "A meal is pre-selected"

echo "== Portion scaling"
set_amount 40
[[ "$(preview)" == "152 kcal" ]] && ok "40 g = 152 kcal" || bad "40 g preview ($(preview))"
check "40 g macros scaled" 'Protein 3.6g · Carbs 27.2g · Fat 2.6g'
set_amount 250
[[ "$(preview)" == "948 kcal" ]] && ok "250 g = 948 kcal" || bad "250 g preview ($(preview))"
set_amount 12.5
[[ "$(preview)" == "47 kcal" ]] && ok "Decimal 12.5 g = 47 kcal" || bad "12.5 g preview ($(preview))"

echo "== Grams/ounces toggle"
set_amount 40
tap_node 'id/buttonOunces'; sleep 1
is_checked 'id/buttonOunces' && ok "oz button selected" || bad "oz button selected"
[[ "$(attr 'id/editAmount' text)" == "1.4" ]] && ok "40 g converts to 1.4 oz" || bad "40 g -> oz ($(attr 'id/editAmount' text))"
check "Suffix shows oz" 'text="oz" resource-id="com.example.nutrimpact:id/textinput_suffix_text"'
set_amount 2
[[ "$(preview)" == "215 kcal" ]] && ok "2 oz (56.7 g) = 215 kcal" || bad "2 oz preview ($(preview))"
tap_node 'id/buttonGrams'; sleep 1
[[ "$(attr 'id/editAmount' text)" == "56.7" ]] && ok "2 oz converts back to 56.7 g" || bad "oz -> g ($(attr 'id/editAmount' text))"

echo "== Validation"
set_amount 0
check "0 g shows error" 'Enter an amount above 0'
[[ "$(attr 'id/buttonAddToLog' enabled)" == "false" ]] && ok "Add disabled for 0 g" || bad "Add disabled for 0 g"
set_amount 6000
check "6000 g shows error" 'Enter an amount above 0'
[[ "$(attr 'id/buttonAddToLog' enabled)" == "false" ]] && ok "Add disabled for 6000 g" || bad "Add disabled for 6000 g"
tap_node 'id/buttonOunces'; sleep 1; set_amount 177
check "177 oz (over 5 kg) shows error" 'Enter an amount above 0'
set_amount 176
[[ "$(attr 'id/buttonAddToLog' enabled)" == "true" ]] && ok "176 oz (just under 5 kg) allowed" || bad "176 oz allowed"
tap_node 'id/buttonGrams'; sleep 1
set_amount ""
check "Empty box: hint shown" 'Enter an amount to see the nutrition'
check "Empty box: no error message" 'Enter an amount above 0' not
[[ "$(attr 'id/buttonAddToLog' enabled)" == "false" ]] && ok "Add disabled when empty" || bad "Add disabled when empty"
set_amount "."
[[ "$(attr 'id/buttonAddToLog' enabled)" == "false" ]] && ok "Add disabled for '.'" || bad "Add disabled for '.'"

echo "== Rotation keeps the form"
tap_node 'id/buttonOunces'; sleep 1; set_amount 2; tap_node 'text="Lunch"'; sleep 0.5
rotate 1
[[ "$(attr 'id/editAmount' text)" == "2" ]] && ok "Landscape: amount kept" || bad "Landscape: amount kept"
is_checked 'id/buttonOunces' && ok "Landscape: oz kept" || bad "Landscape: oz kept"
$ADB shell input swipe 1100 600 1100 150 400; sleep 1; $ADB shell input swipe 1100 600 1100 150 400; sleep 3
is_checked 'text="Lunch"' && ok "Landscape: Lunch kept" || bad "Landscape: Lunch kept"
[[ "$(preview)" == "215 kcal" ]] && ok "Landscape: preview kept (215 kcal)" || bad "Landscape: preview kept ($(preview))"
check "Landscape: Add button reachable by scrolling" 'id/buttonAddToLog'
$ADB exec-out screencap -p > $S/c_land.png
rotate 0
is_checked 'id/buttonOunces' && ok "Portrait again: oz kept" || bad "Portrait again: oz kept"
is_checked 'text="Lunch"' && ok "Portrait again: Lunch kept" || bad "Portrait again: Lunch kept"
[[ "$(preview)" == "215 kcal" ]] && ok "Portrait again: preview kept" || bad "Portrait again: preview kept"

echo "== Save (Lunch, 2 oz)"
tap_node 'id/buttonAddToLog'; sleep 2
check "Returns to the diary after saving" 'id/recyclerDiary'
check "Diary shows the saved food" 'text="Oats so simple - Winter Vanilla Spice"'

echo "== Save (Snacks, 30 g)"
open_search; search oats
tap_node 'text="Oat Bran"'; sleep 2
set_amount 30; tap_node 'text="Snacks"'; sleep 0.5
tap_node 'id/buttonAddToLog'; sleep 2

echo "== Save in landscape (Breakfast, 40 g)"
open_search; search oats
tap_node 'text="Hanaa Whole Oats"'; sleep 2
set_amount 40; tap_node 'text="Breakfast"'; sleep 0.5
rotate 1
$ADB shell input swipe 1100 600 1100 150 400; sleep 1; $ADB shell input swipe 1100 600 1100 150 400; sleep 3
tap_node 'id/buttonAddToLog'; sleep 2
check "Saving from landscape returns to the diary" 'id/recyclerDiary'
rotate 0

echo "== Back button doesn't save"
open_search; search oats
tap_node 'text="Happy Oats Barista'; sleep 2
tap_node 'content-desc="Back"'; sleep 2
check "Toolbar back returns to results" 'id/recyclerResults'

echo "== Products with no nutrition data"
search heinz
tap_node 'text="No nutrition data available"'; sleep 1
check "Snackbar explains it can't be logged" "be added to your log"
check "Add Food screen did not open" 'id/buttonAddToLog' not

echo "== Database contents"
pull_db() { rm -f $S/$1 $S/$1-wal; $ADB shell run-as $PKG cat databases/nutrimpact.db > $S/$1; $ADB shell run-as $PKG cat databases/nutrimpact.db-wal > $S/$1-wal 2>/dev/null; }
pull_db log.db
sqlite3 -header -column $S/log.db "SELECT product_name, meal_type, ROUND(portion_grams,1) AS grams, ROUND(calories_per_100g*portion_grams/100,1) AS kcal, date(log_date*86400,'unixepoch') AS day FROM food_log ORDER BY logged_at;"
[[ "$(sqlite3 $S/log.db 'SELECT COUNT(*) FROM food_log;')" == "3" ]] && ok "Exactly 3 entries (cancelled one not saved)" || bad "Expected 3 entries"
[[ "$(sqlite3 $S/log.db 'SELECT group_concat(meal_type, " ") FROM (SELECT meal_type FROM food_log ORDER BY logged_at);')" == "LUNCH SNACKS BREAKFAST" ]] && ok "Meals saved correctly" || bad "Meals saved correctly"
[[ "$(sqlite3 $S/log.db 'SELECT ROUND(portion_grams,2) FROM food_log ORDER BY logged_at LIMIT 1;')" == "56.7" ]] && ok "2 oz stored as 56.7 g" || bad "2 oz stored as grams"
[[ "$(sqlite3 $S/log.db "SELECT COUNT(*) FROM food_log WHERE log_date = CAST(julianday('now','localtime') - 2440587.5 AS INTEGER);")" == "3" ]] && ok "Entries dated today" || bad "Entries dated today"

echo "== Data survives restarting the app"
$ADB shell am force-stop $PKG; $ADB shell am start -n $PKG/.DiaryActivity >/dev/null; sleep 4
pull_db log2.db
[[ "$(sqlite3 $S/log2.db 'SELECT COUNT(*) FROM food_log;')" == "3" ]] && ok "3 entries still there after restart" || bad "Entries after restart"

echo "== Crashes"
$ADB logcat -d -b crash | grep -q "$PKG" && { bad "App crashed"; $ADB logcat -d -b crash | tail -20; } || ok "No crashes"

$ADB shell settings put system accelerometer_rotation 1
echo "== $PASS passed, $FAIL failed"
