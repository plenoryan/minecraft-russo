# DISPATCH — Worker M1 (Iris Key Unbind & Everyday Mods Integration)

**Milestone**: M1 (Requirements R3, R4)
**Working Directory**: `F:\Jogos\Mine\.agents\teamwork\worker_r2_m1`
**Authoritative Request**: `F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md`
**Scope & Architecture**: `F:\Jogos\Mine\.agents\teamwork\orchestrator_r2\SCOPE.md`
**Survey Reports to Consult**:
- `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_3\handoff.md` (Sections 1.2, 4.1, 4.2)
- `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_2\handoff.md` (Sections 1.2, 4.3)

## MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

## Write Ownership (Exclusive)
- `F:\Jogos\Mine\Instancia_Cliente\options.txt`
- `F:\Jogos\Mine\Instancia_Cliente\mods\` (Farmer's Respite, Comforts)
- `F:\Jogos\Mine\Servidor_Dedicado\mods\` (Farmer's Respite, Comforts, ForgeConfigAPIPort)
- `F:\Jogos\Mine\Instancia_Cliente\kubejs\assets\farmersrespite\lang\`
- `F:\Jogos\Mine\Instancia_Cliente\kubejs\assets\comforts\lang\`
- `F:\Jogos\Mine\Instancia_Cliente\resourcepacks\Russian_Bilingual_Educational_Pack\assets\`
- `F:\Jogos\Mine\Instancia_Cliente\kubejs\client_scripts\russian_tooltips.js`

## Concrete Tasks
1. **Unbind Iris Key 'R'**:
   - In `F:\Jogos\Mine\Instancia_Cliente\options.txt`, change line 132 from `key_iris.keybind.reload:key.keyboard.r` to `key_iris.keybind.reload:key.keyboard.unknown`.
   - Ensure the file is saved strictly with ASCII / UTF-8 Without BOM (No `\uFEFF`).
2. **Download & Provision Mods**:
   - Download Farmer's Respite (`Farmersrespite-Fabric-3.0.2.jar`, 733,624 bytes) from `https://www.curseforge.com/api/v1/mods/811003/files/5252387/download` (use TLS 1.2 and User-Agent 'Mozilla/5.0').
   - Download Comforts (`comforts-fabric-6.4.0+1.20.1.jar`, 693,624 bytes) from `https://cdn.modrinth.com/data/SaCpeal4/versions/pMr60Kkq/comforts-fabric-6.4.0%2B1.20.1.jar`.
   - Deploy both JARs symmetrically to BOTH `F:\Jogos\Mine\Instancia_Cliente\mods\` and `F:\Jogos\Mine\Servidor_Dedicado\mods\`.
   - Copy `ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar` from `Instancia_Cliente\mods\` to `Servidor_Dedicado\mods\`.
3. **Russian Vocabulary & Tooltips**:
   - Create `ru_ru.json` and `pt_br.json` for `farmersrespite` in `kubejs/assets/farmersrespite/lang/` and mirror in `resourcepacks/Russian_Bilingual_Educational_Pack/assets/farmersrespite/lang/`.
   - Verify `comforts` lang files in `kubejs/assets/comforts/lang/` and resourcepack.
   - Update `ruCurriculum` in `F:\Jogos\Mine\Instancia_Cliente\kubejs\client_scripts\russian_tooltips.js` with pedagogical notes for kettle, coffee, tea, sleeping bag, hammock, rope and nail, chair, table.
4. **Verification**:
   - Verify options.txt unbind and Zero-BOM.
   - Verify server dry-run (`fabric-server-launch.jar --help`) runs cleanly with both mods without crash.
   - Document verification commands and output in `handoff.md`.


## 2026-11-07T06:08:33Z
You are worker_r2_m1, working as an implementation Worker on Milestone M1 for the SayIt modpack (Minecraft 1.20.1 Fabric).
Working directory: F:\Jogos\Mine\.agents\teamwork\worker_r2_m1
Authoritative Requirements: F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md
Your Dispatch Task: F:\Jogos\Mine\.agents\teamwork\worker_r2_m1\DISPATCH.md
Scope Reference: F:\Jogos\Mine\.agents\teamwork\orchestrator_r2\SCOPE.md

DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Your tasks:
1. Unbind Iris reload key 'R' in Instancia_Cliente\options.txt (set key_iris.keybind.reload:key.keyboard.unknown, save as clean ASCII/No BOM).
2. Download Farmer's Respite and Comforts mods and deploy symmetrically to both Instancia_Cliente\mods\ and Servidor_Dedicado\mods\. Copy ForgeConfigAPIPort to Servidor_Dedicado\mods\.
3. Create ru_ru.json and pt_br.json for farmersrespite & comforts in kubejs/assets/ and resourcepacks. Update ruCurriculum in russian_tooltips.js.
4. Verify server headless dry-run and options.txt encoding.

Write a complete handoff report to F:\Jogos\Mine\.agents\teamwork\worker_r2_m1\handoff.md and report back via send_message.
