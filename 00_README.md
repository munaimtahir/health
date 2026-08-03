# Vexel Health Passport — AI Development Pack

## Product
**Vexel Health Passport** is a private, offline-first Android application for one person to store medical records, track symptoms and treatment changes, maintain medication history, receive follow-up/check-up reminders, and create a concise appointment summary.

**Tagline:** Your health history, organized.

**Recommended package name:** `pk.vexel.healthpassport`

## Core promise
The application answers four questions:
1. What has happened to my health?
2. Where are my reports and prescriptions?
3. What symptoms or treatment changes occurred over time?
4. What follow-up is due next?

## Pack contents
- Product requirements and scope
- UX and screen specification
- Android architecture
- Data model
- Sprint-by-sprint build plan
- Quality gates and test strategy
- Privacy and security requirements
- Play Store and release checklist
- Master AI development-agent prompt
- Market and visual concept reference

## Recommended implementation order
1. Read `01_PRODUCT_REQUIREMENTS.md`.
2. Read `02_UX_SCREEN_SPEC.md` and `03_TECHNICAL_ARCHITECTURE.md`.
3. Implement strictly through `05_SPRINT_PLAN_AND_GATES.md`.
4. Run all checks in `06_QA_TEST_STRATEGY.md` and `07_SECURITY_PRIVACY.md`.
5. Use `10_MASTER_AI_AGENT_PROMPT.md` as the controlling prompt for the coding agent.

## Non-negotiable product boundary
This is a health-record organization and symptom-logging application. It must not diagnose disease, recommend treatment, advise medication changes, or independently determine when a medical test is required.
