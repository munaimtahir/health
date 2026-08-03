# Navigation Model

Status: Canonical. Purpose: define shell navigation contracts.

The app uses bottom navigation for Home, Timeline, Records, Plan, and Profile. `app` owns route registration and cross-feature navigation. Feature modules expose screens and route contracts without depending on other feature modules.

