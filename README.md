# Fridge Tracker

Portal inventory app + phone web view for receipt scanning and a personal
photo library, all backed by one shared Supabase account.

## What's here

```
fridge-tracker/
├── supabase/
│   └── schema.sql          <- run this in your Supabase project's SQL editor
├── portal-app/              <- Android app that runs on the Portal
│   └── app/src/main/java/com/fridgetracker/app/
│       ├── MainActivity.kt
│       ├── LoginScreen.kt        <- one-time household sign-in
│       ├── InventoryScreen.kt    <- the UI you'll actually see
│       ├── InventoryViewModel.kt
│       ├── InventoryItem.kt
│       ├── CategoryVisuals.kt
│       ├── DesignTokens.kt
│       ├── Type.kt
│       └── SupabaseClient.kt
└── web-client/               <- phone-accessible view: fridge, scan, photo library
    ├── login.html
    ├── index.html             <- fridge inventory
    ├── scan.html              <- receipt photo -> AI extraction -> review -> add
    ├── photos.html             <- your labeled photo library
    ├── styles.css
    ├── config.js
    ├── vercel.json
    └── api/
        ├── extract-receipt.js  <- Vercel function, calls GPT-4o vision
        └── match-photo.js      <- Vercel function, AI-assisted photo matching
```

## Setup

### 1. Supabase project
1. Run `supabase/schema.sql` in the SQL editor — it's safe to re-run, every
   statement uses `if not exists` / `on conflict do nothing`.
2. **Create the shared household account** — Authentication > Users > Add
   user, pick an email and password. This is the ONE account both the Portal
   and the web app sign into. (Anonymous Sign-Ins is no longer used — we
   moved off it so both devices share the same data. You can leave it enabled
   or disable it in Authentication > Providers, doesn't matter either way.)
3. Grab your Project URL and anon public key from Settings > API.

### 2. Wire up credentials
Both need the same URL/key:
- `portal-app/app/src/main/java/com/fridgetracker/app/SupabaseClient.kt`
- `web-client/config.js`

### 3. OpenAI API key (for receipt scanning)
Deploy `web-client/` to Vercel, then in the Vercel project settings add an
environment variable:
```
OPENAI_API_KEY=sk-...
```
Never put this key in any file in this repo — the two `api/*.js` functions
read it server-side only.

### 4. Deploy the web client
```
cd web-client
vercel deploy
```
(or connect the folder to Vercel via their dashboard/GitHub integration —
same pattern as your Lucky Cup deployment.)

### 5. Build tooling for the Portal app — two options

**Option A: Android Studio** (easiest, but a large download)

**Option B: Command-line only** (smaller footprint)
```bash
brew install openjdk@17
brew install --cask android-commandlinetools
sdkmanager "platform-tools" "platforms;android-29" "build-tools;34.0.0"
brew install gradle
```
Add to `~/.zshrc`:
```bash
export ANDROID_HOME=$(brew --prefix)/share/android-commandlinetools
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin
```
From inside `portal-app/`, one-time only:
```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
gradle wrapper --gradle-version 8.7
```

### 6. Build and deploy to the Portal
```bash
cd portal-app
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.fridgetracker.app/.MainActivity
```
On first launch you'll see the sign-in screen — use the household account
from step 1. The session persists after that (no need to sign in every time).

## What works right now
- Shared household login (Portal + web) — same inventory on both
- Portal: adaptive grid, category color badges, real photos when matched,
  search, category filter, 52dp touch targets, Portal+ panel spec compliance
- Web: view fridge, scan a receipt (photo -> GPT-4o extraction -> editable
  review -> confirm -> adds to Supabase), manage a personal photo library
  with AI-assisted fuzzy matching to new items
- Deleting an item works from the Portal (icon on each card)

## What's NOT built yet
- Recipe suggestions from current inventory
- Expiry estimation (schema supports it, nothing populates it yet)
- Deduct-on-cook
- Editing/deleting items from the web view (currently view + add only)
- Real Inter font on the Portal (system default in place, swap instructions
  are in `Type.kt`)

## Notes
- `minSdk = 28` / `targetSdk = 29` to match Portal hardware.
- Storage buckets: `item-photos` (public — so photo URLs work directly in
  `<img>` tags and Coil) and `receipts` (private, currently unused but
  reserved for storing the original receipt image per scan).
- The web app's anon key is meant to be public; row-level security is what
  actually restricts access to the household account's own data.
