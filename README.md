# The Route — NOLA Second Line Tracker (Android)

A fresh Android build from KJ's Emergent spec, not a copy of the Emergent
code (which KJ did not export). Built like Dash Strategist: Kotlin +
Jetpack Compose, Room local storage, OpenStreetMap via osmdroid, GPS
foreground tracking.

## What this first build does
- Home / Parades / Vendors / Profile, portrait by default with a rotation toggle in Profile
- Lead a Parade: GPS live route, breadcrumbs, Home Base, end route
- Map: OpenStreetMap, live pin, breadcrumbs, Home Base, vendor and report pins
- Drop a Plate: vendor name, Plate/Drink/Water, price, at current GPS, expires after 4 hours
- Vendors: All/Plate/Drink/Water filters, featured/promoted highlighting
- Community reports: only within 200m of the live parade pin
- Vibe Check: 5-second on-device mic sample, BPM estimate, 🎺 under 90, 🔥 over 120, audio discarded, never recorded or sent
- Leaders: registration (Pending), 2 vouches from verified leaders to go Active, admin verify/ban
- Members (backend-phase model, KJ 2026-11-03): invite code alone does NOT join a club — the leader must approve each person who tries to join. Approved members can go live independently (not only on a leader's route), member dots show only when zoomed in on the map, and member streams stay live independently if the leader goes offline. Leader manual shutdown for moderation is retained as a deliberate action, separate from auto-ending.
- Payments: TEST MODE ONLY. The $10 annual membership and $5 vendor promotion buttons do not charge anything. Real Square checkout needs a backend and securely-configured sandbox keys, not keys pasted in chat.

## What it does NOT do yet (stated honestly)
- It does NOT share a live route with other phones. There is no backend in
  this build; routes, vendors, reports and leaders are local to this phone.
  Cross-phone live sync is the next backend step, not claimed here.
- No real Square charge is made.

## Building (phone-only, GitHub Actions)
See GITHUB_BUILD.md. The workflow downloads the Gradle wrapper jar at
build time, because the binary jar is not pushed via the GitHub API.
Gradle 8.7 is required (AGP 8.5.2 needs 8.7+).
