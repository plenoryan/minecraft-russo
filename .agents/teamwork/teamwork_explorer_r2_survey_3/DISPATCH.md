# DISPATCH — Explorer 3 (Survey: Mods Integration, Russian Vocabulary & E2E Test Suite)

**Objective**: Survey integration of Farmer's Respite and Comforts (Fabric 1.20.1), Russian vocabulary adaptation for Handcrafted/Farmer's Delight/Respite/Comforts, and E2E test suite extensions.

**Authoritative Requirements**: `F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md` (read lines 48-89 carefully).

**Scope of Investigation**:
1. Mods Integration (R4):
   - Current mod files in `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`.
   - Farmer's Respite (tea, coffee, kettle) and Comforts (sleeping bags, hammocks) for Fabric 1.20.1: versions, download sources/scripts, client vs server segregation (or both), dependencies.
   - Handcrafted and Farmer's Delight items/blocks currently in the game.
2. Russian Vocabulary & Tooltips:
   - Where vocabulary, words, tooltips, translations, and pronunciation support are stored in SayIt (`src/com/spicesfire/sayitenglish/` or assets/data/json).
   - How to adapt vocabulary and tooltips for furniture and daily life items from Handcrafted, Farmer's Delight, Farmer's Respite, and Comforts.
3. Automated Tests (R4 / Acceptance Criteria):
   - Analyze `tests\run_e2e_tests.ps1` (55 tests from round 1).
   - Determine what new automated test assertions must be added to cover:
     - Radial wheel registration and opening ('V')
     - Russian speech challenge gating (strict conditional dispatch on success)
     - Receiver visual notification with initiator head and item '?' icon
     - Iris shader reload 'R' unbinding
     - Farmer's Respite and Comforts mod installation & Russian vocabulary/tooltips.

**Output**: Write a comprehensive, verified handoff report to `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_3\handoff.md`.


## 2026-11-07T05:54:54Z
You are survey_exp_3, working as an Explorer on the SayIt mod (Minecraft 1.20.1 Fabric).
Working directory: F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_3
Authoritative Requirements: F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md
Your Dispatch Task: F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_3\DISPATCH.md

Investigate:
1. Mods Integration (R4):
   - Check mods in Instancia_Cliente\mods\ and Servidor_Dedicado\mods\.
   - Farmer's Respite and Comforts for Fabric 1.20.1: jars, dependencies, segregation rules, automated download/setup scripts.
   - Handcrafted and Farmer's Delight items/blocks in the current instance.
2. Russian Vocabulary & Tooltips:
   - Identify how vocabulary, tooltips, translations, and pronunciation support are structured in SayIt (src/com/spicesfire/sayitenglish/ or resources).
   - How to adapt vocabulary and tooltips for furniture/daily life items from Handcrafted, Farmer's Delight, Farmer's Respite, and Comforts.
3. Automated Tests:
   - Examine tests\run_e2e_tests.ps1 (55 tests from round 1).
   - Propose exact new test assertions to validate:
     * Radial wheel UI ('V')
     * Voice challenge gating (strict conditional dispatch on success)
     * Receiver visual notification (head + item '?')
     * Iris key 'R' unbinding
     * Farmer's Respite + Comforts integration & Russian vocabulary/tooltips.

Write your verified findings and recommendations to F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_3\handoff.md and report back via send_message.
