# RMS Environment Inspector

An offline Android diagnostic application for comparing physical Android and virtual Android environments. It reproduces the RMS emulator predicate, a Firebase Crashlytics-style root predicate, and RMS-style identifier attempts based on the supplied decompiled snippets.

**No network permission.** Device identifiers are masked by default; use **Copy REDACTED report** before sharing results. The utility's Android ID may differ from RMS because Android ID is scoped by signing key, user and device. Modern Android restricts access to IMEI, ICCID and hardware serial. These results may be placeholders or blocked.

## Build an APK using GitHub Actions (no local Android Studio required)

1. Create a **new GitHub repository** (public or private).
2. Extract the contents of this ZIP. Upload the **contents of the `RMS_Environment_Inspector` directory** to the repository root. Ensure `.github/workflows/build-apk.yml` is present (GitHub web upload can hide dotfolders; Git CLI is more reliable).
3. Commit the files to the `main` branch.
4. Open **Actions** → **Build Android APK** → **Run workflow**. A push to `main` also triggers a build.
5. After the job succeeds, open the workflow run and download **RMS-Environment-Inspector-debug-APK** under **Artifacts**.
6. Unzip that artifact and install `app-debug.apk` on the phone and in Virtual Master.

GitHub Actions requires repository Actions to be enabled. The workflow uses Ubuntu, Java 17, Gradle 8.7, Android Gradle Plugin 8.6.1 and compileSdk 35. GitHub's Ubuntu runner normally has Android SDK installed. This project does not bundle a Gradle wrapper; the workflow installs Gradle via setup-gradle.

## Local Android Studio

Open the project directory and build **Build → Build APK(s)**. Android Studio will resolve Gradle and SDK dependencies.

## Caveats

- The root check is the supplied Firebase Crashlytics algorithm, not a comprehensive Magisk test.
- The emulator flag is the supplied `VersionUtility.isDeviceEmulator()` algorithm, not a generic VM detector.
- An app running inside Virtual Master may observe different build values from an app on the host.
- The tool does not communicate with RMS servers or perform Play Integrity attestation.
- A successful build has not been verified in this environment.
