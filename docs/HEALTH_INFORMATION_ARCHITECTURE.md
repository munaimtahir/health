# Health information architecture

The intended user-facing hierarchy is:

`Health` → summary, conditions, medicines, allergies, emergency information

`Timeline` → unified reverse-chronological activity, including symptoms and measurements

`Vault` → private documents and attachments

`Profile` → identity, security, export, backup, and personal health details

Symptoms are retained as a health-event type rather than treated as the product identity. The current implementation has begun this transition with structured condition, allergy, and measurement persistence and dashboard summaries.
