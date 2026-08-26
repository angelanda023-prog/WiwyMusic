# WiwyMusic — project context and durable AI handoff

Last updated: 2026-08-26 (America/Mexico_City)

This is the single source of truth for any editor, IDE assistant, or AI working on WiwyMusic.
Read it completely before inspecting or changing code. Update it after every meaningful change,
build, deployment, migration, or release. Do not create competing project-memory documents.

## Current source state

- Desktop playback recovery published on 2026-08-26. macOS `v1.0.7` and Windows `v1.0.3`
  bundle verified yt-dlp `2026.08.19`, retry fresh YouTube sources with compatible clients and
  formats, and hide technical source errors until bounded recovery is exhausted. macOS falls back
  to its internal YouTube player when AVPlayer rejects a resolved stream. Windows preserves
  required HTTP headers and byte ranges through a loopback audio proxy. Both engines can refresh
  their writable yt-dlp copy without replacing the whole app. macOS OTA DMG SHA-256 is
  `9dba2e19b267f0c251147474a0b521f0847d9160baa7860e01f0a3f134a19cd6`; Windows OTA EXE SHA-256 is
  `12b40c59f8f6b4c62b4a4babd5b358e5eda2e752d4820837f4ae1dc1025cfdec`.
- Cross-platform download update on 2026-08-13: the Download action in the full player now shows
  an animated in-progress state on Android, macOS, and Windows. The user explicitly scoped this
  change to the full player and confirmed it does not apply to the mini-player. macOS `v1.0.6`,
  Windows `v1.0.2`, and Android `v1.1.20` are published.
- Android modifies only the protected `ui/player/PlayerComponents.kt`, with explicit user
  authorization in this conversation. Every player style rotates the existing downloading glyph
  only while Media3 reports queued/downloading. Existing tap behavior still cancels queued/active
  downloads and removes completed downloads. Playback, mini-player, queue, service, dimensions,
  and player connection are unchanged. Debug Kotlin compilation and all universal debug unit
  tests pass.
- macOS full-player Download now renders `ProgressView` while its observable `DownloadStore`
  contains the current video ID, shows a completed mark afterward, and removes the stored offline
  file when that completed action is tapped. Library > Downloads also has a visible trash action
  for every downloaded song. A real M4A download succeeded using the bundled universal `yt-dlp`
  2026.07.04. All 11 Swift tests pass, the arm64/x86_64 production app and DMG build pass, and
  strict deep code-signature verification passes.
- Windows now tracks download IDs in progress, prevents duplicate concurrent downloads, displays
  an indeterminate progress indicator, verifies a non-empty output before cataloging it, deletes a
  failed target, and removes the offline copy when the completed full-player action is tapped.
  Library > Downloads also has a visible trash action for every downloaded song.
  The bundled PE32+ x86-64 `yt-dlp.exe` is refreshed into `%APPDATA%\\WiwyMusic` when the packaged
  copy changes instead of being installed only once. Tests and `createDistributable` pass; the
  generated application JAR contains the exact bundled executable digest. Native MSI/EXE execution
  still requires Windows and was not claimed from the macOS workstation.
- Desktop lyrics no longer send the placeholder album `YouTube Music` to LRCLIB as a real album.
  macOS and Windows try an exact album only when meaningful, retry without album, then fall back to
  LRCLIB search. Both projects include and pass a live regression test for a known YouTube Music
  track.
- New public landing source lives in `website/`. It presents WiwyMusic, Free/Premium,
  Android installation, and independent macOS/Windows downloads. It is published as the isolated
  Cloudflare Pages project `wiwymusic` at <https://wiwymusic.pages.dev>.
- Android `v1.1.17` removes the in-feed Premium card so the original Home carousel
  again follows the greeting directly. Free accounts instead receive the supplied square Premium
  artwork as a dismissible launch dialog on every application foreground. Its pictured
  `ACTÍVALO HOY` area opens the same prefilled `$40 MXN al mes` WhatsApp request. Premium and
  unknown/loading plans do not see it. This behavior is published in OTA `v1.1.17`.
- Android `v1.1.18` adds a six-hour launch-promotion cooldown for confirmed Free
  accounts. The display timestamp is persisted when the dialog opens; elapsed time alone never
  interrupts an active session, and the next eligible application foreground shows it. The dialog
  backdrop is pure black and its close control is a larger orange 58 dp button. Account Settings
  adds `Adquirir código Premium`, opening the corrected Mexican WhatsApp contact with a code-specific
  `$40 MXN al mes` message. The Home carousel now falls back from personal quick picks to playback
  history and then real YouTube Home songs, fixing its absence on new or sparse installations.
  This follow-up is published in OTA `v1.1.18`.
- Android `v1.1.19` recovers remote Media3 source failures, including wrapped
  `ERROR_CODE_IO_UNSPECIFIED` / code 2000 errors, before exposing an error to the user. Recovery
  invalidates the failed URL, temporarily avoids the failed client and first failed codec, and
  allows two bounded fresh-stream retries. Local `content` and `file` items are excluded. After
  retries are exhausted, the full player shows only the friendly temporary-unavailability message;
  manual Retry starts with clean resolver state. The user explicitly authorized changes to the
  protected `MusicService.kt` and full-player playback error UI; mini-player files are unchanged.
  All universal debug unit tests and signed R8 release assembly pass.
- Android `v1.1.20` adds the authenticated Premium expiration display
  in Account Settings and an opt-in top playback overlay called `Isla dinámica de reproducción`.
  Remaining days render as a larger orange bordered chip. The island is Premium-only: Free and
  unknown accounts see a lock and the standard obtain-Premium dialog. Android requests overlay
  permission only after an eligible user enables the setting. Its 320 x 42 dp black capsule is
  centered below the notification/status bar and before the first launcher widget, slides down
  from above, and shows artwork,
  title/artist, and orange play/pause. Its lateral interaction mirrors the mini-player effect
  without touching mini-player code: content follows the finger, reveals an orange direction icon,
  changes at the threshold, and eases back to center. Swiping right advances; swiping left returns
  to the previous song; tapping opens WiwyMusic. The MediaSession controller connects while foregrounded so OEM
  background binding restrictions cannot suppress the overlay. It appears only while the app is
  in background and no `MusicService.kt`, PlayerConnection, or mini-player file is modified.
  Kotlin compilation, all universal debug unit tests, signed R8 release assembly, and APK Signature
  Scheme v2 verification pass. Production APK SHA-256 is
  `a944846ad38c48de6b076571718018adc12de3816e532e29b89ada24f9e103cd`.
  It is published through the immutable R2 archive and public OTA endpoint; the previous stable
  rollback remains tag `snapshot-before-account-expiry-island-v1.1.19` and archive `v1.1.19`.
- The Admin Worker now exposes authenticated `/api/mobile/premium-status`: it derives the user ID
  from the supplied Supabase access token, reads only that user's active subscription with the
  server-side service role, and returns expiration/days with `Cache-Control: no-store`. The route
  is deployed as Worker version `a46d647b-ff49-4887-a24b-81b4f1edd350`; an unauthenticated
  production request correctly returns JSON HTTP 401 instead of redirecting to the admin login.
  No database migration was required.
- Android authentication sessions now use the dedicated `auth_session` Preferences DataStore,
  which is excluded from cloud backup and device-to-device transfer. An in-place APK update
  migrates the legacy session once and removes its old keys from the backed-up `settings` store;
  a fresh installation rejects and clears legacy tokens restored by Android. Consequently,
  uninstalling and reinstalling requires login while ordinary updates preserve the active session.
  Debug Kotlin compilation, all 49 universal debug unit tests, signed R8 release assembly, APK
  Signature Scheme v2, production certificate, package/version, and private-string checks pass.
  This correction is published in OTA `v1.1.21` (`versionCode` 68), source commit `977d76c`.
  Local, immutable R2 archive, and public download match SHA-256
  `d4ed2fad9197a40403d5505a042f50f49660ce50cff60e4d95595179775ab19f`. Rollback tag
  `snapshot-before-auth-backup-fix-v1.1.20` and immutable archive `v1.1.20` preserve the previous
  stable release. No player, mini-player, playback, queue, service, or player-connection file changed.

- Android repository: `/Users/wiwyzho/Documents/Web/WiwyMusic`.
- Android branch: `main`.
- Current Android production source commit: `977d76c`
  (`fix(auth): prevent restored sessions after reinstall`).
- Registration password visibility is published in OTA `v1.1.14`.
- Playlist import lock visibility commit: `d70de59` (`fix(library): show playlist import lock`).
- Playlist import Premium gate commit: `1429cc5` (`feat(library): gate playlist import`).
- Settings inset fix commit: `7c595d3` (`fix(settings): keep actions above overlays`).
- Premium player controls commit: `304dcf7` (`feat(player): gate premium controls`).
- Account/player change commit: `ee6ee9a` (`feat: personalize accounts and playback UI`).
- Full-player download shortcut commit: `8756ad9` (`feat(player): add download shortcut`).
- Premium session fix commit: `7385e6b` (`fix: persist premium plan across sessions`).
- Previous hardening commit: `654626c` (`security: hide OTA repository details`).
- Admin repository: `/Users/wiwyzho/Documents/Web/WiwyMusic-Admin`.
- Admin OTA metadata commit: `7604f99` (`chore(ota): publish Android v1.1.21`, local; the admin
  repository has no Git remote configured).
- Admin Windows download commit: `3c18510` (`feat(admin): add Windows app download`, local;
  deployed Worker version `47dd057a-f636-4710-af74-ff5d5d15b362`; the admin repository has no
  Git remote configured).
- Admin security migration commit: `b1ddf8c` (`security(db): restrict premium mutations`, local;
  production migration applied).
- Admin OTA documentation commit: `c47f6b8` (`docs: record v1.1.9 OTA artifact`, local;
  the admin repository has no Git remote configured).
- Admin OTA archive-routing commit: `9d3d466` (`fix(ota): serve published archive`, local;
  deployed Worker version `91b47c42-f0fa-4479-ad9f-814402d554bf`).
- Admin panel UI commit: `ae42e23` (`feat(admin): improve premium management`, local;
  the admin repository has no Git remote configured).
- Admin APK download button commit: `1520542` (`feat(admin): add APK download button`, local;
  deployed Worker version `94a2fda9-9271-4721-b364-a4075b5c50fb`).
- Admin duplicate APK menu cleanup commit: `f6e3b81`
  (`fix(admin): remove duplicate APK menu link`, local; deployed Worker version
  `d8b52018-8005-4780-ba44-b0f22733ec5d`).
- Admin mobile-native redesign commit: `e080db5` (`feat(admin): redesign mobile dashboard`,
  local; deployed Worker version `f94348ef-c7df-42e0-a9bd-f3721d4a3618`; the admin repository
  has no Git remote configured).
- Admin logo favicon final commit: `73bff42` (`fix(admin): preserve cached favicon path`, local;
  deployed Worker version `dea84bfd-acce-47d3-9268-de739b9f755c`).
- Admin user search commit: `837fee8` (`feat(admin): add user search filters`, local;
  deployed Worker version `fe7781f2-c0b0-444d-9e55-eff724ec5fea`; the admin repository has no
  Git remote configured).
- Premium auto-expiry migration commit: `f676260` (`fix(premium): expire elapsed plans`, local;
  superseded in production by the complete three-tier migration below).
- Three-plan Admin/database commit: `108132f` (`feat(plans): add Premium Plus tier`, local;
  production migration applied; deployed Worker version
  `722f55eb-0dd5-4742-9087-dfcfe11b236c`).
- Admin suspended-subscription deletion commit: `8e12002`
  (`feat(admin): delete suspended subscriptions`, local; deployed as Worker version
  `5e96d14c-3ff2-4a85-98a8-3b97a5c96754`).
- Admin user deletion commit: `ea0ac5c` (`feat(admin): delete user accounts`, local;
  deployed Worker version `dcb17767-7bf9-4044-baad-e3fd47b1a158`; the admin repository has no
  Git remote configured).
- Admin Downloader code commit: `c2e86f8` (`feat(admin): show Downloader code`, local;
  deployed Worker version `db7b1723-e6fd-4a87-95d3-3800450320ba`; the admin repository has no
  Git remote configured).
- Three-plan Android commit: `cdf177c` (`feat(account): show three plan tiers`, included in
  later published OTAs).
- Production APK is `v1.1.21`, `versionCode` 68, distributed through Cloudflare R2.

## Public product website — published 2026-08-15

- Static landing source: `/Users/wiwyzho/Documents/Web/WiwyMusic/website`.
- Visual direction adapts the supplied Qwen cinematic-timeline specification to WiwyMusic: pure
  black canvas, liquid-glass surfaces, gold/orange accents, a scroll-drawn vertical timeline, large
  Playfair headings, compact Apple-like copy, and four alternating experience stages. No reference
  asset or old application mockup is copied.
- Five original cinematic images were generated for the site: headphones/vinyl hero, discovery
  sphere, audio-wave filaments, glass lyric ribbons, and an offline sound capsule. Each has a
  matching 6-second, 1280 × 720 H.264 MP4 loop generated locally with the reusable
  `website/tools/make_loop.swift` AVFoundation tool. PNG posters remain available when video is
  unavailable or has not yet been lazy-loaded.
- The original APK artwork from `assets/icon.png` is copied as
  `website/assets/wiwymusic-icon.png` and used for the browser favicon, Apple touch icon, navbar,
  and footer brand.
- Primary Android actions point to the stable Cloudflare endpoint `/api/ota/download`. Runtime
  release text reads `/api/ota/releases` with `v1.1.18` as a resilient fallback. Separate macOS
  DMG and Windows EXE actions use their existing independent endpoints.
- Sections: cinematic hero, four-step experience timeline, Free/Premium plans,
  final Android/macOS/Windows download call-to-action, and footer. Premium activation wording
  accurately requires a valid code and shows the user-supplied price of `$40 MXN / mes`; the
  Premium card opens WhatsApp at Mexican number `+52 81 3689 0880` with a prefilled request
  message through a CTA labeled `Obtener`. The page does not claim that payment processing is
  implemented.
- Site uses plain HTML, CSS, and JavaScript with no package dependency. It includes accessible
  navigation, keyboard Escape handling, responsive menu, reduced-motion support, scroll reveal,
  semantic headings, and a skip link.
- Local browser validation passed at desktop and 390 x 844 mobile sizes. Mobile has no horizontal
  overflow (`scrollWidth = innerWidth = 390`), menu opens/closes and navigates correctly, and the
  console has no errors or warnings. HTML parsing, JavaScript syntax check, `git diff --check`,
  and public download-link checks pass. Production validation at <https://wiwymusic.pages.dev>
  confirms HTTP 200, loaded icon and video, correct Free/Premium plans and `$40 MXN / mes` price,
  WhatsApp/APK links, no Premium Plus text, no horizontal overflow, and no console errors. The
  WhatsApp now redirects with the corrected international phone value `528136890880`; the old
  `528136899880` value is absent. The `v1.1.18` website update changes the request message to
  explicitly acquire a Premium code and is published at immutable deployment
  <https://2b5c5f83.wiwymusic.pages.dev>; production matches it at
  <https://wiwymusic.pages.dev>.
- Android `v1.1.21` download fallback/version text is published at immutable Pages deployment
  <https://ef03bb1c.wiwymusic.pages.dev> and production <https://wiwymusic.pages.dev>. Production
  HTML returns HTTP 200, contains only the `v1.1.21` fallback, and all Android download actions
  target the verified public OTA download endpoint.
- Desktop download labels now show macOS `v1.0.7 · DMG universal` and Windows
  `v1.0.3 · Instalador x64`, retaining their verified independent OTA endpoints. Published at
  immutable Pages deployment <https://c976a969.wiwymusic.pages.dev> and production
  <https://wiwymusic.pages.dev> on 2026-08-26.

## Android Free-to-Premium promotion — published in v1.1.17

- The in-feed Home card published in `v1.1.16` is removed from current source. `FeaturedCarousel`
  again renders directly after the greeting and its data, playback action, pager, and indicators
  are unchanged.
- `WiwyHomeScreen` now presents the exact user-supplied 1254 × 1254 PNG in a dimmed launch dialog
  whenever the application enters the foreground and the authenticated account is confirmed Free.
  Premium, unknown/loading, and logged-out states fail closed. One foreground generation can claim
  the dialog only once, preventing navigation or recomposition from duplicating it.
- The artwork is stored as `drawable-nodpi/wiwymusic_premium_launch.png`; its SHA-256 exactly
  matches the supplied file:
  `52eda4f21b9e549f989d2f20f355b1098346c4073d5a2d34d500a682080423c2`.
  A transparent accessible action overlays the pictured `ACTÍVALO HOY` button, and a visible close
  action plus Android Back can dismiss the dialog.
- Existing `PremiumFeatureDialog` callers now show `Obtener Premium` and `Ahora no`. The obtain
  action opens `https://wa.me/528136890880` with the prefilled request for the `$40 MXN al mes`
  plan, then closes the dialog.
- The corrected Mexican number is `+52 81 3689 0880`, encoded for `wa.me` as `528136890880`
  without spaces, punctuation, or `+`. The URL returns the expected WhatsApp redirect with that
  exact phone value and the WhatsApp landing page exposes `Chat on WhatsApp`.
- Shared contact constants and intent handling remain in `utils/PremiumContact.kt`; foreground
  eligibility/claim state lives in `utils/PremiumLaunchPromo.kt`. Unit coverage verifies Free,
  Premium, unknown, logged-out, repeated-generation, phone-number, and decoded-message behavior.
- Validation passed: all 43 universal debug unit tests, debug Kotlin compilation, exact source/
  packaged-image digest comparison, `git diff --check`, and production website/WhatsApp redirect
  checks. No mini-player, protected player, playback, queue, service, dimensions, animations, or
  player-connection file was modified. The signed R8 OTA is `v1.1.17`, `versionCode` 64, source
  commit `ae7e3b8`. APK Signature Scheme v2 and the production certificate are valid. Local,
  immutable R2 archive, and public download match SHA-256
  `28607371b774c2ee204548403f76650dab0bf7478a3c1742f4b7937fa7254005`.
- Rollback tag `snapshot-before-premium-launch-dialog-v1.1.16` points to the preceding production
  source commit `297c8c8` and is pushed to `origin`.

### Six-hour pacing, Account CTA, and carousel fallback — published in v1.1.18

- Confirmed Free accounts show the launch artwork at most once per six hours. The last display time
  is stored in DataStore when the dialog actually opens. Foreground entries before the deadline are
  claimed without showing, so crossing six hours while the app remains open does not interrupt the
  user; the following foreground entry rechecks and displays it.
- Premium, unknown/loading, and logged-out accounts remain excluded. A backward device-clock change
  cannot bypass the cooldown. Unit tests cover the initial display, same-generation deduplication,
  the exact six-hour boundary, plan/session exclusions, and clock rollback.
- The full-screen dialog uses a pure black background. Its close action is 58 dp, orange, and uses a
  28 dp black glyph. The supplied square artwork and transparent `ACTÍVALO HOY` WhatsApp action are
  otherwise unchanged.
- Account Settings places an orange full-width `Adquirir código Premium` button immediately below
  code redemption. It opens `wa.me/528136890880` with a prefilled request to acquire a Premium code
  for `$40 MXN al mes`.
- The featured Home carousel keeps personal `quickPicks` as its first source, uses local playback
  history second, and real song items from YouTube Home third. Previously the UI rendered the
  carousel only when `quickPicks` was non-empty, which hid it for new or sparse local histories.
- Modified files are limited to Home, Account Settings, promotion/contact utilities, a DataStore
  preference key, localized strings, tests, and this handoff. No mini-player, protected player,
  playback, queue, service, or player-connection file is changed.
- Validation passed: all 45 universal debug unit tests, signed R8 release build, package/version
  inspection, APK Signature Scheme v2 with the existing production certificate, one signer,
  private-string scan, and exact source/packaged image pixel comparison. Release is `v1.1.18`,
  `versionCode` 65, source commit `0e43e42`. The local APK, immutable R2 archive, and public
  download match SHA-256
  `6278c2298ed3ccc77dfe6a2de1e77c64e9c4ffa36a64680fc7adb7e386b68de4`.
- Rollback tag `snapshot-before-premium-pacing-v1.1.17` points to feature source commit `198f3dd`
  and is pushed to `origin`.

## macOS automatic OTA installer — current stable v1.0.7

- macOS source lives independently at `/Users/wiwyzho/Documents/Web/WiwyMusic-macOS`; this
  change does not modify the Android project, APK, playback service, or Android mini-player.
- Current stable macOS version is `1.0.7` (`CFBundleVersion` 8), published through the independent
  Cloudflare R2 macOS OTA channel on 2026-08-26.
- `MacUpdater` still downloads only through HTTPS and verifies the complete DMG against the
  SHA-256 published in the Cloudflare manifest before installation begins.
- After verification it mounts the DMG read-only, requires `WiwyMusic.app`, validates bundle ID
  `com.wiwymusic.macos`, requires the expected release version, and runs strict deep code-signature
  verification. It copies the candidate to a staging directory before detaching the DMG.
- Installation replaces the running copy in `/Applications` using a temporary backup. If moving
  or validating the new application fails, the previous application is restored. After success,
  WiwyMusic removes the downloaded DMG, closes, and relaunches the newly installed copy.
- If macOS permissions prevent automatic replacement, WiwyMusic preserves and opens the verified
  DMG for manual installation instead of leaving the application unusable.
- The update alert and Settings action now say `Actualizar`, and explain that the application will
  install and restart automatically.
- Existing public `1.0.3` clients contain the older download-only updater. Therefore `1.0.4` must
  be installed manually once; automatic replacement applies to releases after `1.0.4` is installed.
- Validation passed: live YouTube Music resolution, `plutil`, all 11 Swift tests, universal release build for arm64 and x86_64,
  DMG checksum verification, embedded version/build/bundle inspection, and strict code-signature
  verification. The immutable R2 archive and public download both match SHA-256
  `9dba2e19b267f0c251147474a0b521f0847d9160baa7860e01f0a3f134a19cd6`.
- Public endpoints report `v1.0.7` and serve the verified universal DMG:
  `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/macos/releases` and
  `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/macos/download`.

## Independent Windows application — current stable v1.0.3

- Windows source lives at `/Users/wiwyzho/Documents/Web/WiwyMusic-Windows`. It is a separate
  Kotlin/JVM and Compose Desktop project; it does not modify or depend on Android or macOS source.
- The fixed 390 × 844 window mirrors the phone-style WiwyMusic hierarchy: authentication,
  artist onboarding, branded Home, carousel and personalized sections, Search, Library,
  Settings pages, mini-player, and full player.
- It uses the same Supabase project and public client key as Android/macOS for authenticated
  profile tier, onboarding, preferred artists, Favorites, Premium code redemption, and Admin
  presence. Session/local content is stored under `%APPDATA%\\WiwyMusic` and is isolated by user.
- Account tiers are Free, Premium, and Premium Plus. Download, downloaded-file playback, Lyrics,
  sleep timer, and 10-band JavaFX equalizer use the same fail-closed Premium presentation.
- Playback supports YouTube Music resolution through the bundled Windows `yt-dlp.exe`, preview
  sources, downloaded audio, and imported MP3/M4A/AAC/WAV/FLAC/OGG files. Mini-player provides
  previous, next, and play/pause; full player includes queue, seek, shuffle, repeat, favorites,
  download, lyrics, and timer.
- The Windows project also includes a Music Together WebSocket client, playlists, per-account
  history/download catalogs, a PowerShell build script, and a Windows GitHub Actions workflow.
- Native MSI/EXE packaging runs on Windows by design. GitHub Actions run `31645649058` built and
  validated the first x64 MSI and EXE from source commit `c713ed6`; the Windows source is stored
  in the private `angelanda023-prog/WiwyMusic-Windows` repository.
- Current stable Windows version is `v1.0.3`, published through its independent Cloudflare R2
  channel on 2026-08-26. GitHub Actions run `33000163530` built and validated the x64 MSI and EXE
  from source commit `2be075e`. The immutable archive and public download are valid PE32+ x86-64
  executables and match SHA-256
  `12b40c59f8f6b4c62b4a4babd5b358e5eda2e752d4820837f4ae1dc1025cfdec`.
- Public endpoints are
  `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/windows/releases` and
  `https://wiwymusic-admin.angelanda023.workers.dev/api/ota/windows/download`.
- Admin Settings now presents separate Mac and Windows download buttons. This deployment did not
  modify Android or macOS source, playback, OTA files, or their published versions.
- Windows currently exposes the stable channel as a manual installer download; its Settings
  update button is not yet wired to an automatic installer flow.
- For later Windows releases, use `scripts/build-windows.ps1` or the Windows GitHub Actions
  workflow, verify the EXE digest, upload the immutable archive first, and publish
  `ota/windows/releases.json` last.

## APK identity and Premium server hardening — published in v1.1.13

- The security review from `ESTADO_SEGURIDAD_APPS.md` was adapted to WiwyMusic without adding
  Firebase, Play Integrity enforcement, a playback proxy, or any dependency that could reject
  legitimate sideloaded OTA installations.
- New `utils/ApkUpdateVerifier.kt` verifies every downloaded OTA before Android's installer is
  opened. The candidate must keep package `com.wiwymusic`, match the signing certificate of the
  installed app, and, in release builds, match the fixed production SHA-256 certificate digest.
  Redirects are followed manually and every OTA hop must remain HTTPS. Invalid downloads are
  deleted from the private update cache.
- The same release-certificate check is applied centrally when restoring or accepting the server
  Premium tier. An APK re-signed with another certificate is treated as Free, so Download,
  Lyrics, Sleep timer, and Equalizer remain locked through their existing shared entitlement
  state. Debug builds intentionally bypass the production-certificate check.
- `DebugActivity` is no longer exported. The internal crash handler still opens it explicitly in
  its separate process.
- Admin migration `supabase/migrations/0006_security_hardening.sql` was applied to production
  Supabase on 2026-08-04. It removes direct authenticated writes to plan/subscription/code
  data, keeps only `profiles.onboarded` and `profiles.avatar_url` client-writable, makes the
  redemption RPC use an empty search path, serializes concurrent attempts, limits rejected
  attempts to 10 per 10 minutes, and records private audit events without storing entered codes.
- Post-application verification passed: service-role REST can read the new `security_events`
  table (HTTP 200), while anonymous REST access to `redeem_codes` and anonymous execution of
  `redeem_premium_code` are both rejected (HTTP 401). No production user data was printed or
  modified during verification.
- Android recognizes the new `rate_limited` response with a friendly retry message. Normal valid
  redemption responses and the existing Premium celebration payload remain unchanged.
- Validation passed: `git diff --check`, all universal debug unit tests, debug Kotlin compilation,
  and signed R8 universal release assembly. Release `v1.1.13` (`versionCode` 60) uses APK
  Signature Scheme v2 and production certificate SHA-256
  `54165311e546cc7772dc48f059848b5aa5256250b8f6485fcc5b2abae0e8cb70`.
- The immutable R2 archive and public download both match SHA-256
  `a8f221cbee283acf3f336bbc99d7b74d783bb4dbfedc2f805a9b12a3d2977367`. Rollback tag
  `snapshot-before-security-hardening-v1.1.12` points to `cf3b60d` and is pushed to GitHub.
- No mini-player, full-player, playback service, queue, player connection,
  download implementation, lyrics implementation, timer, equalizer, or audio behavior changed.

## Admin Downloader code — deployed 2026-08-06

- Admin Ajustes displays Downloader code `2517976` directly below the stable `Descargar APK`
  action inside the existing Android distribution card.
- The code is presented as a prominent, centered monospace value and does not change the APK
  endpoint, OTA metadata, R2 files, authentication, or Android application.
- Modified Admin file: `src/app/dashboard/settings/page.tsx`. Admin commit: `c2e86f8`.
- ESLint, `tsc --noEmit`, and the Next.js production build pass. Existing Durable Object and
  deprecated middleware warnings remain unchanged.
- Deployed at <https://wiwymusic-admin.angelanda023.workers.dev> as Worker version
  `db7b1723-e6fd-4a87-95d3-3800450320ba`. Production verification passed: `/login` returns
  HTTP 200, unauthenticated Ajustes redirects to login, OTA metadata remains `v1.1.13`, and the
  public APK endpoint still serves `WiwyMusic.apk` as `application/vnd.android.package-archive`.

## Registration password visibility — published in v1.1.14

- `WiwyAuthScreen` now shows an accessible eye button only while creating an account. It toggles
  the new-password field between masked and visible text so the user can verify what they typed.
- Login and recovery flows remain unchanged: the login password stays masked and no credential,
  request, Supabase authentication behavior, or account data is altered.
- Modified Android file: `ui/screens/auth/WiwyAuthScreen.kt`; source commit `48e0c6e`.
  Universal debug Kotlin compilation and unit tests pass, and the signed R8 universal release
  assembly passed. The APK uses Signature Scheme v2 and the existing production certificate.
- The immutable R2 archive and public download both match SHA-256
  `1df700d271e049d439f3a68155d010952be8fc8e3150f81854caca6ca71fb492`.
  Rollback tag `snapshot-before-registration-password-v1.1.13` points to the previously
  published source commit `cd5ff33` and is pushed to GitHub.
- No mini-player, player, playback, download, OTA behavior, or Admin application code changed.

## Admin user deletion — deployed 2026-08-06

- User detail pages now include a red `Eliminar usuario` danger card for every normal account.
  The administrator account never renders this control and is rejected again by the Server
  Action using both its authenticated user ID and configured admin email.
- The client requires an explicit irreversible-action confirmation and shows a pending state.
- `deleteUserAccount` validates the untrusted user ID, re-reads the target through Supabase Auth
  Admin, removes the known avatar object, clears the legacy `redeem_codes.redeemed_by` foreign-key
  reference, permanently deletes the Auth user, invalidates Dashboard/User caches, and redirects
  to the users list. Existing cascade rules remove that user's profile, subscriptions, presence,
  and redemption rows; other users and code availability are not changed.
- Modified Admin files: `src/app/dashboard/actions.ts`,
  `src/app/dashboard/users/[id]/page.tsx`, and new
  `src/components/DeleteUserButton.tsx`.
- Targeted ESLint, `tsc --noEmit`, and Next.js production build pass. Existing local Durable
  Object and deprecated middleware warnings remain unchanged. No user was deleted during tests.
- Deployed at <https://wiwymusic-admin.angelanda023.workers.dev> as Worker version
  `dcb17767-7bf9-4044-baad-e3fd47b1a158`.
- Production verification passed: `/login` returns HTTP 200, unauthenticated `/dashboard` and
  `/dashboard/users` redirect to login, OTA metadata remains `v1.1.13`, and the APK download
  still returns `WiwyMusic.apk` as `application/vnd.android.package-archive`.
- No user was deleted during deployment or verification. Android source, OTA metadata, R2 APK,
  mini-player, player, and playback files were not modified.

## Downloads lock and wrapped offline notice — published in v1.1.12

- The `Descargas` smart card on the Playlist/Library page now displays `PremiumLockBadge` for
  Free or unknown/loading plans. Tapping it opens `PremiumFeatureDialog`; Premium and Premium
  Plus show no lock and retain direct navigation to the download queue.
- Media3 may wrap the app's network `PlaybackException` inside an outer source error code 2000.
  `PlaybackErrorInfo` now inspects nested playback error codes, so wrapped network failures use
  the existing compact `Sin acceso a internet` notice instead of the red diagnostic card.
- Added unit coverage for wrapped network and timeout exceptions. All 37 universal debug unit
  tests and Kotlin debug compilation pass.
- Modified files: `ui/screens/WiwyLibraryScreen.kt`, `ui/player/PlaybackErrorInfo.kt`, and
  `ui/player/PlaybackErrorInfoTest.kt`. Commit: `4230b4c`.
- No mini-player, playback service, queue, player connection, or playback behavior changed.
- Published as v1.1.12 (`versionCode` 59). All 37 unit tests, signed R8 build, v2 signature,
  package/version inspection, private-string scan, immutable R2 archive verification, public
  metadata, and public APK comparison passed. SHA-256:
  `3fda82caeb0b8eade3dc68064548aacfac157288dd35ed19aaeb455de6c73cea`.
- Rollback tag `snapshot-before-download-offline-fix-v1.1.11` preserves commit `3da98c8` and
  was pushed before publication.

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
- Production release: `v1.1.21` (R2 stable OTA).
- `versionName`: `1.1.21`.
- `versionCode`: `68`.
- Production commit: `977d76c` (`fix(auth): prevent restored sessions after reinstall`).
- Last GitHub bridge release: <https://github.com/angelanda023-prog/WiwyMusic/releases/tag/v1.0.42>
- OTA asset name: `WiwyMusic.apk`.
- Production APK SHA-256: `d4ed2fad9197a40403d5505a042f50f49660ce50cff60e4d95595179775ab19f`.
- Previous production baseline: `v1.1.20`, commit `278d86a`, SHA-256
  `a944846ad38c48de6b076571718018adc12de3816e532e29b89ada24f9e103cd`.

The bridge release was published through both GitHub and R2. Clients upgrading from `v1.0.41`
can discover `v1.0.42` through the old GitHub updater; after installation, the Cloudflare
endpoint offers current stable `v1.1.21`.

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
  `ota/archive/v1.1.9/WiwyMusic.apk`, `ota/archive/v1.1.10/WiwyMusic.apk`, and
  `ota/archive/v1.1.11/WiwyMusic.apk`, `ota/archive/v1.1.12/WiwyMusic.apk`,
  `ota/archive/v1.1.13/WiwyMusic.apk`, `ota/archive/v1.1.14/WiwyMusic.apk`,
  `ota/archive/v1.1.15/WiwyMusic.apk`, `ota/archive/v1.1.16/WiwyMusic.apk`, and
  `ota/archive/v1.1.17/WiwyMusic.apk`, `ota/archive/v1.1.18/WiwyMusic.apk`, and
  `ota/archive/v1.1.19/WiwyMusic.apk`, `ota/archive/v1.1.20/WiwyMusic.apk`, and
  `ota/archive/v1.1.21/WiwyMusic.apk`; current `v1.1.21`
  is served from its immutable archive
  through `/api/ota/download`.
- R2 metadata `ota/releases.json` points to `v1.1.21` with the generic improvements message.
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

### Duplicate menu entry cleanup — deployed

- `Descargar APK` was removed from the shared desktop sidebar and mobile drawer because the
  same stable download action already exists in Admin Ajustes.
- `/dashboard/settings` retains its APK button and `/api/ota/download` remains unchanged.
- Modified Admin file: `src/components/AdminShell.tsx`. Commit: `f6e3b81`.
- Targeted ESLint, `tsc --noEmit`, and the Next.js production build passed. Existing local
  Durable Object and deprecated middleware warnings remain unchanged.
- Deployed Worker version: `d8b52018-8005-4780-ba44-b0f22733ec5d`. Production verification
  passed: root and Ajustes redirect unauthenticated access to login, OTA remains v1.1.12, and
  the download endpoint returns HTTP 200 as an Android APK.
- Android source, public OTA metadata, R2 archives, and the production APK were not changed.

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

## Admin logo favicon — deployed 2026-08-03

- The old generic `src/app/favicon.ico` was removed. `src/app/apple-icon.png` is an exact
  192×192 copy of the existing WiwyMusic `public/logo.png`; root metadata uses it for both
  `rel="icon"` and `rel="apple-touch-icon"`.
- `public/icon1.png` serves the same logo for older cached login HTML that still references the
  intermediate numbered icon path. Both icon paths are excluded from the authentication
  middleware so browsers can load them before login.
- Logo, favicon, Apple icon, and cached-path fallback all share SHA-256
  `597045aff8767130ce6b802bcd07ea4917a40cba7cfaee0cd10f8d89b4cdeffd`.
- Admin commits: `efa2e72` (`feat(admin): use logo as favicon`), `2b76672`
  (`fix(admin): avoid stale favicon cache`), `269b87a`
  (`fix(admin): serve logo favicon reliably`), and final compatibility commit `73bff42`
  (`fix(admin): preserve cached favicon path`). Rollback tag
  `snapshot-before-admin-logo-favicon-20260803` points to `e080db5`.
- Validation passed: `git diff --check`, ESLint, `tsc --noEmit`, Next.js production build,
  local HTML metadata inspection, and exact downloaded-image hash comparison.
- Deployed at <https://wiwymusic-admin.angelanda023.workers.dev> as Worker version
  `dea84bfd-acce-47d3-9268-de739b9f755c`. Production HTML emits both icon relations to
  `/apple-icon.png`; `/apple-icon.png` and legacy `/icon1.png` return the exact logo PNG.
- OTA metadata remains `v1.1.9`. No Android source, OTA metadata, R2 APK, mini-player, player,
  or playback file was modified.

## Admin user search and plan filters — deployed 2026-08-03

- Users page now filters instantly by name, email, Supabase user ID, redeemed Premium code,
  and current `Premium`/`Free` plan. Phone was intentionally excluded by user decision because
  it is not needed by the current account model.
- Responsive filter chips provide `Todos`, `Free`, `Premium`, and `Vencidos`. `Vencidos` matches
  the latest subscription when its status is `expired` or its expiration time has passed.
- User cards prefer Auth metadata name and retain email below it; cards without a stored name
  continue using email as the primary identity. Result count and a distinct empty-search state
  update while typing.
- `listUsersWithPlan()` joins `redeem_codes` and `redeem_code_redemptions`, including legacy
  `redeemed_by`, to support code lookup without a database migration. Filtering remains local
  over the existing Admin Auth fetch limit of 1,000 users.
- Modified Admin files: `src/app/dashboard/users/page.tsx`, `src/components/UserTable.tsx`,
  `src/components/AdminIcons.tsx`, and `src/lib/data.ts`.
- Validation passed: `git diff --check`, targeted ESLint, `tsc --noEmit`, Next.js production
  build, and OpenNext Cloudflare build. Existing Durable Object and deprecated middleware
  warnings remain unchanged.
- Admin commit: `837fee8` (`feat(admin): add user search filters`). Rollback tag
  `snapshot-before-admin-user-search-20260803` points to `73bff42`.
- Deployed at <https://wiwymusic-admin.angelanda023.workers.dev> as Worker version
  `fe7781f2-c0b0-444d-9e55-eff724ec5fea`. Production verification passed: unauthenticated root
  still redirects to login, OTA remains `v1.1.9`, and public APK SHA-256 remains
  `bd77d6cd0de3f9085dec257aa78342ca52428c6549a0ad90fda3a414034ede22`.
- Android, OTA metadata, R2 APK, mini-player, player, and playback files remain unchanged.

## Free, Premium, and Premium Plus — production SQL applied 2026-08-03

- Final account model has three visible tiers: `Free`, timed `Premium`, and lifetime
  `Premium Plus`. Premium and Premium Plus share the existing Premium feature gates;
  Premium Plus differs only by lifetime duration and label.
- `supabase/migrations/0005_three_subscription_tiers.sql` is the single production SQL to run.
  It adds `profiles.subscription_tier` with allowed values `free`, `premium`, and
  `premium_plus`; retains `profiles.is_premium` as the backward-compatible access flag; and
  classifies existing profiles without removing current access.
- A profile normalization trigger keeps older writers coherent. Timed grants and code redemptions
  become Premium; lifetime Admin grants become Premium Plus; conversion to Free clears both
  tier and access.
- The migration also installs/replaces `public.expire_premium_subscriptions()`, schedules it every
  minute through `pg_cron`, and runs it immediately. Elapsed timed subscriptions change to
  `expired` and their profiles to Free only when no other valid timed or lifetime subscription
  remains. Premium Plus rows use `expires_at = null` and never expire.
- Admin Dashboard, user cards, filters, detail, history, and grant controls distinguish Premium
  from Premium Plus. Admin commit: `108132f` (`feat(plans): add Premium Plus tier`).
- Android caches and observes the explicit tier while preserving the existing `isPremium` flow
  for all feature gates. Root Settings, shared settings headers, and Account display the three
  labels. Older cached Premium profiles safely fall back to Premium. Android commit: `cdf177c`
  (`feat(account): show three plan tiers`).
- Validation passed: Admin targeted ESLint, `tsc --noEmit`, Next.js build, OpenNext Cloudflare
  build, Android debug Kotlin compilation, and Android unit tests including new tier tests.
- Rollback tag `snapshot-before-three-plans-20260803` exists independently in both Admin and
  Android repositories. The earlier `0004` migration remains as historical precursor; `0005`
  is complete and supersedes it for production installation.
- Production migration `0005` was applied through Supabase SQL Editor. Its immediate expiration
  pass returned `0`, meaning no timed subscription was already overdue. A service-role REST
  verification returned the new column successfully with 5 Premium, 1 Premium Plus, 0 Free,
  and 0 tier/access inconsistencies at verification time.
- Admin was deployed at <https://wiwymusic-admin.angelanda023.workers.dev> as Worker version
  `722f55eb-0dd5-4742-9087-dfcfe11b236c`. Production root still redirects unauthenticated users
  to login. OTA remains `v1.1.9`; its public APK SHA-256 remains
  `bd77d6cd0de3f9085dec257aa78342ca52428c6549a0ad90fda3a414034ede22`.
- Android three-tier source is ready, but its OTA deployment remains pending. R2 APK,
  mini-player, player, and playback files remain unchanged.

## Spotify-style offline entitlement — published in v1.1.10

- On 2026-08-03 the user explicitly authorized the required protected change in
  `playback/MusicService.kt` after receiving the exact file, reason, playback risk, and
  UI-only alternative.
- Playback now selects its cache source for every new data source. Premium and Premium Plus use
  the existing `downloadCache` with `playerCache`/network fallback. Free and unknown/loading
  plans bypass `downloadCache` completely and use only normal streaming plus `playerCache`.
- Expiration from Premium to Free does not remove downloaded files. Offline copies become
  unavailable, while the same song can still stream normally with internet. Renewing Premium
  makes the preserved downloads usable again on subsequent reads.
- Existing download buttons remain protected by the same fail-closed entitlement helper. A
  nullable plan cannot start downloads or consume offline files.
- Modified files: `playback/MusicService.kt`, `playback/DownloadUtil.kt`, and
  `playback/OfflineDownloadAccessTest.kt`. Mini-player UI, dimensions, controls, animations,
  queue, and player connection remain unchanged.
- Validation passed: `git diff --check`, `:app:compileUniversalDebugKotlin`, and
  `:app:testUniversalDebugUnitTest`, including Premium, Free, and unknown-plan access tests.
- Published version is `1.1.10`, `versionCode` 57. Rollback tag
  `snapshot-before-offline-entitlement-v1.1.9` preserves the preceding source state.
- Signed R8 APK, v2 signature, package/version, private-string scan, immutable R2 archive,
  public metadata, and public download verification passed. Production SHA-256 is
  `dd16ed1c907886157652d441bd56c51406194ca5f6abb5c23481a35122ad7e9d`.
- Admin OTA metadata commit: `0590da0`. Android release source commit: `5d00398`.

### Free airplane-mode cache follow-up — included in v1.1.11

- Device testing found that Free could still start previously heard/downloaded online songs in
  airplane mode when a complete copy also existed in `playerCache`. The v1.1.10 split correctly
  bypassed `downloadCache`, but intentionally retained normal streaming cache for all plans.
- For remote music IDs, Free and unknown/loading plans now require an active network carrying
  both Android `NET_CAPABILITY_INTERNET` and `NET_CAPABILITY_VALIDATED` before any cache source
  opens. This prevents `playerCache` from becoming an offline bypass. Premium and Premium Plus
  retain offline playback. Real local-device `content://` and `file://` tracks remain available
  to every plan because they are user files, not WiwyMusic downloads.
- Modified protected file: `playback/MusicService.kt`, within the offline-entitlement scope
  explicitly authorized on 2026-08-03. Also modified `playback/DownloadUtil.kt` and its unit test.
  Mini-player UI, queue, controls, dimensions, animations, and player connection remain unchanged.
- Published in OTA v1.1.11 after signed release verification.
- Release is `versionCode` 58. Signed R8 APK, v2 signature, package/version, private-string scan,
  immutable R2 archive, public metadata, and public download verification passed. SHA-256:
  `bd183e314ced06c628e7f599f62b675135c5ef4fd482d30df6348d08a4c1a3f5`.
- Rollback tag `snapshot-before-free-offline-fix-v1.1.10` preserves commit `0823d5e` and was
  pushed to GitHub before publication.
- `ui/player/PlaybackError.kt` now renders `NoInternet` as a compact neutral Material card with
  cloud-off icon, localized title/message, and one Retry button. It hides code 2000, nested
  exception causes, Copy, and the red unknown-error treatment only for this known network state.
  Other playback errors retain their existing diagnostics. English and Spanish resources were
  added; playback behavior remains unchanged by this presentation-only follow-up.

## Admin suspended-subscription deletion — deployed

- `SubscriptionHistory` shows a red `Eliminar` action beside `Reactivar` only when the row is
  `suspended`. Browser confirmation clearly states that deletion is permanent.
- New `deleteSuspendedSubscription` Server Action revalidates the Supabase session against
  `ADMIN_EMAIL`, validates subscription/user IDs, reads the row, and refuses any status other
  than `suspended`. The final delete repeats ID, owner, and status filters to close race windows.
- Deletion removes only the selected suspended history row. It cannot delete active or expired
  subscriptions and does not change the user's current profile tier.
- Modified Admin files: `src/app/dashboard/actions.ts` and
  `src/components/SubscriptionHistory.tsx`. Admin commit: `8e12002`
  (`feat(admin): delete suspended subscriptions`).
- Validation passed: `git diff --check`, targeted ESLint, `tsc --noEmit`, Next.js production
  build, and OpenNext Cloudflare build. Existing local Durable Object and deprecated middleware
  warnings remain unchanged. Deployed Worker version:
  `5e96d14c-3ff2-4a85-98a8-3b97a5c96754`.

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
