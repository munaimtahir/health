# Health tracking architecture

Tracking is represented by `MeasurementEntity` with a stable `type` value and optional secondary value. This supports:

- `BLOOD_PRESSURE`: systolic/diastolic plus optional pulse in future extension;
- `BLOOD_GLUCOSE`: value, unit, context;
- `TEMPERATURE`: value, unit, context/site;
- `WEIGHT`: value, unit;
- future pulse, SpO₂, respiratory-rate, and custom measurements.

Symptoms remain `HealthEventEntity(kind = "SYMPTOM")`, preserving timestamps, severity, duration, notes, triggers, related medication, episode IDs, and attachment links. This is intentionally non-diagnostic: the app records user-entered measurements and neutral history only.
