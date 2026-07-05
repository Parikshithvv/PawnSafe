# PawnSafe

A fully offline Android app that digitizes pledge and redemption record-keeping for a licensed jewellery pawnbroking business — built for **Sri Nanjundeshwara Jewellers**.

Replaces two physical paper registers (Pledge Book & Redemption Register) with a structured, searchable, offline-first mobile system — no internet dependency, no cloud, full data ownership on-device.

---

## Why This Exists

Small jewellery pawnbrokers in India run entirely on paper ledgers — manual entry, manual interest calculation, no searchability, no backup. PawnSafe digitizes that exact workflow without forcing the shop onto unreliable internet or unfamiliar cloud tools.

---

## Features

- **Pledge & Redemption Registers** — full digital replacement of paper ledgers
- **On-device OCR ticket scanning** — CameraX + ML Kit auto-extracts 14+ fields from scanned pledge tickets
- **Historical interest-rate engine** — date-indexed calculation, zero hardcoded rates, accurate across rate changes over time
- **Customer lookup** — instant phone-number-based search across pledge history
- **Excel export** — 3-sheet report (Pledge Book, Redemption Register, Summary) via Apache POI
- **Overdue detection** — automated background check (365-day threshold) with local notifications
- **WhatsApp receipt sharing** — auto-generated receipt image, shared directly
- **PIN + biometric lock** — app-level security with lockout after repeated failed attempts
- **Dashboard** — Vico charts for pledge/revenue trends + Gemini AI-generated insights
- **Built-in calculator tab**

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | Clean Architecture (Data → Domain → UI) + MVVM + MVI-style UIState |
| DI | Hilt |
| Local DB | Room (SQLite) |
| Camera | CameraX |
| OCR | ML Kit Text Recognition (on-device) |
| Excel Export | Apache POI |
| Background Work | WorkManager |
| Async | Kotlin Coroutines + Flow + StateFlow |
| Charts | Vico |
| AI Insights | Gemini API |

---

## Architecture

```
presentation/   → Compose UI, ViewModels, UIState
domain/         → Use cases, domain models, repository interfaces
data/           → Room DB, DAOs, entities, mappers, repository impls
core/utils/     → Interest calculator, OCR helper, Excel exporter, date utils
worker/         → WorkManager background jobs (overdue detection)
```

Room entities never leak past the `data` layer — domain and UI only ever see clean domain models.

---

## Interest Calculation

```
rate     = getRateForDate(pledgeDate)     // historical lookup, never hardcoded
interest = principal × (rate / 100) ÷ 30 × numberOfDays
total    = principal + interest
```

---

## Roadmap

Currently offline-first, single-device. Schema already includes forward-compatible fields (`shopId`, `customerId`, `isSynced`) for a planned Phase 2:

- REST backend (Ktor) + PostgreSQL
- Multi-device sync (Android + Web)
- JWT-based auth
- Multi-shop support

---

## Setup

1. Clone the repo
2. Add your Gemini API key to `local.properties`:
   ```
   GEMINI_API_KEY=your_key_here
   ```
3. Build:
   ```
   ./gradlew assembleDebug
   ```

---

## Author

**Parikshith V**
[GitHub](https://github.com/Parikshithvv) · Android Developer (Kotlin, Jetpack Compose)
