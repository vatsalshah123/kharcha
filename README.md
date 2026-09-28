# Kharcha — offline multi-currency expense tracker

A small Android app (Kotlin + Jetpack Compose) for tracking travel expenses in a foreign
currency while automatically recording the INR equivalent. Every entry stores **both**
values, frozen at the rate you enter — so later rate changes never rewrite old entries.

Fully offline: no internet permission is requested, and all data is stored in a local
JSON file in the app's private storage on your phone. Nothing leaves the device.

## Features

- Log an expense in any currency (VND, USD, THB, EUR, … or a custom code) with the
  amount you actually paid.
- Enter the exchange rate yourself (e.g. the rate you got when you bought VND cash, or
  today's card rate) — the app has no live rate feed by design, since it works offline.
- The app remembers the last rate you used per currency and pre-fills it next time, so
  you only type it again if it changed.
- Live INR preview as you type.
- Category tags, notes, and date per expense.
- Home screen totals and per-currency filters.
- Edit or delete any entry (long-press or tap the delete icon).

## Building the APK

You need [Android Studio](https://developer.android.com/studio) (which bundles the
Android SDK) — this project cannot be compiled from this chat since the Android
build tools aren't reachable from here.

1. Open the `kharcha` folder in Android Studio (**File → Open**).
2. Let it sync Gradle (first sync downloads dependencies — needs internet once).
3. Plug in your phone (with USB debugging on, or use Android Studio's wireless
   debugging) and click **Run ▶**, or:
4. **Build → Build Bundle(s)/APK(s) → Build APK(s)** to get an installable
   `app-debug.apk` under `app/build/outputs/apk/debug/`. Copy that file to your phone
   and open it to install (you'll need to allow "install unknown apps" for whichever
   app you copy it with).

### Alternative: build via GitHub Actions (no Android Studio needed)

A workflow is included at `.github/workflows/build.yml`. If you push this project to a
GitHub repo, it will automatically build a debug APK and attach it to the workflow run
as a downloadable artifact — you can grab it from the **Actions** tab and copy it to
your phone. Trigger it manually from the **Actions** tab too (workflow_dispatch).

## Project layout

```
app/src/main/java/com/kharcha/app/
  model/            Currency + Expense data classes
  data/              ExpenseRepository — local JSON persistence
  ui/screens/        HomeScreen, AddEditExpenseScreen (Compose)
  ui/theme/          Material 3 theme
  MainActivity.kt    Sets up Compose and simple screen switching
```

## Notes

- Minimum Android version: 8.0 (API 26).
- Rates are entered manually on purpose (per your request) — this keeps the app
  100% offline and avoids depending on any currency-rate API or internet access.
- Want auto-fetched rates when online, multiple "trips", CSV export, or charts? Those
  are straightforward additions — just ask.
