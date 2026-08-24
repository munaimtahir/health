# Requirement Traceability Matrix

| ID | Requirement | Existing implementation | Persistence | UI workflow | Integration | Tests | Runtime evidence | Status | Gap | Planned phase |
|---|---|---|---|---|---|---|---|---|---|---|
| 1.1 | Conditions Details & CRUD | Partial (name, active, resolved, diagnosisDate, resolvedDate, treatingDoctor, notes, tags) | Room table exists | No CRUD screen (only add stub in viewmodel) | None | Basic | None | COMPLETE | Screen workflow (Add, Edit, Detail, Search, Timeline) | Phase 1 |
| 1.2 | Allergies Details & CRUD | Partial (allergen, category, reaction, severity, notes, status) | Room table exists | No CRUD screen (only add stub in viewmodel) | None | Basic | None | COMPLETE | Screen workflow (Add, Edit, Detail, Search, Timeline) | Phase 1 |
| 1.3 | Medicines & Current/Past split | Partial | Room table exists | Basic add, no edit/history views or prescription link | None | Basic | None | COMPLETE | Full medicines views (Current vs Past), prescription linking | Phase 1 |
| 1.4 | Procedures / Surgeries | Missing | Missing | Missing | Missing | Missing | None | COMPLETE | Room entity, DAO, UI CRUD, Timeline link | Phase 1 |
| 1.5 | Hospitalisations | Missing | Missing | Missing | Missing | Missing | None | COMPLETE | Room entity, DAO, UI CRUD, Timeline link | Phase 1 |
| 1.6 | Vaccinations | Missing | Missing | Missing | Missing | Missing | None | COMPLETE | Room entity, DAO, UI CRUD, Timeline link | Phase 1 |
| 1.7 | Devices / Implants | Missing | Missing | Missing | Missing | Missing | None | COMPLETE | Room entity, DAO, UI CRUD, Timeline link | Phase 1 |
| 1.8 | Family History | Missing | Missing | Missing | Missing | Missing | None | COMPLETE | Room entity, DAO, UI CRUD | Phase 1 |
| 2.0 | Emergency Health Profile | Partial | Profile table | Edit personal details in Profile | None | None | None | COMPLETE | Emergency summary card with clear privacy controls | Phase 2 |
| 3.0 | Health Vault Rearchitecture | Partial | DocumentEntity | Flat list of documents | None | None | None | PARTIAL | Categorization cards, sorting/filtering, clean UX | Phase 3 |
| 4.0 | Laboratory Reports | Partial | DocumentEntity (as generic doc) | Generic import | None | None | None | PARTIAL | Structured metadata (test name, lab, category, date, doc link), UI entry | Phase 4 |
| 5.0 | Radiology & Imaging | Partial | DocumentEntity | Generic import | None | None | None | PARTIAL | Structured modality details | Phase 5 |
| 6.0 | Prescriptions | Partial | DocumentEntity | Generic import | None | None | None | PARTIAL | Doctor, specialty, date fields, link to Medicines | Phase 6 |
| 7.0 | Medical Certificates | Partial | DocumentEntity | Generic import | None | None | None | PARTIAL | Certificate type, doctor, validity fields, UI entry | Phase 7 |
| 8.0 | Health Media | Partial | Symptom attachment | Only attached to symptoms | None | None | None | PARTIAL | First-class health media (rash, wound, etc.), metadata, gallery | Phase 8 |
| 9.0 | Camera, Video, Gallery capture | Partial | System photo picker / file picker | System open doc launcher | None | None | None | PARTIAL | System camera integration | Phase 9 |
| 10.0 | Structured Health Tracking | Partial | MeasurementEntity | Basic add stub, recent listing | None | None | None | PARTIAL | Dedicated tracking screen with BP, glucose, temp, weight, pulse, SpO2, resp rate | Phase 10 |
| 11.0 | Tracking History & Charts | Missing | Missing | Missing | Missing | Missing | None | MISSING | Real interactive charts (BP, glucose, temp, weight), period filters | Phase 11 |
| 12.0 | Unified Health Timeline | Partial | HealthEventEntity | Flat event list | None | None | None | COMPLETE | Derived timeline events for all profile changes, reports, vaccinations, and measurements | Phase 12 |
| 13.0 | Dashboard / Home Redesign | Partial | HomeScreen | Basic summary | None | None | None | COMPLETE | Complete overview of profile summary, current medications, latest measurements, quick add | Phase 13 |
| 14.0 | Global Health Search | Missing | Missing | Missing | Missing | Missing | None | MISSING | Search across all domains, local-only indexing and queries | Phase 14 |
| 15.0 | Vault Filtering & Sorting | Missing | Missing | Missing | Missing | Missing | None | MISSING | Category-specific filters (modality, doctor, laboratory, etc.) | Phase 15 |
| 16.0 | Cross-Linked Health Context | Missing | Missing | Missing | Missing | Missing | None | MISSING | Optional links between conditions, medications, reports, prescriptions | Phase 16 |
| 17.0 | Health Profile Report / PDF | Partial | Profile pdf print | PDF report generation | None | None | None | COMPLETE | Support new domains in PDF report | Phase 17 |
| 18.0 | Backup & Restore v1.1 | Partial | Crypt/Backup | Encrypted backups for existing tables | None | None | None | COMPLETE | Include all new v1.1 tables in backup zip/json, test compatibility | Phase 18 |
| 19.0 | Legacy Data Migration | Partial | SQLite migrations | Migrations 1 to 9 | None | None | None | COMPLETE | Implement Migration 9 to 10 for new tables, verify data integrity | Phase 19 |
| 20.0 | Feature Modularization | Partial | Multi-module | Placeholder modules, app does all heavy lifting | None | None | None | PARTIAL | Clean up domain boundaries, viewmodels, modular dependencies | Phase 20 |
| 21.0 | ViewModel Decomposition | Missing | ViewModels | Single shared ViewModel | None | None | None | MISSING | Scope ViewModels specifically | Phase 21 |
| 22.0 | Remove Context from ViewModels | Partial | ViewModels | Pass Context to ~11 ViewModel methods | None | None | None | COMPLETE | Refactor to injected application-scoped abstractions (StorageProvider, FileService) | Phase 22 |
| 23.0 | Paging 3 | Missing | Room query | Non-paginated Timeline list | None | None | None | MISSING | PagingSource for Timeline and large datasets | Phase 23 |
