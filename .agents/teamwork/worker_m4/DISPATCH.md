## 2026-11-04T12:57:20Z
You are a teamwork_worker.
Your Working Directory is: F:\Jogos\Mine\.agents\teamwork\worker_m4\
You MUST read F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md before starting work.
Also inspect your context file at F:\Jogos\Mine\.agents\teamwork\worker_m4\context.md, the master project plan at F:\Jogos\Mine\.agents\teamwork\orchestrator\PROJECT.md, and the worker handoff reports at F:\Jogos\Mine\.agents\teamwork\worker_m1\handoff.md, F:\Jogos\Mine\.agents\teamwork\worker_m2\handoff.md, F:\Jogos\Mine\.agents\teamwork\worker_m3\handoff.md, and F:\Jogos\Mine\.agents\teamwork\test_writer_e2e\handoff.md.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

EXCLUSIVE FILE OWNERSHIP:
You own exclusively:
- F:\Jogos\Mine\build_and_deploy.ps1
- F:\Jogos\Mine\tests\run_e2e_tests.ps1
- F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar
- F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar
Do NOT edit any source code files in src/.

TASKS:
1. Create F:\Jogos\Mine\build_and_deploy.ps1:
   - Compiles all Java source files in F:\Jogos\Mine\src with OpenJDK 17 javac using the verified classpath (sayitenglish jar, client-intermediary.jar, Jade, and runtime libraries).
   - Injects the compiled .class files into sayitenglish-fabric-1.20.1-1.1.0.jar using jar.exe uf.
   - Deploys the freshly built jar into both F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar and F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar.
   - Verifies file existence, non-zero size, and updated timestamps.
2. In F:\Jogos\Mine\tests\run_e2e_tests.ps1:
   - Fix line 740 for test T1_F29 where raw UTF-8 Cyrillic literals ("пол", "лава") caused Windows PowerShell 5.1 to fail regex matching due to Windows-1252 default encoding. Use the pre-existing Base64 decoder $RU.pol_eto_lava or in-memory UTF-8 decoding, matching the pure 7-bit ASCII standard of the script.
3. Execution & Verification:
   - Execute build_and_deploy.ps1 and verify clean output and exit code 0.
   - Execute:
     powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
     Verify 100% of the 55 tests PASS with exit code 0.
   - Execute dedicated server dry-run smoke test:
     powershell -NoProfile -Command "Set-Location 'F:\Jogos\Mine\Servidor_Dedicado'; & 'F:\Jogos\Mine\Java17\bin\java.exe' '-Dfabric.dli.main=net.fabricmc.loader.impl.launch.server.FabricServerLauncher' -jar 'F:\Jogos\Mine\Servidor_Dedicado\fabric-server-launch.jar' --help"
     Verify exit code 0 and clean startup.

Document all changes in F:\Jogos\Mine\.agents\teamwork\worker_m4\changes.md and write a complete handoff report to F:\Jogos\Mine\.agents\teamwork\worker_m4\handoff.md.
When finished, send a message to orchestrator (conversation ID: dd52151e-9fc1-4341-8ae9-2b8120a8bfb1).
