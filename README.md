# Trip Expense — Android

A native Android expense-splitting app built with **Kotlin** and **Jetpack Compose**. This is a port of the React Native version (`../`) — same Firebase backend, same UI, no React Native dependencies.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (single Activity) |
| Backend | Firebase Auth + Firestore |
| State | ViewModel + StateFlow |
| Image loading | Glide Compose |
| Async | Kotlin Coroutines |
| Build | Gradle (AGP 8.2.2) |

## Features

- **Phone OTP sign-in** with 3-step flow (phone → OTP → name)
- **Google Sign-In** via GMS
- **Groups** — create, list, real-time balance tracking
- **Expenses** — add with EQUAL/EXACT split, edit, delete
- **Balance sheet** — collapsible view of who owes whom
- **Offline support** — Firestore persistence (100 MB cache)
- **Skeleton loading** — pulsing placeholders during data fetch
- **Real-time sync** — Firestore snapshot listeners update UI instantly

## Getting Started

### Prerequisites

- Android Studio Hedgehog+ (2023.1.1)
- JDK 17
- A Firebase project with **Phone Authentication** and **Google Sign-In** enabled
- The app's SHA-1 fingerprint registered in Firebase Console

### Setup

```bash
# 1. Open the project in Android Studio
open android-native/

# 2. Sync Gradle (File → Sync Project with Gradle Files)

# 3. Verify google-services.json exists
#    It should be at: app/src/main/google-services.json
#    (auto-copied from the existing React Native project)

# 4. Run on device
#    Select a device/emulator and click Run (▶)
```

## Project Structure

```
app/src/main/java/com/tripexpense/
├── MainActivity.kt            # Entry point — single Activity with Compose
├── TripExpenseApp.kt          # Firebase persistence initialization
├── navigation/
│   ├── Screen.kt              # All route definitions
│   └── NavGraph.kt            # Auth flow + main bottom-tab navigation
├── data/
│   ├── model/Models.kt        # User, Group, Expense data classes
│   └── repository/
│       ├── AuthRepository.kt        # Firebase Auth operations
│       └── FirestoreRepository.kt   # Firestore CRUD and real-time flows
├── viewmodel/
│   ├── AuthViewModel.kt       # Authentication state management
│   ├── GroupViewModel.kt      # Group list + detail with live data
│   └── ExpenseViewModel.kt    # Expense CRUD operations
├── ui/
│   ├── theme/                 # Colors, typography, spacing, Material theme
│   ├── auth/                  # Splash screen, Login screen
│   ├── groups/                # Dashboard, CreateGroup, GroupDetail
│   ├── expenses/              # AddExpense, EditExpense
│   └── profile/               # Profile view/edit
└── components/                # Reusable Compose components
    ├── Avatar.kt              # User avatar (initials or photo)
    ├── BalanceCard.kt         # Net balance summary card
    ├── BalanceSheet.kt        # Collapsible owes table
    ├── Button.kt              # Primary/secondary/danger/ghost buttons
    ├── EmptyState.kt          # Empty list placeholder
    ├── ExpenseItem.kt         # Expense row in lists
    ├── GroupCard.kt           # Group row in dashboard
    ├── SegmentedControl.kt    # EQUAL/EXACT toggle
    ├── Skeleton.kt            # Loading placeholder animation
    ├── TextInput.kt           # Labeled text field with prefix/error
    └── UnsyncedBadge.kt       # Offline sync indicator
```

## Key Design Decisions

- **Money is stored as `Long` in cents** — avoids floating-point rounding errors. Display divides by 100.
- **Real-time data via `callbackFlow`** — wraps Firestore `addSnapshotListener` into Kotlin `Flow` for reactive Compose UI.
- **Phone OTP uses Firebase `PhoneAuthProvider`** — same provider as the RN version; handles verification ID and auto-retrieval.
- **Amounts are digit-only strings** — user types "1050" which becomes `1050` cents = `$10.50`. No decimal point handling needed.
- **Equal split distributes remainder** — first `n` members get `floor(share) + 1`, rest get `floor(share)`.

## Firebase Setup

The app uses the same Firebase project as the original React Native version. The `google-services.json` was copied from `../android/app/google-services.json`. Make sure these are enabled in Firebase Console:

1. **Authentication** → Sign-in methods → **Phone** (enabled)
2. **Authentication** → Sign-in methods → **Google** (enabled, with correct SHA-1)
3. **Firestore Database** → Create database (start in test mode, then apply rules from `../firestore.rules`)

### Firestore indexes

Required for the app to work:

| Collection | Fields | Type |
|-----------|--------|------|
| `groups` | `members` (array) | `array-contains` |
| `groups/{groupId}/expenses` | `timestamp` (desc) | Ordered |

These are defined in `../firestore.rules` and `../docs/schema.json`.

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Release bundle
./gradlew bundleRelease
```

## Known Limitations

- The member picker/search screen is a stub — it doesn't query Firestore yet
- EXACT split input fields are not fully implemented in the add expense screen
- The offline sync badge exists as a component but isn't wired to the expense list UI
- Cloud Functions for balance calculation are untouched from the RN project
