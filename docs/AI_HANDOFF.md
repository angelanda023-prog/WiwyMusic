# WiwyMusic — project context and durable AI handoff

Last updated: 2026-08-01 (America/Mexico_City)

This is the single source of truth for any editor, IDE assistant, or AI working on WiwyMusic.
Read it completely before inspecting or changing code. Update it after every meaningful change,
build, deployment, migration, or release. Do not create competing project-memory documents.

## Current source state

- Android repository: `/Users/wiwyzho/Documents/Web/WiwyMusic`.
- Android branch: `main`.
- Current Android source commit: `7385eed` (`feat: check OTA while app is active`).
- Previous hardening commit: `654626c` (`security: hide OTA repository details`).
- Admin repository: `/Users/wiwyzho/Documents/Web/WiwyMusic-Admin`.
- Admin OTA source commit: `2ecffc0` (`feat: serve OTA files through Worker`).
- Production APK remains `v1.0.41`; commits after `474de8f` are not published in an Android OTA.
- The first unpublished bridge release must use a new version, expected next values:
  `versionName` `1.0.42`, `versionCode` `43`, tag `v1.0.42`.

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
- Production release: `v1.0.41`.
- `versionName`: `1.0.41`.
- `versionCode`: `42`.
- Production commit: `474de8f` (`build: prepare v1.0.41`).
- GitHub release: <https://github.com/angelanda023-prog/WiwyMusic/releases/tag/v1.0.41>
- OTA asset name: `WiwyMusic.apk`.
- Production APK SHA-256: `d1a6c76cf0353a345c67d776bad903333f31d5970f330ace4a9efed74419f927`.
- Previous production baseline: `v1.0.40`, commit
  `3fcfee0`.

The currently installed production APK still reads stable releases from GitHub. A hardened
updater migration is staged locally (see the section below); it must first be delivered once
through the existing GitHub channel before later OTAs can use the repository-neutral endpoint.

## APK hardening and rollback baseline — staged after v1.0.41

Rollback is fixed to the unchanged production release:

- Tag: `v1.0.41`.
- Commit: `474de8f`.
- APK SHA-256: `d1a6c76cf0353a345c67d776bad903333f31d5970f330ace4a9efed74419f927`.
- An exact copy is stored privately in R2 as `ota/WiwyMusic.apk`.
- R2 metadata is stored as `ota/releases.json`.
- Public repository-neutral endpoints:
  - `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/releases`
  - `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/download`
- OTA bridge Worker version: `8ac43778-7375-4fe4-b3a7-f71becf31d79`.
- Admin source commit: `2ecffc0` (`feat: serve OTA files through Worker`).

Staged Android hardening:

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

Verification of the staged APK:

- `compileUniversalDebugKotlin`: successful.
- `testUniversalDebugUnitTest`: successful.
- `assembleUniversalRelease` with R8: successful.
- APK Signature Scheme v2: valid, one signer.
- No `angelanda023-prog`, own GitHub repository URL, or local Git commit was found in DEX.
- Local staged APK SHA-256: `bd3b122b94c3851ef3ac1561a603fee77d31266528d109f4aed1112d2b35bb29`.
- That APK was built from the hardening state before commit `7385eed`; it proves repository
  removal but does not contain the later 180-minute foreground cadence.
- No final release APK containing commit `7385eed` exists yet. The next release must be rebuilt
  after bumping the version. Do not publish the old local APK as a new OTA.

Migration rule: the first hardened release must also be uploaded to the existing GitHub
release channel so installed `v1.0.41` clients can discover it. After those clients update,
future stable releases are published by uploading the signed APK and updated
`WiwyMusic-Admin/ota/releases.json` to the two R2 object keys above. Never replace metadata
before the corresponding APK upload completes and its remote SHA-256 is verified.

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

### First repository-hiding bridge release

Installed `v1.0.41` clients still discover updates through GitHub. Therefore the first hardened
release must exist in both R2 and GitHub. Publish in this exact order:

1. Increment `versionCode` and `versionName` in `app/build.gradle.kts`; expected next values are
   `43` and `1.0.42`.
2. Run debug compilation, unit tests, and `git diff --check`.
3. Confirm protected player files are unchanged.
4. Commit and push the final source.
5. Build the signed universal release with `--no-configuration-cache`.
6. Verify version, signature, and SHA-256. Inspect DEX again for the private repository owner,
   private repository URL, and local commit.
7. Upload the APK to remote R2 key `ota/WiwyMusic.apk` using `--remote`.
8. Update `WiwyMusic-Admin/ota/releases.json` with the new version and only the generic
   improvements message; upload it to remote R2 key `ota/releases.json`.
9. Download `/api/ota/download` and verify its SHA-256 matches the local APK. Verify
   `/api/ota/releases` returns the new version.
10. Create the GitHub bridge release `v1.0.42`, asset name exactly `WiwyMusic.apk`, generic
    release text, and mark it Latest. This one GitHub release lets existing clients migrate.
11. Verify the GitHub asset SHA-256 also matches.

Never update R2 metadata before the matching APK is uploaded and verified; otherwise clients
may discover a version whose file is missing or stale.

### Later stable releases

After the bridge is installed, stable clients use Cloudflare only. Repeat steps 1–9 above.
GitHub is no longer part of the in-app OTA lookup or download path. The Android client checks
on each transition to foreground, deduplicates startup checks within 10 seconds, repeats every
180 minutes while foregrounded, and retains the six-hour background WorkManager fallback.

R2 operational commands and rollback digest are also recorded in:
`/Users/wiwyzho/Documents/Web/WiwyMusic-Admin/ota/README.md`.

Publishing a public APK is an external disclosure. Obtain explicit user authorization before
uploading a new R2 APK, changing live R2 metadata, or creating a GitHub Release.

## Codebase graph

Prefer codebase-memory MCP for code discovery. Project name:

`Users-wiwyzho-Documents-Web-WiwyMusic`

After meaningful code changes, refresh its index so future agents see current symbols and
relationships.
