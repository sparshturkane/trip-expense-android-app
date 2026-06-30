# Build Plan — Trip Expense (Android Native)

## Overview

This is a native Android port of the React Native `Trip Expense` app. It is written in **Kotlin** using **Jetpack Compose** with **Material 3** design, **Firebase** backend, and **Navigation Compose** for routing.

### Why this port exists

The original React Native build was difficult to debug on device. This native Android project eliminates the Metro bundler, Hermes, and RN bridge overhead, replacing them with standard Android tooling (Gradle, Kotlin compiler, Android Studio debugger).

---

## Architecture

### Layer diagram

```
UI Layer (Compose Screens)
    ↕ StateFlow.collectAsState()
ViewModel Layer (AuthViewModel, GroupViewModel, ExpenseViewModel)
    ↕ suspend functions / Flow
Repository Layer (AuthRepository, FirestoreRepository)
    ↕ Firebase SDK
Firebase (Auth, Firestore, Cloud Functions)
```

### Key decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| UI toolkit | Jetpack Compose | Declarative UI matches React Native mental model; less boilerplate than XML |
| Navigation | Navigation Compose | Single-Activity architecture; type-safe routes via sealed class |
| State management | ViewModel + StateFlow | Lifecycle-aware; standard Android MVVM |
| Real-time data | Firestore `addSnapshotListener` → `callbackFlow` | Same reactive pattern as RN's `onSnapshot` |
| Monetary values | `Long` in cents | Avoids floating-point rounding; matches RN exactly |
| DI | Manual constructor injection | Simple enough for this app size; Hilt/Koin would add overhead |
| Image loading | Glide Compose | Industry standard; handles caching, transformations |
| Skeleton loading | Custom alpha animation | Lightweight; no extra library dependency |
| Phone Auth | Firebase PhoneAuthProvider | Same provider as RN; native callback API |
| Google Sign-In | Credential Manager / GMS | Uses `google-services.json` from RN project |

---

## File Structure

```
app/src/main/java/com/tripexpense/
├── MainActivity.kt              # Single Activity host, sets Compose content
├── TripExpenseApp.kt            # Application class — Firebase persistence init
├── navigation/
│   ├── Screen.kt                # Route sealed class with argument helpers
│   └── NavGraph.kt              # Auth nav (Splash→Login) + Main nav (bottom tabs)
├── data/
│   ├── model/Models.kt          # User, Group, Expense, SplitStrategy data classes
│   └── repository/
│       ├── AuthRepository.kt    # OTP send/verify, Google sign-in, profile fetch
│       └── FirestoreRepository.kt # Groups & expenses CRUD, real-time flows
├── viewmodel/
│   ├── AuthViewModel.kt         # Auth state, listenToAuth, OTP flow helpers
│   ├── GroupViewModel.kt        # Group list + detail with real-time subscriptions
│   └── ExpenseViewModel.kt      # Add/update/delete expense
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Exact palette from RN (tint=#34C759, etc.)
│   │   ├── Type.kt              # Typography matching RN font sizes
│   │   ├── Dimens.kt            # Spacing, Radius, AppSize objects
│   │   └── Theme.kt             # Material 3 light color scheme
│   ├── auth/
│   │   ├── SplashScreen.kt      # Logo + spinner, auto-navigates to Login
│   │   └── LoginScreen.kt       # 3-step flow: phone → OTP → name (or Google)
│   ├── groups/
│   │   ├── DashboardScreen.kt   # Group list, BalanceCard, FAB, skeleton loading
│   │   ├── CreateGroupScreen.kt # Name input + member picker (stub)
│   │   └── GroupDetailScreen.kt # Expense list, BalanceSheet, FAB
│   ├── expenses/
│   │   ├── AddExpenseScreen.kt  # Description, amount, paidBy, split strategy
│   │   └── EditExpenseScreen.kt # Edit + delete with confirmation dialog
│   └── profile/
│       └── ProfileScreen.kt     # Avatar, name editing, sign-out
└── components/
    ├── Avatar.kt                # Initials circle or photo with Glide
    ├── BalanceCard.kt           # Net balance display card
    ├── BalanceSheet.kt          # Collapsible owes table
    ├── Button.kt                # Primary/secondary/danger/ghost variants
    ├── EmptyState.kt            # Centered icon + title + action
    ├── ExpenseItem.kt           # Row with avatar, description, amount, share
    ├── GroupCard.kt             # Row with avatar, name, balance, member count
    ├── SegmentedControl.kt      # EQUAL/EXACT toggle
    ├── Skeleton.kt              # Pulsing placeholder view
    ├── TextInput.kt             # Labeled input with prefix, error state
    └── UnsyncedBadge.kt         # Yellow "Unsynced" pill for pending writes
```

---

## Data Flow

### Authentication

```
App launch
  → TripExpenseApp.onCreate (Firestore persistence 100MB)
  → MainActivity.setContent { NavGraph() }
  → NavGraph creates AuthViewModel
  → AuthViewModel.init → listenToAuth()
      → authStateFlow() collects FirebaseAuth state
      → on each auth change: fetches user profile from Firestore
      → emits AuthState(user, isLoading, authInitialized)
  → NavGraph reads authState:
      if !authInitialized || isLoading → SplashScreen
      if user == null → AuthNavHost (Splash → Login)
      if user != null → MainNavHost (bottom tabs)
```

### Real-time group data

```
DashboardScreen mounts
  → LaunchedEffect(userId) → groupViewModel.subscribeToGroups(userId)
  → GroupViewModel calls firestoreRepo.groupsFlow(userId)
  → callbackFlow wraps Firestore `whereArrayContains("members", userId)` snapshot
  → Each snapshot: maps docs → Group → GroupWithMeta (computes userBalance)
  → Emits GroupListState → DashboardScreen recomposes
```

### Offline support

- Firestore persistence enabled at app start (100 MB cache)
- `UnsyncedBadge` indicator for pending writes (tracked via `hasPendingWrites`)
- Network can be toggled via `FirestoreRepository.goOnline()` / `goOffline()`

---

## Firebase Data Model

### `users/{uid}`

| Field | Type | Description |
|-------|------|-------------|
| `uid` | String | Auth UID |
| `name` | String | Display name |
| `email` | String | Email |
| `phone` | String | Phone number |
| `photoURL` | String? | Photo URL |

### `groups/{groupId}`

| Field | Type | Description |
|-------|------|-------------|
| `groupId` | String | Doc ID (stored redundantly) |
| `name` | String | Group name |
| `members` | List\<String\> | Member UIDs |
| `balances` | Map\<String, Long\> | `"userA_userB" → amountInCents` |
| `createdAt` | Timestamp | Creation time |
| `createdBy` | String | Creator UID |

### `groups/{groupId}/expenses/{expenseId}`

| Field | Type | Description |
|-------|------|-------------|
| `expenseId` | String | Doc ID |
| `groupId` | String | Parent group ID |
| `description` | String | Expense description |
| `amount` | Long | Amount in cents |
| `paidBy` | String | Payer UID |
| `splitStrategy` | String | `"EQUAL"` or `"EXACT"` |
| `owedBy` | Map\<String, Long\> | `uid → amountOwedInCents` |
| `timestamp` | Timestamp | Created |
| `updatedAt` | Timestamp | Last updated |

---

## Dependencies (Gradle)

```kotlin
// Compose BOM 2024.02.00
// Firebase BOM 33.0.0
// Navigation Compose 2.7.7
// Lifecycle ViewModel Compose 2.7.0
// Glide Compose 1.0.0-beta01
// Google Play Services Auth 21.0.0
// Kotlin Coroutines Play Services 1.7.3
// Shimmer (Facebook) 0.5.0
```

---

## Known Gaps vs. RN version

| Feature | RN Status | Android Status | Notes |
|---------|-----------|----------------|-------|
| Phone OTP login | ✅ | ✅ | 3-step flow implemented |
| Google Sign-In | ✅ | ✅ | Uses GMS + Credential Manager |
| Group list | ✅ | ✅ | Real-time with `array-contains` |
| Create group | ✅ | ✅ | Member picker is stub (no search) |
| Group detail | ✅ | ✅ | Expenses listed, BalanceSheet |
| Add expense | ✅ | ✅ | EQUAL split only; EXACT needs UI |
| Edit expense | ✅ | ✅ | Saves description + amount |
| Delete expense | ✅ | ✅ | Confirmation dialog |
| Profile editing | ✅ | ✅ | Name edit inline |
| Sign out | ✅ | ✅ | Auth + Google sign-out |
| Skeleton loading | ✅ | ✅ | Custom alpha animation |
| BalanceSheet | ✅ | ✅ | Collapsible, parses balance map |
| Offline badge | ✅ | ⚠️ | Logic exists but not wired to UI |
| Equal split remainder | ✅ | ✅ | First n members +1 cent |
| Exact split | ✅ | ❌ | UI needs member input fields |
| Member search | ✅ | ❌ | Search query is stub |
| Cloud Function | ✅ | ⚠️ | Same JS function, untouched |
| User name editing | ✅ | ✅ | Inline edit with save/cancel |

---

## Build & Run

```bash
# Open in Android Studio:
open android-native/

# Or build from command line:
cd android-native
./gradlew assembleDebug

# Install on device:
./gradlew installDebug
```

### Prerequisites

1. Android Studio Hedgehog (2023.1.1) or later
2. JDK 17
3. A Firebase project with **Phone Auth** and **Google Sign-In** enabled
4. `google-services.json` in `app/src/main/` (copied from RN project)
5. App's **SHA-1 fingerprint** added to Firebase Console

### Configuration

- `google-services.json` is already present (copied from the existing RN project)
- No environment variables needed — Firebase config is handled by `google-services.json` and `build.gradle.kts`
- If Google Sign-In fails, verify the `webClientId` in Firebase Console matches the one in `google-services.json`
