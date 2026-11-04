# Auditor Context: Forensic Integrity Verification

Target: Rigorous forensic audit across all deliverables (R1-R5).
Checks:
1. Static analysis: Genuine logic vs hardcoded strings/facades/dummies.
2. Runtime tracing & build validation: Execute build_and_deploy.ps1 and run_e2e_tests.ps1 -Strict directly, inspect return codes.
3. Bytecode validation: Inspect compiled classes in JARs (Alphabet, ChallengeHud, ChallengeSession, Packets, Progress, ChallengeManager$CoopManager).
4. Environment conformity: Check AGENTS.md rules (no bash syntax, no DLSS forcing, Windows 11 PowerShell, client/server mod segregation).
5. Output verdict: CLEAN or INTEGRITY VIOLATION.

Input Files:
- F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md
- F:\Jogos\Mine\.agents\teamwork\orchestrator\PROJECT.md
- F:\Jogos\Mine\TEST_READY.md
- All worker handoffs: F:\Jogos\Mine\.agents\teamwork\worker_m1\handoff.md, worker_m2\handoff.md, worker_m3\handoff.md, worker_m4\handoff.md
