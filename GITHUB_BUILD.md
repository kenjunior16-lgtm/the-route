# Building The Route on your phone (GitHub Actions)

1. Create a new PRIVATE repo on github.com, e.g. `the-route`. Leave it empty (no README, or an empty README is fine).
2. In Muse's GitHub App installation, make sure that repo is selected, then save.
3. Muse will upload all text files except `.github/workflows/android.yml` (GitHub blocks workflow files via the API) and except the binary Gradle wrapper jar.
4. Create `.github/workflows/android.yml` yourself in the GitHub web UI, at exactly that path, with the workflow content Muse gives you. Do not nest it under another `android.yml` folder.
5. Push / commit to `main` triggers Actions. Green = download the artifact `the-route-debug-apk`, unzip it, install `app-debug.apk`.
6. Install over the previous build first if you have one; only uninstall first if Android refuses with a signature mismatch (debug builds from different runners can differ).

Permissions the app asks for: Location (tracking), Notifications, Microphone (Vibe Check only, 5 seconds, on-device, discarded).
