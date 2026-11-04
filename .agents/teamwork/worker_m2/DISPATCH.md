## 2026-11-04T12:16:10Z
You are a teamwork_worker.
Your Working Directory is: F:\Jogos\Mine\.agents\teamwork\worker_m2\
You MUST read F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md before starting work.
Also inspect your context file at F:\Jogos\Mine\.agents\teamwork\worker_m2\context.md, the master project plan at F:\Jogos\Mine\.agents\teamwork\orchestrator\PROJECT.md, and the explorer survey reports at F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_3\analysis.md and F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_3\handoff.md.

MANDATORY INTEGRITY WARNING:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

EXCLUSIVE FILE OWNERSHIP:
You own exclusively:
- F:\Jogos\Mine\download_qol_mods.ps1
- F:\Jogos\Mine\Instancia_Cliente\mods\ (new QoL mod JARs only)
- F:\Jogos\Mine\Servidor_Dedicado\mods\ (new QoL mod JARs only)
Do NOT edit any other files.

TASKS:
1. Requirement R4 (Integration of QoL and Dynamism Mods):
   - Create F:\Jogos\Mine\download_qol_mods.ps1 using PowerShell to download and place the 6 verified Fabric 1.20.1 QoL mods from Modrinth CDN (as detailed in handoff.md Section 1.3):
     * omnimovement-fabric-1.0+mc1.20.1.jar (Client only)
     * InventorySorter-1.9.0-1.20.jar (Client & Server)
     * stack-to-nearby-chests-mc1.20.1-0.5.10.jar (Client only)
     * entityculling-fabric-1.11.3-mc1.20.1.jar (Client only)
     * krypton-0.2.3.jar (Client & Server)
     * appleskin-fabric-mc1.20.1-2.5.2.jar (Client only)
   - Ensure strict client/server segregation per AGENTS.md L13: client-only mods must NOT be copied into Servidor_Dedicado\mods\.
   - Run the script and verify all files exist with non-zero byte sizes and matching SHA-512 hashes.
2. Verification:
   - Perform dedicated server dry-run smoke test:
     & "F:\Jogos\Mine\Java17\bin\java.exe" -Dfabric.dli.main=net.fabricmc.loader.impl.launch.server.FabricServerLauncher -jar "F:\Jogos\Mine\Servidor_Dedicado\fabric-server-launch.jar" --help
     Verify clean startup output without ClassNotFoundException or mod resolution failures.

Document all changes in F:\Jogos\Mine\.agents\teamwork\worker_m2\changes.md and write a complete handoff report to F:\Jogos\Mine\.agents\teamwork\worker_m2\handoff.md.
When finished, send a message to orchestrator (conversation ID: dd52151e-9fc1-4341-8ae9-2b8120a8bfb1).
