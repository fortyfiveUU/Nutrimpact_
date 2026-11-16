#!/bin/zsh
# Module: EEE521 - Final Year Project
# Project: Nutrimpact
# File: run_all.sh
# Description: Runs every emulator check script in order and prints each script's
# failures and totals. Takes around 20 minutes. Each script clears the app's data
# first, so they can run in any order.
#
# Requirements: an emulator or device connected (adb devices), internet access,
# and a debug build: ./gradlew assembleDebug

DIR=${0:A:h}
for script in $DIR/0*_test.sh; do
  echo "######## ${script:t}"
  ~/Library/Android/sdk/platform-tools/adb logcat -b crash -c
  zsh "$script" 2>&1 | grep -E '^(FAIL|== [0-9])'
done
