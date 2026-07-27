# GeoPilot

GeoPilot is a privacy-first Android location simulation workspace for QA and development. It uses Android's official mock-location mechanism and does not require root access.

## Current scope

- Modern adaptive Compose interface for phones and tablets
- Interactive coordinate canvas and exact numeric input
- Altitude, bearing, accuracy, speed, and update interval model
- Persistent simulation configuration
- Foreground location service with start and stop controls
- Device-readiness guidance
- Dark and light Material 3 themes
- Automated lint, tests, checksums, APK artifacts, and tagged releases

Dashboard, reusable scenarios, routes, and run history are represented in navigation but intentionally deferred from the first functional delivery.

## Device setup

1. Install the debug APK from the GitHub Actions artifact.
2. Enable Android Developer options.
3. Open `Select mock location app` and select GeoPilot.
4. Grant precise location and notification permissions.
5. Choose coordinates and start the simulation.

Android and target applications may identify locations as mocked. GeoPilot does not attempt to conceal that signal.

## GitHub build

Push the project to a GitHub repository. The `Android CI` workflow runs lint and unit tests, then publishes `app-debug.apk` as a workflow artifact.

Creating a tag such as `v0.1.0` also creates a GitHub Release containing an **unsigned** release APK and SHA-256 checksum. Debug APK artifacts are signed automatically with Android's generated debug key and can be installed directly.

## Local commands

```shell
./gradlew lint test assembleDebug
```

The project requires JDK 17. Android Studio is optional when GitHub Actions is used for builds.

## Security and privacy

- No analytics or network permissions
- No API keys or secrets
- Configuration remains on-device
- Release signing credentials are never stored in the repository

## License

Copyright (c) 2026. All rights reserved until a project license is selected.
