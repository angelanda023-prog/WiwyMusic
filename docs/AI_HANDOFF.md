# WiwyMusic — project context and durable AI handoff

Last updated: 2026-08-01 (America/Mexico_City)

This is the single source of truth for any editor, IDE assistant, or AI working on WiwyMusic.
Read it completely before inspecting or changing code. Update it after every meaningful change,
build, deployment, migration, or release. Do not create competing project-memory documents.

## Current source state

- Android repository: `/Users/wiwyzho/Documents/Web/WiwyMusic`.
- Android branch: `main`.
- Current Android release source commit: `dedacc2` (`perf: stream onboarding artist results`).
- Previous hardening commit: `654626c` (`security: hide OTA repository details`).
- Admin repository: `/Users/wiwyzho/Documents/Web/WiwyMusic-Admin`.
- Admin OTA metadata commit: `e84c2c5` (`chore: publish OTA metadata for v1.0.42`).
- Admin OTA documentation commit: `273c032` (`docs: record v1.0.42 OTA artifacts`).
- Production APK is `v1.0.42`, `versionCode` 43. The repository-hiding bridge is complete.

## Artist onboarding performance — published in v1.0.42

File: `app/src/main/kotlin/com/wiwymusic/ui/screens/auth/WiwyOnboardingScreen.kt`.

- Initial artist discovery no longer blocks on all 12 genre searches.
- Results render progressively as each request finishes.
- At most three InnerTube artist searches run concurrently.
- Each initial or manual artist search has a five-second timeout.
- Completed discovery results are reused while the application process remains alive.
- Duplicate artist IDs are merged while preserving the progressively received order.
- Artist selection limits, Supabase persistence, login flow, and onboarding completion behavior
  remain unchanged.
- Debug compilation, unit tests, and the signed R8 release build passed.
- No protected mini-player/player file was modified.

## Premium redeem codes — published in v1.0.40

Placement follow-up published in v1.0.41: root Settings no longer renders `RedeemCodeCard`;
only Account keeps the redemption card. This cleanup does not change redemption logic or the
admin panel.

- Android shows one reusable `RedeemCodeCard` only in Account.
- The card requires a Supabase login, calls `redeem_premium_code`, refreshes
  `profiles.is_premium`, and shows a code-native confetti celebration with total Premium
  days remaining.
- Admin supports only 7, 15, 30, 60, 90, 180, and 365-day codes. A code may be normal
  (one total redemption) or unlimited (many users, once per user), and active unlimited
  codes can be revoked.
- Blank admin code input generates a random `WIWY-*` code; custom codes remain supported.
- Existing Premium days are extended. Lifetime Premium is never replaced by a timed code.
- Applied production migration:
  `/Users/wiwyzho/Documents/Web/WiwyMusic-Admin/supabase/migrations/0003_unlimited_redeem_codes.sql`.
- Premium-code support was deployed in historical Admin Worker version
  `9e8c7604-ad91-4948-b43b-ac124c8b95fa`; the current Worker version is recorded in the
  hardening section below.
- Android feature was introduced in OTA `v1.0.40`; placement cleanup is published in
  `v1.0.41` (`versionCode` 42).
- Protected mini-player/player files were not modified.

## Current production state

- Android app written in Kotlin and Jetpack Compose with Material Design 3.
- Application ID: `com.wiwymusic`.
- Production release: `v1.0.42`.
- `versionName`: `1.0.42`.
- `versionCode`: `43`.
- Production commit: `dedacc2` (`perf: stream onboarding artist results`).
- GitHub bridge release: <https://github.com/angelanda023-prog/WiwyMusic/releases/tag/v1.0.42>
- OTA asset name: `WiwyMusic.apk`.
- Production APK SHA-256: `7b186d299f85393dbca89a9c4fd68ed001e976f702fb75bf6f271c312214a25e`.
- Previous production baseline: `v1.0.41`, commit `474de8f`, SHA-256
  `d1a6c76cf0353a345c67d776bad903333f31d5970f330ace4a9efed74419f927`.

The bridge release was published through both GitHub and R2. Clients upgrading from `v1.0.41`
can discover `v1.0.42` through the old GitHub updater; after installation, stable update checks
and downloads use the repository-neutral Cloudflare endpoint.

## APK hardening and rollback baseline — published in v1.0.42

Rollback is fixed to the unchanged production release:

- Tag: `v1.0.41`.
- Commit: `474de8f`.
- APK SHA-256: `d1a6c76cf0353a345c67d776bad903333f31d5970f330ace4a9efed74419f927`.
- An exact rollback copy is stored privately in R2 as
  `ota/archive/v1.0.41/WiwyMusic.apk`.
- The current `v1.0.42` copy is archived as `ota/archive/v1.0.42/WiwyMusic.apk` and served from
  `ota/WiwyMusic.apk`.
- R2 metadata `ota/releases.json` points to `v1.0.42` with the generic improvements message.
- Public repository-neutral endpoints:
  - `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/releases`
  - `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/download`
- OTA bridge Worker version: `8ac43778-7375-4fe4-b3a7-f71becf31d79`.
- Admin source commit: `2ecffc0` (`feat: serve OTA files through Worker`).

Published Android hardening:

- Stable update checks and APK downloads use only the Cloudflare endpoint.
- Update notification checks run when the app process enters the foreground and every
  180 minutes while it remains foregrounded. A 10-second deduplication window prevents the
  existing activity-start check from creating a duplicate request. The foreground loop stops
  when the app leaves the foreground; the existing six-hour WorkManager fallback remains.
- GitHub owner, repository name, release scraping, and commit-history requests were removed
  from runtime code.
- Release notes remain generic and do not reveal implementation details.
- Release `BuildConfig.GIT_COMMIT` is the neutral value `release`; debug builds retain their
  useful local commit suffix.
- R8 code optimization/obfuscation and resource shrinking remain enabled.
- `LASTFM_API_KEY`, `LASTFM_SECRET`, and `TOGETHER_BEARER_TOKEN` were audited without printing
  values and were empty in the production build environment. No playback file was modified.
- Supabase URL and publishable/anonymous key remain in the client by design; authorization is
  enforced by Supabase RLS and RPC policies, not by treating those public values as secrets.

Verification of the published APK:

- `compileUniversalDebugKotlin`: successful.
- `testUniversalDebugUnitTest`: successful.
- `assembleUniversalRelease` with R8: successful.
- APK Signature Scheme v2: valid, one signer.
- No `angelanda023-prog`, own GitHub repository URL, or local Git commit was found in DEX.
- Local, R2, GitHub download, and GitHub asset digest all match SHA-256
  `7b186d299f85393dbca89a9c4fd68ed001e976f702fb75bf6f271c312214a25e`.
- Verified package `com.wiwymusic`, `versionName` `1.0.42`, `versionCode` 43.

Migration is complete. Future stable releases use R2 for in-app discovery and download. Keep
the `v1.0.41` archive object for rollback. Never replace metadata before the corresponding APK
upload completes and its remote SHA-256 is verified.

## User decisions that must be preserved

- Do not use old UI mockups. User requested original, code-native UI work.
- A broad experimental visual redesign was tried and rejected. It was fully reverted before
  `v1.0.34`; do not restore it automatically.
- Maintain current architecture and playback behavior.
- Do not change APIs, databases, services, routes, or playback logic for visual work unless
  strictly required and explicitly authorized.
- Keep Spanish UI wording where supplied by user.

## Mini-player protection

Mini-player must remain exactly unchanged unless user gives explicit authorization after
receiving file, reason, risk, and alternative explanation.

Protected files/components:

- `app/src/main/kotlin/com/wiwymusic/ui/player/MiniPlayer.kt`
- `app/src/main/kotlin/com/wiwymusic/ui/player/MiniPlayerComponents.kt`
- `app/src/main/kotlin/com/wiwymusic/ui/player/Player.kt`
- `app/src/main/kotlin/com/wiwymusic/ui/component/BottomSheet.kt`
- `app/src/main/kotlin/com/wiwymusic/constants/Dimensions.kt`
- `app/src/main/kotlin/com/wiwymusic/ui/theme/Theme.kt`
- `app/src/main/kotlin/com/wiwymusic/ui/theme/Type.kt`
- Playback service, queue, state, and connection logic.

`MainActivity.kt` is sensitive because it hosts root UI and player integration. In `v1.0.34`,
user explicitly authorized changing only the main `TopAppBar` title block. Do not use that
authorization for future changes elsewhere in the file.

If a future change appears to require protected code:

1. Name exact file and component.
2. Explain why modification is necessary.
3. Explain risk to mini-player/playback.
4. Offer an alternative that avoids protected code.
5. Wait for explicit authorization.

## UI state implemented in v1.0.34

### Home

Files:

- `app/src/main/kotlin/com/wiwymusic/MainActivity.kt`
- `app/src/main/kotlin/com/wiwymusic/ui/screens/HomeScreen.kt`
- `app/src/main/res/values-es/strings.xml`

Behavior:

- Main logo and `WiwyMusic` wordmark match Settings: 40 dp logo, 12 dp gap,
  `Wiwy` in `onSurface`, `Music` in orange `#F5791F`, 22 sp extra-bold text.
- `Continuar escuchando` heading is always rendered. Its item grid appears when data is not
  empty.
- `Fijar en marcación rápida` heading is always rendered immediately below it. Its grid
  appears when pinned songs are not empty.
- Spanish resource `keep_listening` is `Continuar escuchando`.
- Spanish resource `pin_to_speed_dial` is `Fijar en marcación rápida`.

### Music recognition

File:

- `app/src/main/kotlin/com/wiwymusic/ui/screens/musicrecognition/MusicRecognitionScreen.kt`

Behavior:

- Uses the shared `WiwySettingsPageHeader`, exactly matching the Account settings header.
- Header contains the 40 dp app logo, `WiwyMusic` wordmark, version/plan, right-side back
  button, and page title `Identificar canción` below the branded row.
- Old `graphic_eq` icon and old `WiwyMusic` header text were removed.
- The duplicate in-content `Identificar canción` title was removed.
- Recognition state machine, microphone permission, audio capture, search navigation, and
  result logic were not changed.

### Unified settings pages in v1.0.35

Single sources of truth:

- `app/src/main/kotlin/com/wiwymusic/ui/screens/settings/WiwySettingsPageComponents.kt`
  - `WiwySettingsPageHeader`: Account-style branded header and title.
  - `WiwySettingsCard`: Account-style 24 dp, `surfaceContainerLow`, zero-elevation card.
- `app/src/main/kotlin/com/wiwymusic/ui/component/Preference.kt`
  - Settings preference cards/groups use the same 24 dp, `surfaceContainerLow`,
    zero-elevation treatment.
- `app/src/main/kotlin/com/wiwymusic/ui/screens/settings/SettingsDimensions.kt`
  - Settings group card radius is 24 dp.

All destinations opened from Settings now use `WiwySettingsPageHeader`, including Account,
Appearance, Player, Content, Privacy, Storage, Notifications, Update, About, Backup,
Changelog, palette/theme tools, integrations, Discord pages, Last.fm, Music Together,
PoToken pages, widget settings, custom background, recognition, debug, Android Auto, and
Always-on display. Each page title is rendered below the branded header row. The root
`SettingsScreen` intentionally keeps its existing Settings search action.

Special cards in Music Recognition, Storage, Update, and About were also normalized to the
Account card surface, 24 dp radius, and zero elevation. Functional inner controls and preview
cards retain their semantics.

Validation completed after these changes:

```bash
./gradlew :app:compileUniversalDebugKotlin
```

Result: `BUILD SUCCESSFUL`. Protected mini-player/player files remained unchanged.

## Audio quality state from v1.0.33

Preserve these four choices and Wi-Fi control:

- Ahorro de datos: 96 kbps.
- Normal: 160 kbps.
- Alta: up to 320 kbps.
- Automática: recommended, adapts to network.
- Additional switch: `Máxima calidad solo con Wi‑Fi`.

## Admin live presence (deployed in v1.0.38)

Android reports authenticated-user presence to the Supabase `app_presence` table through
`AdminPresenceReporter.kt`. It uses a separate read-only `MediaController` client plus the
process lifecycle, so no protected mini-player, playback service, queue, state, or connection
file was modified. The report contains app foreground state, playback state, current song
metadata, and a heartbeat timestamp. `SupabaseAuth.upsertAppPresence` performs the RLS-bound
upsert using the user's own JWT and retries once after token refresh on HTTP 401.

The companion admin project is `/Users/wiwyzho/Documents/Web/WiwyMusic-Admin`. Its Users table,
user detail, and dashboard now show online APK users, current playback, last activity, and
Premium days remaining. Days are derived from `subscriptions.expires_at` and refreshed every
30 seconds, so the count decreases automatically. Migration
`supabase/migrations/0002_app_presence.sql` creates the table, owner-only RLS policies, index,
and Realtime publication entry.

Migration `0002_app_presence.sql` was applied to production Supabase on 2026-07-31. The admin
Worker was deployed at <https://wiwymusic-admin.angelanda023.workers.dev> with Cloudflare
version `f22e09d5-b33a-45bf-b34a-f25c72768d68`. Android reporting was published in OTA
`v1.0.38`; remote asset digest matches the production SHA-256 above.

### Follow-up behavior published in v1.0.39

- Admin Worker version `c67f0ac0-e14c-46e9-9a9d-a71a4ef2a144` treats a fresh
  `is_playing` heartbeat as online even when the APK is in background.
- Online/playing users sort above inactive users; remaining users sort by latest activity.
- Supabase Realtime remains the primary refresh path; a 10-second panel refresh is fallback.
- Android heartbeat interval is 15 seconds for faster background-playing freshness.
- Update screen hides changelog and commit-history entry points.
- Every in-app update prompt uses a generic improvements message and does not expose release
  details. Future GitHub OTA descriptions should also remain generic.
- Android portion was published in OTA `v1.0.39`; asset digest matches the production
  SHA-256 above.

## UI changes in v1.0.36

- `AboutScreen` uses a full-size native `LazyColumn` without duplicated nested-scroll
  connections, allowing every contributor card to remain reachable.
- `WiwyProfileAvatar` is now an accessible button. Tapping any shared header avatar opens
  `AvatarPickerSheet` to take a photo, choose one from the gallery, or select a preset.
- Main navigation destination transitions are defined in `NavigationBuilder`:
  - Settings and Home enter from right to left.
  - Playlist/Library enters from bottom to top.
  - Search enters from top to bottom.
- `MainActivity.kt` and all protected mini-player/player files remain unchanged.
- Validation: `./gradlew :app:compileUniversalDebugKotlin` completed successfully.

### Settings navigation in v1.0.37

- Every destination opened inside Settings now uses the shared `settingsPage` navigation
  transition in `NavigationBuilder.kt`.
- Forward navigation enters from right to left; back navigation reverses toward the right.
- This includes Appearance and its tools, Widget, Content, Player, Storage, Privacy, Backup,
  integrations, Update/Changelog, About, Account, Downloads, Notifications, recognition,
  PoToken, and custom background.
- Validation: `./gradlew :app:compileUniversalDebugKotlin` completed successfully.

## Build and verification

Common validation:

```bash
./gradlew :app:compileUniversalDebugKotlin
./gradlew :app:testUniversalDebugUnitTest
```

Release build:

```bash
./gradlew --no-configuration-cache :app:assembleUniversalRelease
```

Use `--no-configuration-cache` because `app/build.gradle.kts` invokes Git during configuration.
Debug builds use that hash; release builds override `BuildConfig.GIT_COMMIT` with `release`.

Before publishing:

- Run unit tests.
- Confirm `git diff --check` is clean.
- Confirm only intended files changed.
- Confirm protected player files are unchanged.
- Verify APK reports expected `versionName` and `versionCode` with Android `aapt`.
- Verify signature with Android `apksigner`.
- Compute SHA-256.
- Never print signing passwords, tokens, `local.properties`, or `keystore.properties`.

Release APK output:

`app/build/outputs/apk/universal/release/app-universal-release.apk`

## OTA publishing workflow

### Completed repository-hiding bridge

`v1.0.42` was uploaded to R2 first, its remote SHA-256 was verified, and only then was its R2
metadata activated. The same APK was published once through GitHub as the Latest bridge release
for installed `v1.0.41` clients. Both remote assets match the local signed APK.

### Later stable releases

After the bridge is installed, stable clients use Cloudflare only. For every later version:

1. Increment `versionCode` and `versionName`.
2. Compile, test, inspect the diff, and confirm protected files are unchanged.
3. Commit and push source, then build the signed universal release with R8.
4. Verify package version, signature, SHA-256, and absence of private repository strings.
5. Archive the APK under `ota/archive/v<version>/WiwyMusic.apk`.
6. Upload and remotely verify `ota/WiwyMusic.apk`.
7. Only after APK verification, update and upload `ota/releases.json` with generic text.
8. Verify both public OTA endpoints. GitHub is not needed for later in-app OTAs.

The Android client checks on each transition to foreground, deduplicates startup checks within
10 seconds, repeats every 180 minutes while foregrounded, and retains the six-hour background
WorkManager fallback.

Never update R2 metadata before the matching APK is uploaded and verified; otherwise clients
may discover a version whose file is missing or stale.

R2 operational commands and rollback digest are also recorded in:
`/Users/wiwyzho/Documents/Web/WiwyMusic-Admin/ota/README.md`.

Publishing a public APK is an external disclosure. Obtain explicit user authorization before
uploading a new R2 APK, changing live R2 metadata, or creating a GitHub Release.

## Codebase graph

Prefer codebase-memory MCP for code discovery. Project name:

`Users-wiwyzho-Documents-Web-WiwyMusic`

After meaningful code changes, refresh its index so future agents see current symbols and
relationships.
