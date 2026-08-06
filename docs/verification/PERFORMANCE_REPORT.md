# Performance report

The model-layer acceptance suite now summarizes 10,000 synthetic symptom events, verifies exact counts, and enforces a 5-second upper bound for the pure summary calculation. The timeline uses a lazy list and Room Flow; PDF/export/backup remain I/O-heavy operations that are verified by build and workflow tests rather than a device benchmark. No ANR or unbounded-memory defect was observed in the current connected run. Large PDF/export/backup device benchmarking remains an acceptance limitation and is being addressed in the remaining hardening pass.
