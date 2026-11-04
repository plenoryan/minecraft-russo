## 2026-11-04T12:16:24Z
You are a teamwork_test_writer.
Your Working Directory is: F:\Jogos\Mine\.agents\teamwork\test_writer_e2e\
You MUST read F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md before starting work.
Also inspect your context file at F:\Jogos\Mine\.agents\teamwork\test_writer_e2e\context.md, the master project plan at F:\Jogos\Mine\.agents\teamwork\orchestrator\PROJECT.md, and the explorer survey reports at F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_3\analysis.md and F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_3\handoff.md.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

EXCLUSIVE FILE OWNERSHIP:
You own exclusively:
- F:\Jogos\Mine\TEST_INFRA.md
- F:\Jogos\Mine\TEST_READY.md
- F:\Jogos\Mine\tests\run_e2e_tests.ps1
Do NOT edit any source code or implementation files.

TASKS:
1. Create F:\Jogos\Mine\TEST_INFRA.md following the Project Pattern template:
   - Test Philosophy: requirement-driven, opaque-box, Category-Partition + BVA + Pairwise + Workload testing.
   - Feature Inventory mapping all features from PROJECT.md to Tiers 1-4.
   - Test Architecture & runner specification.
   - Coverage thresholds (minimum 55 tests).
2. Extend F:\Jogos\Mine\tests\run_e2e_tests.ps1:
   - Preserve all existing 45 test cases intact.
   - Implement the 10 new rigorous test cases designed in Explorer 3's analysis (T1_F24 to T1_F32 and T4_07):
     * T1_F24: Speech HUD suppression on single-word / item challenges
     * T1_F25: Speech HUD display on full sentences with compact box and header prefix "disse: "
     * T1_F26: Cyrillic identical letters natural substitution (А, О, Е, Т, К, М)
     * T1_F27: Cyrillic non-identical letters gradual progression (no premature lock on oak_log/wood)
     * T1_F28: Cooperative phrase invocations (item request, waypoint beacon, follow me)
     * T1_F29: Dynamic/kamikaze mini-challenges (Floor is Lava, movement)
     * T1_F30: Cooperative ranking and scoring system (5 tiers: Новичок to Легенда)
     * T1_F31: QoL mods installation and client/server segregation (AGENTS.md L13)
     * T1_F32: Dedicated server dry-run clean execution
     * T4_07: Integrated cooperative multiplayer session scenario
   - Ensure the test runner outputs clear structured progress, counts total passed/failed, and exits with code 0 on pass or code 1 on failure.
3. Publish F:\Jogos\Mine\TEST_READY.md when complete.

Document your work and write your handoff report to F:\Jogos\Mine\.agents\teamwork\test_writer_e2e\handoff.md.
When finished, send a message to orchestrator (conversation ID: dd52151e-9fc1-4341-8ae9-2b8120a8bfb1).
