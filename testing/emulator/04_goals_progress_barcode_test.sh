#!/bin/zsh
# Module: EEE521 - Final Year Project
# Project: Nutrimpact
# File: 04_goals_progress_barcode_test.sh
# Description: Sprint 5 - goals, settings, kcal/kJ, ounces default, Progress screen, typed barcode lookup, scanner permission and camera.
#
# End-to-end check script for a running Android emulator. It drives the app with
# adb (taps, typing, swipes, rotation) and reads the screen with uiautomator,
# printing PASS/FAIL for each check and a total at the end.
#
# Requirements: one emulator or device connected (adb devices), internet access,
# and a debug build: ./gradlew assembleDebug. Run from anywhere: zsh 04_goals_progress_barcode_test.sh
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
tap_node() {
  local a=(${=$(node "$1" | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' ')})
  [[ ${#a} -lt 4 ]] && { echo "  (could not find $1)"; return 1; }
  $ADB shell input tap $(( a[1] + (a[3]-a[1])*85/100 )) $(( (a[2]+a[4])/2 ))
}
tap_centre() {
  local a=(${=$(node "$1" | grep -o 'bounds="[^"]*"' | grep -o '[0-9]*' | tr '\n' ' ')})
  [[ ${#a} -lt 4 ]] && { echo "  (could not find $1)"; return 1; }
  $ADB shell input tap $(( (a[1]+a[3])/2 )) $(( (a[2]+a[4])/2 ))
}
type_into() { tap_node "$1"; sleep 0.3; $ADB shell input keyevent KEYCODE_MOVE_END; for i in $(seq 1 14); do $ADB shell input keyevent KEYCODE_DEL; done; [[ -n "$2" ]] && $ADB shell input text "$2"; sleep 0.5; }
search() { type_into 'id/editSearch' "$1"; tap_node 'content-desc="Search"'; for i in $(seq 1 15); do sleep 1; has 'id/progressSearch' || break; done; sleep 1; }
log_food() {  # query, product, amount, meal  (amount in the unit already selected)
  tap_node 'text="Add food"'; sleep 2.5; search "$1"
  tap_node "text=\"$2"; sleep 2; [[ -n "$3" ]] && type_into 'id/editAmount' "$3"; tap_node "text=\"$4\""; sleep 0.5
  tap_node 'id/buttonAddToLog'; sleep 3
}
diary_text() { attr "id/$1" text; }
launch() { $ADB shell am force-stop $PKG; $ADB shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; sleep 4.5; }

$ADB shell settings put system accelerometer_rotation 0; $ADB shell settings put system user_rotation 0
$ADB install -r $APK >/dev/null
$ADB shell pm clear $PKG >/dev/null
$ADB logcat -b crash -c
launch

echo "== Diary shows default goals"
check "Top bar has Progress button" 'content-desc="Progress"'
check "Top bar has Settings button" 'content-desc="Settings"'
eq "Total starts at 0 kcal" "$(diary_text textTotalCalories)" "0 kcal"
eq "Calorie goal line (UK default 2,000 kcal)" "$(diary_text textCalorieGoal)" "of 2,000 kcal · 2,000 kcal left"
eq "Protein goal (50g)" "$(diary_text textProteinProgress)" "Protein 0 / 50g"
eq "Carbs goal (260g)" "$(diary_text textCarbsProgress)" "Carbs 0 / 260g"
eq "Fat goal (70g)" "$(diary_text textFatProgress)" "Fat 0 / 70g"
$ADB exec-out screencap -p > $S/s5_diary_empty.png

echo "== Settings screen"
tap_centre 'content-desc="Settings"'; sleep 2
eq "Calorie goal field shows 2000" "$(attr 'id/editCalorieGoal' text)" "2000"
eq "Protein field shows 50" "$(attr 'id/editProteinGoal' text)" "50"
check "Privacy note shown" 'Nutrimpact has no accounts'
is_on() { node "$1" | grep -q 'checked="true"'; }
is_on 'id/buttonUnitKcal' && ok "kcal selected by default" || bad "kcal selected by default"
is_on 'id/buttonPortionGrams' && ok "grams selected by default" || bad "grams selected by default"
type_into 'id/editCalorieGoal' 100
tap_centre 'id/buttonSaveSettings'; sleep 1
check "Too-low calorie goal shows error" 'Enter a number from 500 to 10,000'
check "Still on Settings after invalid save" 'id/buttonSaveSettings'
type_into 'id/editProteinGoal' 0
tap_centre 'id/buttonSaveSettings'; sleep 1
check "Zero protein goal shows error" 'Enter a number from 1 to 1,000'
tap_centre 'id/buttonResetGoals'; sleep 0.5
eq "Reset restores 2000" "$(attr 'id/editCalorieGoal' text)" "2000"
check "Reset clears errors" 'Enter a number from' not
type_into 'id/editCalorieGoal' 1800
type_into 'id/editProteinGoal' 120
$ADB shell input keyevent KEYCODE_ESCAPE; sleep 0.3
tap_centre 'id/buttonUnitKj'; sleep 0.3
tap_centre 'id/buttonPortionOunces'; sleep 0.3
$ADB exec-out screencap -p > $S/s5_settings.png
tap_centre 'id/buttonSaveSettings'; sleep 2
check "Save returns to the diary" 'id/textDiaryDay'
eq "Diary now in kJ" "$(diary_text textTotalCalories)" "0 kJ"
eq "Goal line converted to kJ (1,800 kcal = 7,531 kJ)" "$(diary_text textCalorieGoal)" "of 7,531 kJ · 7,531 kJ left"
eq "Protein goal updated to 120g" "$(diary_text textProteinProgress)" "Protein 0 / 120g"

echo "== Settings survive a restart"
launch
eq "Still kJ after restart" "$(diary_text textTotalCalories)" "0 kJ"
tap_centre 'content-desc="Settings"'; sleep 2
eq "Calorie goal saved as 1800" "$(attr 'id/editCalorieGoal' text)" "1800"
is_on 'id/buttonUnitKj' && ok "kJ still selected" || bad "kJ still selected"
is_on 'id/buttonPortionOunces' && ok "oz still selected" || bad "oz still selected"
$ADB shell input keyevent KEYCODE_BACK; sleep 1.5

echo "== Logging with kJ and the ounces default"
tap_node 'text="Add food"'; sleep 2.5; search oats
check "Search cards in kJ (379 kcal = 1,586 kJ)" 'text="1,586 kJ per 100g"'
tap_node 'text="Oats so simple'; sleep 2
is_on 'id/buttonOunces' && ok "Add Food starts in ounces" || bad "Add Food starts in ounces"
eq "Default amount 3.5 oz (100 g)" "$(attr 'id/editAmount' text)" "3.5"
check "Per-100g line in kJ" 'Per 100g: 1,586 kJ'
tap_centre 'id/buttonGrams'; sleep 0.8; type_into 'id/editAmount' 40
eq "Preview in kJ (40 g = 634 kJ)" "$(attr 'id/textPortionCalories' text)" "634 kJ"
tap_node 'text="Breakfast"'; sleep 0.5; tap_node 'id/buttonAddToLog'; sleep 3
eq "Diary total 634 kJ" "$(diary_text textTotalCalories)" "634 kJ"
eq "Left to eat (7,531 − 634)" "$(diary_text textCalorieGoal)" "of 7,531 kJ · 6,897 kJ left"
eq "Protein progress" "$(diary_text textProteinProgress)" "Protein 4 / 120g"
check "Meal subtotal in kJ" 'text="634 kJ"'

echo "== Going over the goal"
tap_node 'text="Add food"'; sleep 2.5; search oats
tap_node 'text="Oats so simple'; sleep 2
tap_centre 'id/buttonGrams'; sleep 0.8; type_into 'id/editAmount' 500
tap_node 'text="Dinner"'; sleep 0.5; tap_node 'id/buttonAddToLog'; sleep 3
eq "Over-goal message (2,047 − 1,800 kcal = 1,032 kJ over)" "$(diary_text textCalorieGoal)" "of 7,531 kJ · 1,032 kJ over"
$ADB exec-out screencap -p > $S/s5_over.png

echo "== Back to kcal"
tap_centre 'content-desc="Settings"'; sleep 2; tap_centre 'id/buttonUnitKcal'; sleep 0.3; tap_centre 'id/buttonSaveSettings'; sleep 2
eq "Diary back in kcal" "$(diary_text textTotalCalories)" "2,047 kcal"
eq "Over message in kcal" "$(diary_text textCalorieGoal)" "of 1,800 kcal · 247 kcal over"

echo "== Log a day within the goal (yesterday)"
tap_centre 'content-desc="Previous day"'; sleep 1.5
tap_node 'text="Add food"'; sleep 2.5; search oats
tap_node 'text="Oat Bran"'; sleep 2; tap_centre 'id/buttonGrams'; sleep 0.8; type_into 'id/editAmount' 100
tap_node 'text="Lunch"'; sleep 0.5; tap_node 'id/buttonAddToLog'; sleep 3
eq "Yesterday total 150 kcal" "$(diary_text textTotalCalories)" "150 kcal"
tap_centre 'content-desc="Next day"'; sleep 1.5

echo "== Progress screen"
tap_centre 'content-desc="Progress"'; sleep 2.5
check "Progress screen opens" 'id/chartWeek'
eq "Average over logged days ((2,047 + 150) / 2)" "$(attr 'id/textAverage' text)" "1,098 kcal a day on average"
eq "Days logged" "$(attr 'id/textDaysLogged' text)" "Based on 2 of 7 days logged"
eq "Days within goal" "$(attr 'id/textWithinGoal' text)" "Within your goal on 1 of 2 days"
check "Protein average vs goal ((48.1 + 7) / 2)" 'Protein: 28g (goal 120g)'
range_expected="$(date -v-6d '+%-d %b') – $(date '+%-d %b')"
eq "Week range shows last 7 days" "$(attr 'id/textWeekRange' text)" "$range_expected"
eq "Next-week disabled on latest week" "$(attr 'content-desc="Next week"' enabled)" "false"
check "Chart has a description for screen readers" 'Bar chart of energy eaten each day this week'
$ADB exec-out screencap -p > $S/s5_progress.png
tap_centre 'content-desc="Previous week"'; sleep 1.5
check "Previous week is empty" 'Nothing logged in this week.'
check "Summary hidden for empty week" 'id/textAverage' not
tap_centre 'content-desc="Next week"'; sleep 1.5
eq "Back to this week" "$(attr 'id/textAverage' text)" "1,098 kcal a day on average"
$ADB shell settings put system user_rotation 1; sleep 4
eq "Rotation keeps the week" "$(attr 'id/textDaysLogged' text)" "Based on 2 of 7 days logged"
$ADB exec-out screencap -p > $S/s5_progress_land.png
$ADB shell settings put system user_rotation 0; sleep 3
$ADB shell input keyevent KEYCODE_BACK; sleep 1.5
check "Back returns to diary" 'id/textDiaryDay'

echo "== Typed barcodes"
tap_node 'text="Add food"'; sleep 2.5
search 3017620422003
check "Barcode lookup finds Nutella" 'text="Nutella"'
check "Brand string split and joined" 'text="Ferrero, Nutella"'
check "Nutrition shown (539 kcal)" 'text="539 kcal per 100g"'
n=$(ui | grep -o 'id/textProductName' | wc -l | tr -d ' '); eq "Exactly one result for a barcode" "$n" "1"
tap_node 'text="Nutella"'; sleep 2
check "Barcode result opens Add Food" 'Per 100g: 539 kcal'
$ADB shell input keyevent KEYCODE_BACK; sleep 1; $ADB shell input keyevent KEYCODE_BACK; sleep 1.5
has 'id/editSearch' || { tap_node 'text="Add food"'; sleep 2.5; }
search 0000000000000
check "Unknown barcode message" 'No product found for barcode 0000000000000'
search 3017620422004
check "Wrong check digit isn't treated as a barcode" 'No product found for barcode' not

echo "== Scanner: permission refused"
check "Barcode icon in search bar" 'content-desc="Scan a barcode"'
tap_centre 'content-desc="Scan a barcode"'; sleep 2.5
check "Camera permission is requested" 'permission_deny_button\|permission_allow'
$ADB exec-out screencap -p > $S/s5_perm.png
tap_centre 'id/permission_deny_button'; sleep 1.5
check "Refusal shows explanation" 'Nutrimpact needs the camera to scan barcodes'
tap_centre 'id/buttonScannerClose'; sleep 1.5
check "Close returns to search" 'id/editSearch'

echo "== Scanner: permission granted"
$ADB shell pm grant $PKG android.permission.CAMERA
tap_centre 'content-desc="Scan a barcode"'; sleep 4
check "Camera preview shown" 'id/previewCamera'
check "Scan hint shown" 'Point the camera at a product barcode'
check "No error message" 'Nutrimpact needs the camera' not
$ADB exec-out screencap -p > $S/s5_scanner.png
$ADB shell settings put system user_rotation 1; sleep 4
check "Scanner survives rotation" 'id/previewCamera'
$ADB shell settings put system user_rotation 0; sleep 3
tap_centre 'content-desc="Back"'; sleep 1.5
check "Back from scanner returns to search" 'id/editSearch'

echo "== Crashes"
$ADB logcat -d -b crash | grep -q "$PKG" && { bad "App crashed"; $ADB logcat -d -b crash | tail -25; } || ok "No crashes"
$ADB shell settings put system accelerometer_rotation 1
echo "== $PASS passed, $FAIL failed"
