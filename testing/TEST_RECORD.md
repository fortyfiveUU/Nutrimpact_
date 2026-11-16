# Nutrimpact – Test Record

Module: EEE521 – Final Year Project · Project: Nutrimpact

This record lists the testing carried out on Nutrimpact during Sprints 3–6, the results, the defects found and how they were fixed, and what has not yet been tested.

---

## 1. Test environment

| Item | Value |
|---|---|
| Development machine | Apple Silicon Mac, macOS |
| Build tools | Android Gradle Plugin 8.2.2, Gradle 8.2, JDK 17 (JetBrains Runtime) |
| App settings | `compileSdk` 34, `targetSdk` 34, `minSdk` 26; 100% Java (no Kotlin) |
| Emulator | Pixel 3a AVD, Android 16 (API 36), arm64, 1080 × 2220 |
| Extra screen configurations | Same emulator resized: small phone (720 × 1280 at 320 dpi = 360 × 640 dp), tablet (1600 × 2560 at 320 dpi = 800 × 1280 dp), and text at 130% |
| Network | Live Open Food Facts API for the emulator scripts; a fake local server (OkHttp MockWebServer) for the instrumented search tests |

---

## 2. Types of testing

| Type | What it covers | Where it lives | How to run |
|---|---|---|---|
| **Unit tests** (JUnit 4) | Pure Java logic: portion scaling, unit conversion, meal grouping, totals, goals, barcode check digits, weekly summaries, JSON parsing | `app/src/test/` | `./gradlew testDebugUnitTest`, or right-click `app/src/test` → Run Tests in Android Studio |
| **Instrumented tests** (JUnit 4 + Espresso + Room) | Database queries on a real device; each screen driven through its real UI; search against a fake server, including server errors | `app/src/androidTest/` | Start an emulator, then `./gradlew connectedDebugAndroidTest`, or right-click `app/src/androidTest` → Run Tests |
| **Static analysis** (Android Lint) | Code and resource problems, API-level issues, accessibility hints | Built in | `./gradlew lintDebug` |
| **End-to-end emulator scripts** | The whole app on an emulator against the live API, driven by `adb` (taps, typing, swipes, rotation, airplane mode, dark mode) and read with `uiautomator`; also reads the app's database file | `testing/emulator/` | `zsh testing/emulator/run_all.sh` |
| **Screen configuration runs** | The instrumented suite on a small phone, a tablet and with 130% text, plus screenshots | `testing/emulator/screen_configs_test.sh` | `zsh testing/emulator/screen_configs_test.sh` |
| **Manual / visual checks** | Screenshots reviewed for layout, overlap, dark mode, landscape and screen size | `testing/emulator/output/` (not committed) | During each sprint |

---

## 3. Results summary

Latest full run: **16 November 2026**, on the finished Sprint 6 code.

| Suite | Checks | Result |
|---|---|---|
| Unit tests | 38 | ✅ 38 passed |
| Instrumented tests | 32 | ✅ 32 passed |
| Instrumented tests – small phone | 32 | ✅ 32 passed |
| Instrumented tests – tablet | 32 | ✅ 32 passed |
| Instrumented tests – 130% text | 32 | ✅ 32 passed |
| Android Lint | – | ✅ 0 errors (27 warnings: 16 are "newer library version available"; none are bugs) |
| `01_search_test.sh` | 30 | ✅ 30 passed |
| `02_add_food_test.sh` | 46 | ✅ 46 passed |
| `03_diary_test.sh` | 48 | ✅ 48 passed |
| `04_goals_progress_barcode_test.sh` | 68 | ✅ 68 passed |
| **Total** | **358** | **All passing** |

Sprint 5's final run (commit `99da51c`) also passed everything: 39 unit tests and 192 emulator checks. In that run, one step of `03_diary_test.sh` failed once (a search that didn't finish in time), causing 20 follow-on failures; the live data was unchanged and the script passed 48/48 when run again straight afterwards, so this was treated as a one-off timing failure.

---

## 4. Unit tests

| Test class | Tests | What is checked |
|---|---|---|
| `PortionCalculatorTest` | 8 | Scaling per-100g values to a portion (e.g. 40 g of 379 kcal/100g = 151.6 kcal), missing values, zero/negative portions, g ↔ oz conversion, 0–5 kg limits |
| `MealTypeTest` | 2 | Meal suggested for each time of day (boundaries at 05:00, 11:00, 15:00, 17:00, 22:00); unknown stored names fall back to Snacks |
| `DailyTotalsTest` | 3 | Adding up portions for a day; empty/null lists; null entries skipped |
| `DiaryItemTest` | 4 | Grouping foods under meal headings in Breakfast → Snacks order with subtotals; empty meals hidden; unknown meals go to Snacks |
| `NutritionGoalsTest` | 5 | UK Reference Intake defaults (2000 kcal, 50 g protein, 260 g carbs, 70 g fat); valid ranges; progress percentages capped at 100% |
| `EnergyUnitTest` | 3 | kcal ↔ kJ (1 kcal = 4.184 kJ); safe fallback for unknown stored values |
| `BarcodeUtilsTest` | 4 | Real EAN-13, EAN-8, UPC-A and GTIN-14 codes accepted; wrong check digit, wrong length, letters and empty input rejected; GS1 check-digit calculation |
| `WeekSummaryTest` | 5 | Seven days returned with gaps filled; averages count logged days only; days within goal (on the goal counts as within); days outside the week ignored |
| `ProductLookupResponseTest` | 4 | Parsing real-shaped Open Food Facts barcode JSON with Gson; not-found responses; missing fields; splitting the brands string |
| **Total** | **38** | |

(The Android Studio template test `ExampleUnitTest.kt` was removed in Sprint 6 when the project became 100% Java.)

---

## 5. Instrumented tests (added in Sprint 6)

These run on an emulator or phone from Android Studio. The screen tests reset the app's database and settings before each test, so they don't depend on each other or on the internet.

| Test class | Tests | What is checked |
|---|---|---|
| `AppContextTest` | 1 | The tests run against the Nutrimpact app package (Java replacement for the Kotlin template) |
| `FoodLogDaoTest` | 5 | Room queries on an in-memory database: save and read back; filter by day and order by time; delete then restore with the same ID (as Undo does); daily totals scaled in SQL (196.6 kcal for two foods); SQL totals match the Java `DailyTotals` exactly |
| `SearchScreenTest` | 8 | Against a **fake server**: result cards; the "Open Food Facts is having problems" message for HTTP 503; no-results message; empty search error; barcode looked up on the product endpoint; unknown barcode (HTTP 404); wrong check digit searched as text; results shown in kJ when chosen |
| `AddFoodScreenTest` | 5 | Opens at 100 g; preview scales (40 g = 152 kcal); g ↔ oz conversion; 0 g, 6000 g and empty disable Add while 5000 g is allowed; Add saves the right name, meal, grams and day to the database |
| `DiaryScreenTest` | 7 | Empty state; totals and goal line ("of 2,000 kcal · 1,803 kcal left"); over goal ("274 kcal over"); swipe to delete then Undo; **swiping the same food again after Undo, three times in a row**; meal headings can't be swiped; day arrows show each day's own foods |
| `SettingsScreenTest` | 4 | UK defaults and privacy note shown; invalid goals rejected and nothing saved; Reset restores defaults; goals, kJ and ounces saved |
| `ProgressScreenTest` | 2 | Average over logged days (2,000 kcal), days logged, days within goal; empty previous week hides the summary |
| **Total** | **32** | |

**Checking the tests can fail:** to make sure the tests aren't passing by accident, the app was deliberately broken so that a server error showed the wrong message. `SearchScreenTest.serverError_showsProblemMessage` failed as expected, and passed again once the code was restored.

---

## 6. End-to-end emulator scripts

Each script clears the app's data first, then runs through the feature as a user would, against the live Open Food Facts API.

### `01_search_test.sh` – food search (30 checks)
- Start message and prompt text on launch.
- Empty and spaces-only searches show an error; the magnifying glass stays visible; typing clears the error.
- Search runs from the magnifying glass, the on-screen keyboard's search key, and a physical keyboard's Enter key.
- Spinner shown while loading (checked on a slowed-down network) and hidden afterwards.
- Result cards show name, brands joined with commas, kcal per 100g and macros.
- Products with no nutrition data show "No nutrition data available", never 0 kcal.
- No-results message names the search; airplane mode shows the connection message; search works again once back online.
- Two quick searches in a row: only the latest one's results are shown.
- Rotation keeps results, scroll position, search text and messages; a search started before rotating still finishes.
- Dark mode is readable. No crashes.

### `02_add_food_test.sh` – Add Food (46 checks)
- Tapping a result opens Add Food with the product name, brand and per-100g values; default 100 g; a meal is pre-selected.
- Portion scaling: 40 g = 152 kcal, 250 g = 948 kcal, 12.5 g = 47 kcal; macros scaled.
- g/oz toggle converts in both directions (40 g → 1.4 oz; 2 oz → 56.7 g); 2 oz = 215 kcal.
- Validation: 0 g, 6000 g and 177 oz rejected; 176 oz accepted; empty box and "." keep Add disabled.
- Rotation keeps amount, unit, meal and preview; the Add button is reachable in landscape.
- Saving in portrait and landscape returns to the diary; the back button saves nothing; products with no nutrition data can't be added.
- **Database check:** the saved rows are read from the device: correct meals, 2 oz stored as 56.7 g, all dated today, still there after restarting the app.

### `03_diary_test.sh` – daily diary (48 checks)
- App opens on an empty diary for Today with a 0 kcal total and the next-day button disabled.
- Logging foods returns to the diary; foods are grouped by meal in the right order; totals and macros are correct (e.g. 151.6 + 45 = 197 kcal).
- Day switching: Today → Yesterday → a dated day (e.g. "Sat 14 Nov") and back; logging to Yesterday stores yesterday's date.
- Swipe to delete: snackbar, row and empty heading removed, total updated; Undo restores it; headings can't be swiped; deletes stick if Undo isn't pressed; deleting the last food shows the empty message.
- Rotation keeps the day; back from search returns to the diary; data survives restarting the app; dark mode. No crashes.

### `04_goals_progress_barcode_test.sh` – goals, progress and barcodes (68 checks)
- Diary shows the UK default goals and progress lines; top bar has Progress and Settings.
- Settings: shows saved values; rejects a calorie goal of 100 and a protein goal of 0; Reset restores defaults; Save returns to the diary; settings survive a restart.
- kJ: search cards (379 kcal = 1,586 kJ), Add Food, diary totals, goal line (1,800 kcal = 7,531 kJ) and meal subtotals all switch unit.
- Ounces default: Add Food opens in ounces at 3.5 oz (100 g).
- Going over the goal shows "… over" (e.g. 1,032 kJ over), and the bar turns red.
- Progress screen: average over logged days ((2,047 + 150) ÷ 2 = 1,098 kcal), days logged, days within goal, macro averages, week range, next-week disabled on the latest week, empty previous week, rotation, screen-reader description.
- Typed barcodes: 3017620422003 finds Nutella (one result, brands "Ferrero, Nutella", 539 kcal); an unknown barcode shows "No product found…"; a number with a wrong check digit is searched as text instead.
- Scanner: camera permission requested; refusing shows an explanation; granting shows the camera preview and hint; survives rotation; back returns to search. No crashes.

---

## 7. Screen sizes and text size (Sprint 6)

The full instrumented suite was run three more times on the emulator with its display changed, and screenshots of the diary and search screens were reviewed.

| Configuration | Instrumented tests | Visual review |
|---|---|---|
| Small phone, 360 × 640 dp | ✅ 32 / 32 | Everything fits; the long search hint is shortened with "…", which is acceptable |
| Tablet, 800 × 1280 dp | ✅ 32 / 32 | Works; content stretches across the full width, leaving a lot of empty space. A wider tablet layout would look better but isn't needed |
| Text at 130% (accessibility setting) | ✅ 32 / 32 | Readable; the protein and carbs labels on the diary wrap onto two lines ("Protein 0 / ⏎ 50g") |

---

## 8. Defects found and fixed

| # | Found in | Defect | How it was found | Fix |
|---|---|---|---|---|
| 1 | Setup | `data_extraction_rules.xml` had a deleted comment opener, so the project wouldn't build | First build | Restored the template comment |
| 2 | Setup | Product names always showed "Product not listed" | Code review against the API's JSON | Added `@SerializedName("product_name")` |
| 3 | Sprint 3 | Every search failed with HTTP 503: the old `cgi/search.pl` endpoint was overloaded | Emulator test, confirmed with `curl` | Switched to `search.openfoodfacts.org` and adapted the models (`hits`, brands list) |
| 4 | Sprint 3 | After the empty-search error appeared, Material replaced the magnifying glass with an error icon, leaving no search button | Emulator screenshot | Kept the end icon (`errorIconDrawable="@null"`) and cleared the error when typing |
| 5 | Sprint 3 | New results opened at the previous list's scroll position | Emulator test after scrolling | Reset the list to the top when a new search starts |
| 6 | Sprint 4 | Pressing Enter on a physical keyboard moved focus to the first result card instead of searching (only once results were on screen) | Emulator test, then logging the key events | Handle Enter on key release in an `OnKeyListener` |
| 7 | Sprint 4 | Add Food was cramped in landscape: meal chips and preview hard to reach | Emulator screenshot in landscape | Added a landscape layout where the Add button scrolls with the form |
| 8 | Sprint 4 | Undo snackbar disappeared after 2.75 s, too short to react | Emulator test timing | Snackbar now stays 5 s |
| 9 | Sprint 5 | Progress chart's goal label overlapped a tall over-goal bar | Emulator screenshot | Label moved left with a solid background |
| 10 | Sprint 5 | Scanner vibration constant only exists on Android 11+, but the app supports Android 8+ | Android Lint | Fallback to a standard vibration on older versions |
| 11 | Sprint 5 | Progress summary could read "1 of 1 days" | Android Lint | Plural string resource |
| 12 | Sprint 6 | Diary progress bars had no visible track: at 0% nothing showed, and you couldn't see how much of a goal was left | Screenshots from the screen-size runs | Set the track colour so the empty bar shows in light and dark mode |

**Other improvements made during testing:** the diary list was changed from `notifyDataSetChanged()` to `ListAdapter` + `DiffUtil` (recommended practice; smooth delete/Undo animations and tells the swipe handler exactly which row changed).

---

## 9. Testing challenges (test harness issues, not app defects)

Several failures turned out to be problems with the test scripts rather than the app. They were investigated (screenshots, logging) before being ruled out:

- **Emulator keyboard toolbar:** the emulator shows a floating keyboard toolbar on the left of the screen that covered the Breakfast chip, so taps landed on the keyboard. Scripts now tap the right-hand side of controls.
- **Edge back gesture:** a swipe starting within about 80 px of the screen edge is treated by Android as "Back", which closed the app mid-test. Swipes now start further in.
- **Simulated swipe speed:** a 150 ms simulated swipe gives Android only one or two movement updates, so swipe-to-delete sometimes didn't trigger. Logging showed the row being moved and released just short of the threshold. A 400 ms swipe, closer to a real thumb, deleted the row 8 times out of 8, and the Espresso test `DiaryScreenTest.swipeAgainAfterUndo` confirms swiping works repeatedly after Undo.
- **Screen-reading speed:** each `uiautomator` screen read takes about 1 s, so short-lived things (the spinner, the snackbar) were sometimes missed. The spinner is now checked on a slowed network, and the snackbar is read once and Undo tapped from that same read.
- **Live data:** the emulator scripts rely on particular Open Food Facts products appearing in search results, so changes to that live data can cause failures that aren't bugs. The instrumented search tests avoid this by using a fake server.
- **Stale results (Sprint 6):** the first screen-size run reported results that were left over from an earlier run, because the script didn't set JDK 17 and the tests never started. This was spotted because only 1 test was reported instead of 32. The script now sets JDK 17, deletes old results before each run, and warns if no results are produced.

---

## 10. Not yet tested

| Area | Why | Plan |
|---|---|---|
| Scanning a real barcode with the camera | The emulator has no real camera | Test on a physical Android phone |
| Android versions other than 16 | Only one emulator image installed | Install an Android 8 or 9 emulator image and run the instrumented tests |
| Real small phones and tablets | Screen sizes were tested by resizing one emulator, not on real devices | Optional: run on a real device if available |
| Screen reader (TalkBack) | Not yet checked | Manual check |

---

## 11. Sprint 6 testing log

| Date | Work | Result |
|---|---|---|
| 16 Nov 2026 | Copied the emulator check scripts into the project (`testing/emulator/`) with portable paths and a `run_all.sh` | All four scripts pass from their new location |
| 16 Nov 2026 | Made the project 100% Java: removed the Kotlin plugin and the two Kotlin template tests, replaced the instrumented one with `AppContextTest.java` | App builds; 38 unit tests pass |
| 16 Nov 2026 | Added Room DAO tests (`FoodLogDaoTest`) | 5 / 5 pass |
| 16 Nov 2026 | Added Espresso UI tests for search (with a fake server), Add Food, diary, settings and progress | 26 / 26 pass on first run |
| 16 Nov 2026 | Mutation check: broke the server-error message on purpose | The matching test failed as expected; code restored |
| 16 Nov 2026 | Ran the instrumented suite on a small phone, a tablet and 130% text | 32 / 32 in each; found invisible progress-bar tracks (defect 12) and wrapping macro labels at large text |
| 16 Nov 2026 | Fixed defect 12 | Tracks visible in light and dark mode |
| 16 Nov 2026 | Final full run of every suite | 358 / 358 checks pass (section 3) |
