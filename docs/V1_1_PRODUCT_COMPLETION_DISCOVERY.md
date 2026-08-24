# V1.1 Product Completion Discovery

## Project Identity Shift
The application is transitioning from a symptom logger to a complete personal longitudinal health record and health passport.

## Baseline Analysis
- **App Module**: `com.vexel.passport`, versionCode 5, versionName 1.0.5
- **Modules**:
  - `:app` - central NavHost, routes, main activity, single shared ViewModel (PassportViewModel).
  - `:core:database` - Room DB with 9 tables, explicit migrations.
  - `:core:model` - shared models.
  - `:core:files` - secure storage for PDFs, images, etc.
  - `:core:security` - encryption services.
  - `:feature:*` - placeholder scaffolding (empty files, reserved for future modularization).
- **Tooling**: Compile/Target SDK 36, Kotlin 2.0.21, Gradle 8.9.

## Target Architecture & UI Changes
1. **Database Schema Evolving to v10**:
   - Add new tables/entities for `Procedures`, `Hospitalisations`, `Vaccinations`, `Devices`, and `FamilyHistory`.
   - Implement DB migration `MIGRATION_9_10`.
   - Update `ConditionEntity` and `AllergyEntity` if needed.
2. **Tab Navigation**:
   - `Health` (formerly Home/Dashboard)
   - `Timeline` (formerly Records)
   - `+` (Universal Add Action Sheet)
   - `Vault` (Document store)
   - `Profile`
3. **Structured Vault**:
   - Categorize files: Laboratory Reports, Radiology Reports, Prescriptions, Medical Certificates, Health Media, and Other Documents.
4. **Structured Health Tracking**:
   - Trackers for Blood Pressure, Blood Glucose, Temperature, Weight, Pulse, SpO₂, and Respiratory Rate.
   - History and interactive graphs/charts.
5. **Timeline Expansion**:
   - Render all actions, medications, vaccinations, procedures, and trackers chronologically.
6. **Global Search**:
   - Unified search across conditions, medications, doctors, clinics, tests, and reports.
