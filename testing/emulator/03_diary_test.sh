#!/bin/zsh
# Module: EEE521 - Final Year Project
# Project: Nutrimpact
# File: 03_diary_test.sh
# Description: Sprint 4 - daily diary: meal grouping, totals, day switching, logging to past days, swipe-to-delete with Undo, rotation, restart.
#
# End-to-end check script for a running Android emulator. It drives the app with
# adb (taps, typing, swipes, rotation) and reads the screen with uiautomator,
# printing PASS/FAIL for each check and a total at the end.
#
# Requirements: one emulator or device connected (adb devices), internet access,
# and a debug build: ./gradlew assembleDebug. Run from anywhere: zsh 03_diary_test.sh
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
attr() { node "$1" | grep -o "$2=\"[^\"]*\"" | head -1 | cut -d'"' -f2; }
eq() { [[ "$2" == "$3" ]] && ok "$1" || bad "$1 (expected '$3', got '$2')"; }
bounds() { node "$1" | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' '; }
tap_node() {
  local a=(${=$(bounds "$1")}); [[ ${#a} -lt 4 ]] && { echo "  (could not find $1)"; return 1; }
  $ADB shell input tap $(( a[1] + (a[3]-a[1])*85/100 )) $(( (a[2]+a[4])/2 ))
}
swipe_away() {  # swipe a row off to the left
  local a=(${=$(bounds "$1")}); [[ ${#a} -lt 4 ]] && { echo "  (could not find $1)"; return 1; }
  local y=$(( (a[2]+a[4])/2 )); $ADB shell input swipe 900 $y 60 $y 400
}
set_amount() { tap_node 'id/editAmount'; sleep 0.3; $ADB shell input keyevent KEYCODE_MOVE_END; for i in $(seq 1 8); do $ADB shell input keyevent KEYCODE_DEL; done; $ADB shell input text "$1"; sleep 0.6; }
search() { tap_node 'id/editSearch'; sleep 0.3; $ADB shell input keyevent KEYCODE_MOVE_END; for i in $(seq 1 12); do $ADB shell input keyevent KEYCODE_DEL; done; $ADB shell input text "$1"; sleep 0.3; tap_node 'content-desc="Search"'; for i in $(seq 1 15); do sleep 1; has 'id/progressSearch' || break; done; sleep 1; }
log_food() {  # product, grams, meal
  tap_node 'text="Add food"'; sleep 2.5; search oats
  tap_node "text=\"$1"; sleep 2; set_amount "$2"; tap_node "text=\"$3\""; sleep 0.5
  tap_node 'id/buttonAddToLog'; sleep 3
}
total() { attr 'id/textTotalCalories' text; }
day() { attr 'id/textDiaryDay' text; }
rotate() { $ADB shell settings put system user_rotation $1; sleep 5; }
pull_db() { rm -f $S/$1 $S/$1-wal; $ADB shell run-as $PKG cat databases/nutrimpact.db > $S/$1; $ADB shell run-as $PKG cat databases/nutrimpact.db-wal > $S/$1-wal 2>/dev/null; }

$ADB shell settings put system accelerometer_rotation 0; rotate 0
$ADB install -r $APK >/dev/null
$ADB shell pm clear $PKG >/dev/null
$ADB logcat -b crash -c
$ADB shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 5

echo "== App opens on an empty diary for today"
check "Launcher opens the diary" 'id/recyclerDiary\|id/textDiaryEmpty'
eq "Day label is Today" "$(day)" "Today"
check "Empty message shown" 'Nothing logged for this day yet'
eq "Total is 0 kcal" "$(total)" "0 kcal"
eq "Next-day button disabled on today" "$(attr 'content-desc="Next day"' enabled)" "false"
check "Add food button shown" 'text="Add food"'
$ADB exec-out screencap -p > $S/f_empty.png

echo "== Log two foods today"
log_food "Oats so simple" 40 Breakfast
check "Back on the diary after saving" 'id/recyclerDiary'
eq "Still showing Today" "$(day)" "Today"
check "Breakfast heading shown" 'text="Breakfast"'
check "Food shown in diary" 'text="Oats so simple - Winter Vanilla Spice"'
check "Portion and brand shown" 'text="40 g · Quaker, Quaker Oats"'
eq "Total = 152 kcal" "$(total)" "152 kcal"
check "Empty message hidden" 'Nothing logged for this day yet' not
log_food "Oat Bran" 30 Snacks
eq "Total = 197 kcal (151.6 + 45)" "$(total)" "197 kcal"
eq "Protein progress shown" "$(attr 'id/textProteinProgress' text)" "Protein 6 / 50g"
b=$(ui | grep -o 'text="Breakfast"\|text="Snacks"' | tr '\n' ' ')
eq "Meals in order: Breakfast before Snacks" "$b" 'text="Breakfast" text="Snacks" '
$ADB exec-out screencap -p > $S/f_today.png

echo "== Previous day"
tap_node 'content-desc="Previous day"'; sleep 1.5
eq "Label is Yesterday" "$(day)" "Yesterday"
eq "Yesterday total is 0 kcal" "$(total)" "0 kcal"
check "Yesterday is empty" 'Nothing logged for this day yet'
eq "Next-day button enabled" "$(attr 'content-desc="Next day"' enabled)" "true"
tap_node 'content-desc="Previous day"'; sleep 1.5
expected=$(date -v-2d "+%a %-d %b")
eq "Two days ago shows the date" "$(day)" "$expected"
tap_node 'content-desc="Next day"'; sleep 1.5
eq "Next goes forward to Yesterday" "$(day)" "Yesterday"

echo "== Log a food to yesterday"
log_food "Hanaa Whole Oats" 100 Lunch
eq "Returns to Yesterday after saving" "$(day)" "Yesterday"
eq "Yesterday total = 370 kcal" "$(total)" "370 kcal"
tap_node 'content-desc="Next day"'; sleep 1.5
eq "Today unchanged at 197 kcal" "$(total)" "197 kcal"

echo "== Swipe to delete, with Undo"
swipe_away 'text="Oat Bran"'; sleep 0.6
d=$(ui)   # read the screen once, then tap Undo straight away
echo "$d" | grep -q 'Removed Oat Bran' && ok "Snackbar confirms removal" || bad "Snackbar confirms removal"
echo "$d" | grep -q 'text="Oat Bran"' && bad "Row removed from list" || ok "Row removed from list"
echo "$d" | grep -q 'text="Snacks"' && bad "Empty Snacks heading removed" || ok "Empty Snacks heading removed"
echo "$d" | tr '>' '\n' | grep 'id/textTotalCalories' | grep -q 'text="152 kcal"' && ok "Total drops to 152 kcal" || bad "Total drops to 152 kcal"
u=(${=$(echo "$d" | tr '>' '\n' | grep 'text="Undo"' | head -1 | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' ')})
[[ ${#u} -ge 4 ]] && $ADB shell input tap $(( (u[1]+u[3])/2 )) $(( (u[2]+u[4])/2 )); sleep 1.5
eq "Undo restores 197 kcal" "$(total)" "197 kcal"
check "Oat Bran back in the list" 'text="Oat Bran"'
check "Snacks heading back" 'text="Snacks"'
swipe_away 'text="Breakfast"'; sleep 1
check "Meal headings can't be swiped away" 'text="Breakfast"'
eq "Total unchanged after swiping a heading" "$(total)" "197 kcal"
swipe_away 'text="Oat Bran"'; sleep 7
check "Snackbar gone after 5 s" 'Removed Oat Bran' not
eq "Delete sticks when Undo isn't pressed" "$(total)" "152 kcal"
swipe_away 'text="Oats so simple'; sleep 0.6
d=$(ui)
echo "$d" | grep -q 'Nothing logged for this day yet' && ok "Deleting the last food shows the empty message" || bad "Empty message after deleting last food"
u=(${=$(echo "$d" | tr '>' '\n' | grep 'text="Undo"' | head -1 | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' ')})
[[ ${#u} -ge 4 ]] && $ADB shell input tap $(( (u[1]+u[3])/2 )) $(( (u[2]+u[4])/2 )); sleep 1.5
eq "Undo brings it back (152 kcal)" "$(total)" "152 kcal"

echo "== Rotation"
tap_node 'content-desc="Previous day"'; sleep 1.5
rotate 1
eq "Landscape: still on Yesterday" "$(day)" "Yesterday"
eq "Landscape: total kept" "$(total)" "370 kcal"
$ADB exec-out screencap -p > $S/f_land.png
rotate 0
eq "Portrait: still on Yesterday" "$(day)" "Yesterday"
tap_node 'content-desc="Next day"'; sleep 1.5

echo "== Navigation"
tap_node 'text="Add food"'; sleep 2.5
$ADB shell input keyevent KEYCODE_BACK; sleep 1.5
check "Back from search returns to the diary" 'id/textDiaryDay'

echo "== Database and restart"
pull_db d.db
sqlite3 -header -column $S/d.db "SELECT product_name, meal_type, portion_grams AS g, date(log_date*86400,'unixepoch') AS day FROM food_log ORDER BY log_date, logged_at;"
today=$(sqlite3 $S/d.db "SELECT CAST(julianday('now','localtime') - 2440587.5 AS INTEGER);")
eq "Yesterday's entry stored with yesterday's date" "$(sqlite3 $S/d.db "SELECT COUNT(*) FROM food_log WHERE log_date = $today - 1;")" "1"
eq "Today has 1 entry left after delete" "$(sqlite3 $S/d.db "SELECT COUNT(*) FROM food_log WHERE log_date = $today;")" "1"
$ADB shell am force-stop $PKG; $ADB shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 5
eq "After restart: opens on Today" "$(day)" "Today"
eq "After restart: total kept (152 kcal)" "$(total)" "152 kcal"

echo "== Dark mode"
$ADB shell cmd uimode night yes >/dev/null; sleep 3
eq "Dark mode: diary still shows total" "$(total)" "152 kcal"
$ADB exec-out screencap -p > $S/f_dark.png
$ADB shell cmd uimode night no >/dev/null; sleep 2

echo "== Crashes"
$ADB logcat -d -b crash | grep -q "$PKG" && { bad "App crashed"; $ADB logcat -d -b crash | tail -20; } || ok "No crashes"
$ADB shell settings put system accelerometer_rotation 1
echo "== $PASS passed, $FAIL failed"
