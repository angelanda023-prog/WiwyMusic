# WiwyMusic — project context and durable AI handoff

Last updated: 2026-08-03 (America/Mexico_City)

This is the single source of truth for any editor, IDE assistant, or AI working on WiwyMusic.
Read it completely before inspecting or changing code. Update it after every meaningful change,
build, deployment, migration, or release. Do not create competing project-memory documents.

## Current source state

- Android repository: `/Users/wiwyzho/Documents/Web/WiwyMusic`.
- Android branch: `main`.
- Current Android release source commit: `db2cae7` (`build: bump version to 1.1.9`).
- Playlist import lock visibility commit: `d70de59` (`fix(library): show playlist import lock`).
- Playlist import Premium gate commit: `1429cc5` (`feat(library): gate playlist import`).
- Settings inset fix commit: `7c595d3` (`fix(settings): keep actions above overlays`).
- Premium player controls commit: `304dcf7` (`feat(player): gate premium controls`).
- Account/player change commit: `ee6ee9a` (`feat: personalize accounts and playback UI`).
- Full-player download shortcut commit: `8756ad9` (`feat(player): add download shortcut`).
- Premium session fix commit: `7385e6b` (`fix: persist premium plan across sessions`).
- Previous hardening commit: `654626c` (`security: hide OTA repository details`).
- Admin repository: `/Users/wiwyzho/Documents/Web/WiwyMusic-Admin`.
- Admin OTA metadata commit: `027f1b1` (`chore: publish OTA metadata for v1.1.9`).
- Admin OTA documentation commit: `c47f6b8` (`docs: record v1.1.9 OTA artifact`, local;
  the admin repository has no Git remote configured).
- Admin OTA archive-routing commit: `9d3d466` (`fix(ota): serve published archive`, local;
  deployed Worker version `91b47c42-f0fa-4479-ad9f-814402d554bf`).
- Admin panel UI commit: `ae42e23` (`feat(admin): improve premium management`, local;
  the admin repository has no Git remote configured).
- Admin APK download button commit: `1520542` (`feat(admin): add APK download button`, local;
  deployed Worker version `94a2fda9-9271-4721-b364-a4075b5c50fb`).
- Admin mobile-native redesign commit: `e080db5` (`feat(admin): redesign mobile dashboard`,
  local; deployed Worker version `f94348ef-c7df-42e0-a9bd-f3721d4a3618`; the admin repository
  has no Git remote configured).
- Production APK is `v1.1.9`, `versionCode` 56, distributed through Cloudflare R2.

## Settings keyboard-aware scrolling — published in v1.1.7

- Every destination registered through `settingsPage` now receives a full-screen
  `imePadding()` container. When Android keyboard opens, available page height shrinks instead
  of allowing keyboard to cover focused controls.
- `AccountSettings` now explicitly fills available screen height while retaining its existing
  vertical scroll. It also consumes the existing player-aware bottom inset, letting user scroll
  Redeem code and Logout above an active mini-player without modifying that player.
- Existing screen-specific `verticalScroll`, `LazyColumn`, and other scrolling containers remain
  unchanged; common wrapper does not add conflicting nested scrolling.
- Modified files: `ui/screens/NavigationBuilder.kt` and
  `ui/screens/settings/AccountSettings.kt`. Mini-player, player, playback, navigation
  transitions, account logic, and code-redemption logic remain unchanged.
- Validation passed: `git diff --check`, `:app:testUniversalDebugUnitTest`, signed R8 release
  build, signature and version checks, private-string scan, immutable R2 verification, and
  public metadata/download verification. Production SHA-256 is
  `87d791b70b95445be264ab03a52dbc7ee8b1fd29340a51df9024e9d3dc9106ce`.
- Rollback tag `snapshot-before-settings-insets-v1.1.6` points to `6fd5765` and is pushed to
  `origin`.

## Playlist import Premium entry — published in v1.1.8

- The `Importar playlist` action in `WiwyLibraryScreen` is now the single Premium gate for the
  backup/import destination. Free accounts see the same small lock badge used by other Premium
  controls; tapping it opens the shared `PremiumFeatureDialog` without navigating.
- Premium accounts continue directly to `settings/backup_restore`.
- `BackupAndRestore` now has one unified full layout. The former Free-only layout and its
  individual Premium cards/locks were removed, so authenticated Premium users do not see
  redundant locks after entering.
- Modified files: `ui/screens/WiwyLibraryScreen.kt` and
  `ui/screens/settings/BackupAndRestore.kt`. No mini-player, full-player, playback, account,
  import, backup, cloud-sync, YouTube Music, or Spotify operation was changed.
- Validation passed: `git diff --check`, `:app:compileUniversalDebugKotlin`,
  `:app:testUniversalDebugUnitTest`, signed R8 release build, signature and version checks,
  private-string scan, immutable R2 verification, and public metadata/download verification.
  Production SHA-256 is
  `036769d3b78b2c4426668faf6ab56d2c52b94b08acf8f1db00ab74ce22f5f769`.
- Rollback tag `snapshot-before-playlist-import-gate-v1.1.7` points to `c3e03b0` and is pushed
  to `origin`.

### Lock-badge visibility follow-up — published in v1.1.9

- The first v1.1.8 layout placed the lock after the long `Importar playlist` label, where it
  could be clipped on narrow cards. `ActionCard` now overlays `PremiumLockBadge` at the card's
  top-end corner, matching the visible badge placement used by Lyrics and Download.
- Premium behavior and navigation are unchanged. Only `WiwyLibraryScreen.kt` is modified; no
  protected player or mini-player file is touched.
- Validation passed: unit tests, signed R8 release build, signature and version checks,
  private-string scan, immutable R2 verification, and public metadata/download verification.
  Production SHA-256 is
  `bd77d6cd0de3f9085dec257aa78342ca52428c6549a0ad90fda3a414034ede22`.
- Rollback tag `snapshot-before-import-lock-visibility-v1.1.8` points to `756df8e` and is pushed
  to `origin`.

## Premium player controls — published in v1.1.6

- On 2026-08-02 the user explicitly authorized a UI-only Premium gate in the protected
  full-player and queue components. This authorization covers only Download, Lyrics, Sleep
  timer, and Equalizer presentation/click handling; it does not authorize playback, queue,
  mini-player, service, dimensions, animations, or player-connection changes.
- Free accounts keep all four controls visible with a small lock badge. Tapping one opens the
  shared localized `PremiumFeatureDialog` and does not execute the underlying action.
- Premium accounts retain the previous Download, Lyrics, Sleep timer, and Equalizer behavior.
  A nullable/loading plan is treated as locked to avoid granting Premium access before the
  profile is confirmed.
- The Download gate is applied to every full-player style in `PlayerTopActions`. Lyrics and
  Sleep timer are gated in the shared `Queue` callbacks and display locks in every applicable
  queue style, including V7/V8 lyrics. Equalizer is gated in the existing player quick-action
  menu.
- Modified protected UI files: `ui/player/PlayerComponents.kt`, `ui/player/Queue.kt`, and
  `ui/player/QueueComponents.kt`. The changes are limited to plan observation, lock overlays,
  and guarded callbacks. Existing download/service commands, lyric sheet callback, sleep timer
  implementation, equalizer dialog, playback controls, and mini-player files remain intact.
- Shared UI lives in `ui/component/PremiumFeatureDialog.kt`; English and Spanish strings were
  added to their existing resource files.
- Published version is `1.1.6`, `versionCode` 53. Debug compilation, unit tests, signed R8
  release build, signature, private-string scan, immutable R2 archive, public metadata, and
  public download verification passed. Production SHA-256 is
  `821e1ed514b70ee9a2c61a170c91710ee39b800ec3a3df88081ad06d14af8cb0`.
- Rollback tag `snapshot-before-premium-controls-v1.1.5` points to `ff988f0` and is pushed to
  `origin`.

## Local playback fix — published in v1.1.3

- On 2026-08-02 the user reported `UnexpectedLoaderException`, source error code `2000`, for
  some Library tracks and explicitly authorized the required protected change in
  `playback/MusicService.kt` after receiving the file, reason, risk, and alternative.
- `createDataSourceFactory()` now bypasses YouTube resolution for both `content://` and
  `file://` sources. This prevents on-device URIs from being interpreted as YouTube IDs.
- `createMediaSourceFactory()` keeps the existing MP4/WebM extractors for online playback and
  uses Media3's full default extractor set for local files, adding MP3, FLAC, OGG, WAV, and
  other supported containers.
- The native Download start/cancel/remove action was removed from `ui/menu/PlayerMenu.kt`
  because it duplicates the full-player shortcut. The optional external-downloader action is
  preserved when configured.
- Validation passed: `git diff --check`, `:app:compileUniversalDebugKotlin`, and
  `:app:testUniversalDebugUnitTest`.
- Published version is `1.1.3`, `versionCode` 50. Signed R8 build, unit tests, signature,
  version, private-string scan, R2 object, and public endpoint verification passed. Production
  SHA-256 is `e127c343c4298e1aae4459611fdb102fd608451a67a996be815fd457f1dfc279`.
- Rollback tag `snapshot-before-local-playback-fix-v1.1.2` points to `ed90c01` and is pushed
  to `origin`. Mini-player files remain untouched.

## Full-player download shortcut — published in v1.1.2

Explicit authorization and rollback:

- On 2026-08-02 the user explicitly authorized changing the full-player action after receiving
  the protected-file warning, then selected the safe alternative: add Download and hide Share.
- Rollback tag `snapshot-before-player-download-button-v1.1.1` points to commit `6dee848` and is
  pushed to `origin`.

Prepared behavior:

- `PlayerTopActions` in `ui/player/PlayerComponents.kt` no longer renders the Share shortcut.
- The same position renders a native Download action in player styles V1 through V6. Styles V7
  and V8 receive Download as an additional action between Favorite and More.
- The shortcut reuses the existing Premium download rules and state: it starts a download,
  cancels a queued/in-progress download, and removes a completed offline download.
- In published v1.1.2, the Download action inside `PlayerMenu` remained available; v1.1.3
  removes that duplicate. Sharing elsewhere in the app is unchanged.
- Published version is `1.1.2`, `versionCode` 49. Debug compilation, unit tests, signed R8
  build, signature verification, archived R2 verification, and public APK/metadata verification
  pass. Production SHA-256 is
  `c3bb075b5e1a013932bf5bfa36c8e3777dcbbb01e30f66dc756db1369a08ad54`.
- Mini-player files, `Player.kt`, playback service, queue, playback behavior, dimensions,
  animations, and full-player connection were not changed.

## Premium/Free plan label and Account layout — published in v1.1.1

Rollback snapshot:

- Git tag: `snapshot-before-plan-label-v1.1.0`.
- Exact commit: `3eefc3b`.
- The tag is pushed to `origin` and restores the documented published `v1.1.0` source state.

- Root Settings and the shared settings-page header always render the authenticated plan beside
  the installed version as exactly `Premium` or `Free`; the label is no longer hidden while the
  nullable profile state finishes loading.
- Opening Settings or Account triggers a fresh profile read for the current Supabase user/token,
  so a previously incomplete startup refresh can recover without restarting the application.
- Account always renders `Premium` or `Free` in the plan area previously adjacent to the logout
  control. The logout button was removed from the account card and placed immediately below the
  `Canjear código` card.
- Published version is `1.1.1`, `versionCode` 48. Debug compilation, unit tests, signed R8
  build, signature verification, archived R2 upload, and public APK/metadata verification pass.
- Production SHA-256 is
  `1689097628e97c0a1702862c375a59435c83714e3b1a057a479c45da785ed41b`.
- No mini-player, full-player, playback service, queue, or protected component file was changed.

## Premium session restoration — published in v1.1.0

Rollback snapshot:

- Git tag: `snapshot-before-premium-session-fix-v1.0.45`.
- Exact commit: `4c43747`.
- The tag is pushed to `origin` and restores the documented published `v1.0.45` source.

Published behavior:

- Premium and Free remain the two account plans. `profiles.is_premium = true` renders
  `⭐ Premium`; `false` renders `Plan Free`.
- The most recently confirmed plan is cached together with its Supabase user ID. It is restored
  only for the same user, preventing one account's plan from leaking into another account.
- A process restart, OTA installation, temporary network interruption, or token renewal no
  longer erases an already confirmed Premium/Free label while the profile reconnects.
- Initial profile loading waits for saved-session restoration to finish. It reruns whenever the
  access token changes, fixing the startup race where the old token could return HTTP 401 and
  leave the plan as unknown.
- Successful REST refreshes and Supabase Realtime changes update both the in-memory state and
  the per-user cache. Server subscriptions, redeem codes, and Premium authorization are unchanged.
- The bottom navigation still hides while scrolling downward and returns immediately when
  scrolling upward. A post-scroll boundary check now also restores it upon reaching the bottom.
- Screens without scrollable content keep the bottom navigation visible because their unconsumed
  scroll is treated as a boundary rather than an active downward content scroll.
- Published version is `1.1.0`, `versionCode` 47. Debug compilation, unit tests, signed R8
  build, signature verification, direct R2 verification, and public SHA-256 verification pass.
- No mini-player, full-player, playback service, queue, or protected component file was changed.

## Foreground OTA refresh — published in v1.0.45

Rollback snapshot:

- Git tag: `snapshot-before-foreground-ota-refresh-20260801`.
- Exact commit: `43cae41`.
- The tag is pushed to `origin` and restores the documented published `v1.0.44` source.

Published behavior:

- Opening the application or returning it to the foreground forces a fresh stable OTA metadata
  request instead of reusing the three-hour release cache.
- The foreground loop performs another fresh request every 180 minutes while the application
  remains foregrounded and is cancelled when it leaves the foreground.
- The existing ten-second DataStore deduplication window prevents activity/process startup from
  causing duplicate requests.
- The latest foreground result is exposed as a `StateFlow` and observed by `MainActivity`, so an
  available update opens the existing in-app update sheet even if system update notifications
  are disabled.
- The update-notification preference still controls system notifications and the six-hour
  WorkManager fallback; disabling it no longer disables foreground in-app checks.
- `Updater.getLatestVersionName(forceRefresh)` passes the foreground refresh request through to
  the stable release fetch. Manual checks retain their existing forced-refresh behavior.
- Published version is `1.0.45`, `versionCode` 46. Debug compilation, unit tests, signed R8
  build, signature verification, and remote SHA-256 verification pass.
- No mini-player, full-player, playback service, queue, or protected component file was changed.

## Bottom navigation directional auto-hide — published in v1.0.44

Rollback snapshot created before this experiment:

- Git tag: `snapshot-before-bottom-nav-autohide-20260801`.
- Exact commit: `a0abc58948b4f979c829889d628876c9852baeff`.
- The tag is pushed to `origin` and restores the complete state after published `v1.0.42`.

Additional rollback snapshot created before the directional correction:

- Git tag: `snapshot-bottom-nav-v1.0.43`.
- Exact commit: `7da1cb3`.
- The tag is pushed to `origin` and restores the published `v1.0.43` behavior.

Current behavior:

- The floating bottom navigation toolbar hides only when nested content scrolls downward.
- It returns immediately when the user scrolls upward, using the existing bottom-to-top spring
  slide animation; it no longer waits for scrolling to stop.
- Direction events are collected without consuming or altering the nested content scroll.
- Route changes immediately restore the toolbar.
- Layout insets and mini-player anchors remain unchanged, avoiding content or player jumps.
- Only the bottom navigation logic inside `MainActivity.kt` is changed; no mini-player,
  full-player, playback service, queue, or protected component file is modified.
- Debug compilation, unit tests, signed R8 build, signature verification, and remote SHA-256
  verification passed.

### One-second idle restore — published in v1.1.4

- After a downward scroll hides the bottom toolbar, every new downward event restarts a
  one-second timer. The toolbar becomes visible when the page remains stationary for one full
  second.
- Upward scrolling, reaching a scroll boundary, changing routes, and pages without scrollable
  content continue to restore the toolbar immediately.
- The change is limited to the bottom-navigation event collector in `MainActivity.kt`.
  Mini-player layout, anchors, controls, playback, and protected files remain unchanged.
- Published version is `1.1.4`, `versionCode` 51. Signed R8 build, unit tests, signature,
  version, private-string scan, immutable R2 archive, and public endpoint verification passed.
  Production SHA-256 is
  `f581b7f79bc7e2438ac350f48600485d70a1d585d3ea6fe875c5a30d69b50281`.
- Rollback tag `snapshot-before-bottom-nav-idle-v1.1.3` points to `1963c08` and is pushed to
  `origin`. Mini-player and protected player files remain unchanged.

## Account-isolated recommendations, player menu, and task-close playback — published in v1.1.5

- `WiwyHomeScreen` keys its personalized artist request by the current Supabase user and
  onboarding state. It clears the previous result immediately on logout/account change and
  reloads after a new user finishes artist selection.
- Personalized home sections query only `preferred_artists.source = onboarding`. Learned rows
  from the old device-wide history flow no longer appear as selected preferences.
- The automatic `learnFromHistory(database)` call at login was removed because the local event
  database is shared by the installation and could copy one account's listening history into a
  different account. Library and playback history records themselves are not deleted.
- Artist pages remain the primary song source; an artist-filtered song search is used as a
  fallback so a newly onboarded account receives music for its selected artists even when an
  artist page has no song section.
- In `PlayerMenu`, the quick action previously used for copying/sharing the track link now opens
  the existing Equalizer dialog. The older Equalizer list row was removed to prevent duplication.
- On 2026-08-02 the user explicitly authorized a visual-only change in
  `ui/player/PlayerComponents.kt`: every full-player style (V2 through V8 and the default style)
  now draws the same determinate `CircularWavyProgressIndicator` concept used around the mini
  player artwork around its Play/Pause icon. The wave advances from the existing playback
  `position/duration`; loading remains indeterminate. Click handling, play/pause/replay logic,
  touch sizes, queue, service, and the mini-player files are unchanged.
- On 2026-08-02 the user explicitly authorized changing protected
  `playback/MusicService.kt`. `onTaskRemoved` now always saves the persistent queue, stops and
  clears playback, removes the foreground notification, and stops the service when the app is
  removed from Recents. Minimizing, changing apps, locking the screen, and normal background
  playback do not call this path.
- Mini-player UI files, dimensions, controls, animations, and full-player connection remain
  unchanged. Unit tests, signed R8 build, signature, version, private-string scan, immutable R2
  archive, and public endpoint verification pass. Production SHA-256 is
  `10de1de5bfc14ed469cd8ce615ee573cb7a6e074432e487eab6ffc16e58e7d40`.
- Rollback tag `snapshot-before-account-playback-ui-v1.1.4` points to `04ee608` and is pushed
  to `origin`.

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
- Production release: `v1.1.9` (R2 stable OTA).
- `versionName`: `1.1.9`.
- `versionCode`: `56`.
- Production commit: `db2cae7` (`build: bump version to 1.1.9`).
- Last GitHub bridge release: <https://github.com/angelanda023-prog/WiwyMusic/releases/tag/v1.0.42>
- OTA asset name: `WiwyMusic.apk`.
- Production APK SHA-256: `bd77d6cd0de3f9085dec257aa78342ca52428c6549a0ad90fda3a414034ede22`.
- Previous production baseline: `v1.1.8`, commit `159650f`, SHA-256
  `036769d3b78b2c4426668faf6ab56d2c52b94b08acf8f1db00ab74ce22f5f769`.

The bridge release was published through both GitHub and R2. Clients upgrading from `v1.0.41`
can discover `v1.0.42` through the old GitHub updater; after installation, the Cloudflare
endpoint offers current stable `v1.1.9`.

## APK hardening and rollback baseline — published in v1.0.42

Rollback is fixed to the unchanged production release:

- Tag: `v1.0.41`.
- Commit: `474de8f`.
- APK SHA-256: `d1a6c76cf0353a345c67d776bad903333f31d5970f330ace4a9efed74419f927`.
- An exact rollback copy is stored privately in R2 as
  `ota/archive/v1.0.41/WiwyMusic.apk`.
- Versioned copies exist at `ota/archive/v1.0.42/WiwyMusic.apk`,
  `ota/archive/v1.0.43/WiwyMusic.apk`, `ota/archive/v1.0.44/WiwyMusic.apk`, and
  `ota/archive/v1.0.45/WiwyMusic.apk`, `ota/archive/v1.1.0/WiwyMusic.apk`,
  `ota/archive/v1.1.1/WiwyMusic.apk`, `ota/archive/v1.1.2/WiwyMusic.apk`,
  `ota/archive/v1.1.3/WiwyMusic.apk`, `ota/archive/v1.1.4/WiwyMusic.apk`,
  `ota/archive/v1.1.5/WiwyMusic.apk`, `ota/archive/v1.1.6/WiwyMusic.apk`,
  `ota/archive/v1.1.7/WiwyMusic.apk`, `ota/archive/v1.1.8/WiwyMusic.apk`, and
  `ota/archive/v1.1.9/WiwyMusic.apk`; current `v1.1.9`
  is served from its immutable archive
  through `/api/ota/download`.
- R2 metadata `ota/releases.json` points to `v1.1.9` with the generic improvements message.
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
- Versioning rule requested on 2026-08-01: after the `1.0.x` line (for example `1.0.20`), the
  next feature version is `1.1.0`; do not continue it as another `1.0.x` patch unless the user
  explicitly requests that.

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

## Admin Premium controls — deployed 2026-08-02

- `GrantPremiumForm.tsx` presents `Otorgar Premium` and `Convertir a Gratis` in the same
  two-column action row. Both retain their existing Server Actions and shared pending state.
- The codes table now displays every account that redeemed a code, including reusable codes,
  with email and redemption date. Older one-use codes fall back to `redeemed_by` and
  `redeemed_at` when no history row exists.
- `listRedeemCodes()` combines `redeem_codes`, `redeem_code_redemptions`, and Supabase Auth
  users; no database migration was required.
- Validation passed with targeted ESLint, `tsc --noEmit`, Next.js production build, and the
  OpenNext Cloudflare build. Deployed Worker version:
  `90481757-9a06-46d3-8692-8cd9b9b5c22d`.

## Admin APK download button — deployed 2026-08-02

- The persistent admin sidebar now includes a `Descargar APK` button beneath its navigation.
  It is available from Dashboard, Usuarios, and Códigos.
- The button is a standard download link to `/api/ota/download`, so it always serves the stable
  immutable APK selected by `ota/releases.json`; it does not duplicate or expose the R2 object.
- Modified file: `/Users/wiwyzho/Documents/Web/WiwyMusic-Admin/src/app/dashboard/layout.tsx`.
- Validation passed: targeted ESLint, `tsc --noEmit`, Next.js production build, and OpenNext
  Cloudflare build. Existing Durable Object and deprecated middleware warnings remain unchanged.
- Admin commit: `1520542` (`feat(admin): add APK download button`). Rollback tag
  `snapshot-before-admin-apk-download-20260802` points to `c47f6b8`.
- Deployed at <https://wiwymusic-admin.angelanda023.workers.dev> as Worker version
  `94a2fda9-9271-4721-b364-a4075b5c50fb`.
- Production verification passed: `/api/ota/releases` still reports `v1.1.9`; the download
  responds as `application/vnd.android.package-archive` with filename `WiwyMusic.apk`, and its
  SHA-256 remains `bd77d6cd0de3f9085dec257aa78342ca52428c6549a0ad90fda3a414034ede22`.
- No Android, OTA metadata, R2 object, mini-player, player, or playback file was modified.

## Admin mobile-native redesign — deployed 2026-08-03

- Admin commit: `e080db5` (`feat(admin): redesign mobile dashboard`). Rollback tag
  `snapshot-before-admin-native-redesign-20260803` points to deployed baseline `1520542`.
- Persistent desktop sidebar and a 200 ms slide-in mobile drawer provide Dashboard, Usuarios,
  Códigos, Descargar APK, Ajustes, and Cerrar sesión. Mobile header contains the menu button,
  WiwyMusic Admin title, and a small notifications popover.
- Dashboard now greets the administrator and shows four large live cards: Usuarios, Premium,
  Reproduciendo, and Códigos. Existing Supabase aggregations and ten-second realtime refresh
  remain unchanged.
- Users table was replaced by touch-friendly responsive cards showing plan status, APK presence,
  Premium time remaining, current playback when available, and a full-width detail action.
- User detail is one column: identity, activity, plan controls, subscription history, and account
  information. Premium duration uses the existing allowed day options; actions still call the
  existing grant/convert Server Actions.
- Codes table was replaced by responsive cards. A floating `Crear código` button expands the
  creation form. `Editar` updates duration/unlimited status for future canjes. `Eliminar` performs
  the existing safe soft-deactivation, preserving redemption history; no destructive row deletion
  was added.
- New `/dashboard/settings` route provides administrator identity and stable APK download access.
  Shared custom outline SVG icons avoid a new dependency. `dashboard/loading.tsx` adds responsive
  skeleton cards. Global panel surface is `#F5F7FB` with white 20 px cards, soft shadows,
  blue-violet gradients, 200 ms transitions, accessible touch targets, and reduced-motion support.
- Validation passed: `git diff --check`, targeted ESLint, `tsc --noEmit`, Next.js production build,
  and OpenNext Cloudflare build. Existing local Durable Object and deprecated middleware warnings
  remain unchanged.
- Browser validation reached the protected login correctly; authenticated local pages could not be
  inspected without sharing production credentials or bypassing auth. No credential or auth bypass
  was used.
- Deployed at <https://wiwymusic-admin.angelanda023.workers.dev> as Worker version
  `f94348ef-c7df-42e0-a9bd-f3721d4a3618`.
- Production verification passed: root redirects unauthenticated access to login, OTA metadata
  remains `v1.1.9`, and `/api/ota/download` still returns `WiwyMusic.apk` with SHA-256
  `bd77d6cd0de3f9085dec257aa78342ca52428c6549a0ad90fda3a414034ede22`.
- No Android source, OTA metadata, R2 object, mini-player, player, or playback file was modified.

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
5. Upload the APK under `ota/archive/v<version>/WiwyMusic.apk` and remotely verify its hash.
6. Only after archive verification, update and upload `ota/releases.json` with generic text.
7. Verify both public OTA endpoints and confirm the downloaded APK hash. GitHub is not needed
   for later in-app OTAs.

The OTA Worker validates the published `tag_name` and serves
`ota/archive/<tag_name>/WiwyMusic.apk`. The legacy `ota/WiwyMusic.apk` object is only a fallback;
do not rely on replacing it during normal publication. This immutable-archive routing was
deployed after R2 repeatedly reported a successful overwrite while direct downloads still
returned the previous stable object.

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
