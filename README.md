# Sparrow

Sparrow is a small virtual bird that lives as a floating overlay on your
Android home screen and over other apps — not inside its own app screen.
Tap it for a greeting or battery status, drag it around, long-press for a
quick menu, and stop it any time from its notification.

## What Sparrow does

- Draws a small, original, code-drawn sparrow (no imported artwork) in a
  system overlay window that floats above your home screen and other apps.
- Reacts to taps (greeting/battery message), long-presses (a small menu),
  and drags (move it anywhere on screen).
- Occasionally flies to a new spot near a screen edge on its own, unless you
  turn that off in Settings.
- Shows a small, ever-present notification while it's running, with
  pause/resume, stop, and settings actions.
- Works entirely offline. There is no account, backend, analytics, or ads.

## Why overlay permission is required

Everything above depends on Android's **"Display over other apps"**
permission (`SYSTEM_ALERT_WINDOW` + `TYPE_APPLICATION_OVERLAY`). This is the
only way an app can draw on top of your home screen and other apps, and
Android requires you to grant it yourself in system Settings — no app can
turn it on for itself. Sparrow explains this before asking, and you can
revoke it at any time (see below); Sparrow detects that and shuts itself
down safely.

## Android overlay limitations

By design, and because of Android platform restrictions, Sparrow:

- **Can** appear over your home screen, over ordinary apps, and while you
  switch between apps.
- **Cannot** appear over the lock screen, system permission dialogs, other
  secure system surfaces, or some banking/security-sensitive apps that
  deliberately block overlays.
- Never captures screenshots, records the screen, or reads content from
  other apps — the overlay window only draws its own small bird and speech
  bubble; it has no way to see what's underneath it.

## Privacy

Sparrow works offline and stores only:

- The name you give it (local `DataStore` preferences, on-device only)
- Your settings (pet size, movement preferences, toggles, last position)

It never collects analytics, never uses the network, never reads other
apps' content, and never uses the microphone, camera, or an accessibility
service.

## Building locally

Requirements: JDK 17.

```bash
./gradlew test         # unit tests
./gradlew lint          # Android lint
./gradlew assembleDebug # debug APK
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

**About the Gradle wrapper:** this repo intentionally does **not** commit
`gradlew`, `gradlew.bat`, or `gradle/wrapper/gradle-wrapper.jar` — the jar
is a binary file, and generating one by hand outside of a real Gradle
install isn't something to do reliably. The included
`gradle/wrapper/gradle-wrapper.properties` pins the intended version
(Gradle 8.9). To materialize the wrapper locally, either:

- Open the project in **Android Studio** — it detects the missing wrapper
  and offers to generate it automatically, or
- Run `gradle wrapper --gradle-version 8.9 --distribution-type bin` once
  from a local Gradle install.

After that, `./gradlew` works normally for every command above. CI does
this automatically on every run (see below), so you don't need Android
Studio or a local Gradle install just to get a built APK.

## Running tests

```bash
./gradlew test
```

Unit tests cover name validation, greeting selection, battery message
selection, battery percentage normalization, position boundary/nearest-edge
calculations, movement state-machine transitions, and saved-settings
clamping — all as fakes/pure functions, so none of them need a real device,
real overlay permission, or a real battery.

## Getting the APK from GitHub Actions

Every push to `main`, every pull request into `main`, and manual runs
(`workflow_dispatch`) trigger `.github/workflows/android.yml`, which tests,
lints, and builds a debug APK, then uploads it as a workflow artifact named
**`sparrow-debug-apk`**. To download it: open the workflow run under the
repo's **Actions** tab, and grab the artifact from the run summary page.
This debug build requires no signing secrets.

## Starting and stopping Sparrow

- **Start:** open the app, finish the one-time setup (name → overlay
  permission → ready), then tap **Start Sparrow**. You can also start it
  again later from the Settings screen.
- **Stop:** any of — the **Stop** action in Sparrow's notification, **Stop
  Sparrow** in its long-press menu, or the **Stop Sparrow** button in
  Settings. All three immediately remove the overlay window.

## Revoking overlay permission

Android Settings → Apps → Sparrow → **Display over other apps** → turn it
off. Sparrow checks for this periodically while running and shuts itself
down safely (no crash) if permission is revoked — you'll need to grant it
again (from Settings → "Overlay permission") to start Sparrow again.

## Foreground-service notification

While Sparrow is running, Android requires — and Sparrow always shows — a
persistent notification ("Sparrow is flying — Your floating pet is
active") with **Pause/Resume**, **Stop**, and **Settings** actions. This
notification is never hidden while the service runs; on Android 13+ it
also requires the `POST_NOTIFICATIONS` permission, which Sparrow requests
the first time you start it (Sparrow still works if you decline — you'd
just lose the visible notification, not the pet itself).

## Release signing

Release builds (`./gradlew assembleRelease`) are **unsigned** in this
repo — no keystore or signing secrets are included or referenced anywhere,
by design, so the debug CI workflow never depends on secrets. To produce an
installable release build:

1. Generate a keystore: `keytool -genkeypair -v -keystore release.keystore -alias sparrow -keyalg RSA -keysize 2048 -validity 10000`
2. Add a `signingConfigs { release { ... } }` block to `app/build.gradle.kts`
   referencing that keystore (via `gradle.properties`/environment
   variables — never commit keystore passwords), and reference it from
   `buildTypes.release`.
3. If you also want a release CI job, add the keystore and its passwords as
   GitHub Actions **encrypted secrets**, decode the keystore in a workflow
   step, and run `./gradlew assembleRelease` with the signing properties
   passed in as `-P` Gradle properties.

## Google Play policy considerations

If you plan to publish Sparrow (or an app like it) on Google Play, review
Play's current policies before doing so — they change independently of
this codebase:

- **"Display over other apps" (`SYSTEM_ALERT_WINDOW`)** is a
  Play-restricted permission with its own declaration/justification
  requirements in Play Console.
- **Foreground services** must declare an accurate
  `foregroundServiceType` (this project uses `specialUse`, with the
  required `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` justification in the
  manifest) and are subject to Play's foreground-service policies,
  including keeping the use case genuinely user-initiated and visible via
  the persistent notification, exactly as this app already does.
- Google Play policies are updated periodically — check the current
  [Play Console policy center](https://support.google.com/googleplay/android-developer/topic/9858052)
  before submission rather than relying solely on this document.

## A note on tooling versions

This project intentionally targets **AGP 8.7.2 / Kotlin 2.1.0 / Gradle
8.9** rather than the newest available major versions at the time you're
reading this. Android's tooling moves fast (including a substantial
Kotlin-integration redesign in newer AGP releases), and this combination
was chosen because it's a thoroughly documented, stable pairing — the goal
is a project that reliably compiles the first time you run CI, not one
that chases the latest release. Feel free to upgrade (`gradle/libs.versions.toml`
is the single place to bump versions) once you've confirmed a green build.
