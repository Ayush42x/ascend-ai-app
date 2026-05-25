# Ascend AI — Firebase Auth Setup Guide

## Prerequisites
- Android Studio Hedgehog or later
- Firebase account
- Google Cloud Console access

---

## Step 1 — Create Firebase Project

1. Go to https://console.firebase.google.com
2. Click **Add project** → name it `ascend-ai`
3. Disable Google Analytics (optional) → **Create project**
4. Click **Add app** → select **Android**
5. Enter package name: `com.ascendai`
6. Download `google-services.json`
7. Place it in `app/` directory of your project

---

## Step 2 — Enable Authentication Methods

In Firebase Console → **Authentication** → **Sign-in method**:

1. Enable **Email/Password**
2. Enable **Google**
   - Set project support email
   - Note your **Web client ID** — you need it in LoginScreen.kt

---

## Step 3 — Enable Firestore

In Firebase Console → **Firestore Database**:

1. Click **Create database**
2. Choose **Start in test mode** (change rules before production)
3. Select a region closest to your users

Paste these security rules in **Rules** tab:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /goals/{goalId} {
      allow read, write: if request.auth != null &&
        request.auth.uid == resource.data.userId;
    }
    match /tasks/{taskId} {
      allow read, write: if request.auth != null &&
        request.auth.uid == resource.data.userId;
    }
  }
}
```

---

## Step 4 — Get SHA-1 Fingerprint (required for Google Sign-In)

Run in terminal from your project root:
```bash
./gradlew signingReport
```

Copy the `SHA-1` under `debug` variant.

In Firebase Console → **Project settings** → **Your apps** → **Add fingerprint** → paste SHA-1.

---

## Step 5 — Update Web Client ID in LoginScreen.kt

In `LoginScreen.kt`, find:
```kotlin
.requestIdToken("YOUR_WEB_CLIENT_ID")
```

Replace with your Web Client ID from:
Firebase Console → **Project settings** → **General** → scroll down to **Web API Key** section.

The Web Client ID is under **OAuth 2.0 client IDs** in Google Cloud Console → it ends in `.apps.googleusercontent.com`.

---

## Step 6 — Plug Files Into Project

### File placement map:

```
app/src/main/java/com/ascendai/
│
├── AscendApp.kt                          ← from AppAndMainActivity.kt
├── MainActivity.kt                       ← from AppAndMainActivity.kt
│
├── domain/
│   ├── model/
│   │   └── AuthModels.kt
│   └── repository/
│       └── IAuthRepository.kt
│
├── data/
│   └── repository/
│       └── AuthRepositoryImpl.kt
│
├── viewmodel/
│   └── AuthViewModel.kt
│
├── di/
│   └── HiltModules.kt
│
└── ui/
    ├── theme/
    │   └── Theme.kt
    ├── components/
    │   └── AuthComponents.kt
    ├── navigation/
    │   └── NavGraph.kt
    └── screens/
        ├── SplashScreen.kt
        ├── LoginScreen.kt
        ├── SignUpScreen.kt
        └── ForgotPasswordScreen.kt
```

---

## Step 7 — AndroidManifest.xml

Add internet permission and configure Google Sign-In activity:
```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET"/>

    <application
        android:name=".AscendApp"
        android:theme="@style/Theme.AscendAI"
        ...>

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

    </application>
</manifest>
```

---

## Step 8 — Typography (add to theme package)

Create `Type.kt` in `ui/theme/`:
```kotlin
package com.ascendai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AscendTypography = Typography(
    displayLarge  = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold,     letterSpacing = (-1).sp),
    headlineMedium= TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    titleMedium   = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyMedium    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,   lineHeight = 22.sp),
    labelSmall    = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,   letterSpacing = 0.04.sp)
)
```

---

## Step 9 — Run the App

```bash
./gradlew assembleDebug
```
Or press **Run ▶** in Android Studio.

---

## Auth Flow Summary

```
App launch
    │
    ▼
SplashScreen (2s)
    │
    ├── sessionUser != null ──► Dashboard
    │
    └── not signed in ─────► LoginScreen
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
               Email/pwd    Google SSO    Sign up link
                    │             │             │
                    └──────┬──────┘             │
                           ▼                   ▼
                    Firebase Auth         SignUpScreen
                           │                   │
                           ▼                   ▼
                    AuthRepository       Firebase Auth
                           │                   │
                           ▼                   ▼
                    Firestore doc      Firestore doc created
                           │                   │
                           └──────┬────────────┘
                                  ▼
                            Dashboard
```

---

## Session Persistence

Firebase Auth handles session persistence automatically using encrypted SharedPreferences. `currentUserFlow` in `AuthRepositoryImpl` uses `AuthStateListener` — the user stays signed in across app restarts until they explicitly sign out.

No extra work needed. The `sessionUser` StateFlow in `AuthViewModel` drives the nav graph root on every cold start.

---

## What to Build Next

Run: **"Generate Dashboard Screen"** or **"Generate AI Planner Engine"**
