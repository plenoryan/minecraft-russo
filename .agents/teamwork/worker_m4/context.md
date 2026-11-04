# Worker M4 Context: Build Pipeline, Dual Deployment & Canonical E2E Verification

## Assigned Scope
Implement Milestone M4 (Requirement R5):
1. Create `F:\Jogos\Mine\build_and_deploy.ps1`:
   - Compiles all `src/**/*.java` using OpenJDK 17 `javac` against the complete classpath.
   - Injects the compiled `.class` files into `sayitenglish-fabric-1.20.1-1.1.0.jar` using `jar.exe uf`.
   - Deploys the resulting JAR to `Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar` and `Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`.
2. Fix the PowerShell 5.1 Cyrillic literal decoding in `tests\run_e2e_tests.ps1` (line 740 for `T1_F29`) using standard Base64 `$RU.pol_eto_lava` or `b64u`.
3. Execute `build_and_deploy.ps1` and verify clean exit code 0 and updated JAR timestamps.
4. Execute `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict` and verify 55/55 tests pass with exit code 0.
5. Verify dedicated server smoke test with the newly deployed JAR (`fabric-server-launch.jar --help`).

## Input Files
- Original Request: F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md
- Master Project: F:\Jogos\Mine\.agents\teamwork\orchestrator\PROJECT.md
- Worker M1 Handoff: F:\Jogos\Mine\.agents\teamwork\worker_m1\handoff.md
- Worker M2 Handoff: F:\Jogos\Mine\.agents\teamwork\worker_m2\handoff.md
- Worker M3 Handoff: F:\Jogos\Mine\.agents\teamwork\worker_m3\handoff.md
- Test Writer Handoff: F:\Jogos\Mine\.agents\teamwork\test_writer_e2e\handoff.md

## Owned Files (Strict Exclusive Ownership)
- F:\Jogos\Mine\build_and_deploy.ps1
- F:\Jogos\Mine\tests\run_e2e_tests.ps1
- F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar
- F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar
