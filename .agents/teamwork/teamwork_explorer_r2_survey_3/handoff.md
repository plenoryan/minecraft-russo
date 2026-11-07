# Handoff Report — Explorer 3: Survey of Mods Integration, Russian Vocabulary & E2E Test Suite

**Author**: survey_exp_3  
**Working Directory**: `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_3`  
**Milestone Scope**: R4 Mods Integration (Farmer's Respite, Comforts, Handcrafted, Farmer's Delight), Russian Vocabulary Adaptation, and E2E Test Suite Extensions.  
**Date**: 2026-11-07  

---

## 1. Observation

### 1.1 Current Mod Inventory & Segregation State
- **Client Mods Directory (`F:\Jogos\Mine\Instancia_Cliente\mods\`):**
  - Contains **32 JAR files** totaling ~140 MB.
  - Core content mods present:
    * `FarmersDelight-1.20.1-2.1.1+refabricated.jar` (6,723,291 bytes)
    * `handcrafted-fabric-1.20.1-3.0.6.jar` (7,106,384 bytes)
    * `naturalist-fabric-4.0.3-1.20.1.jar` (4,310,075 bytes)
    * `geckolib-fabric-1.20.1-4.8.4.jar` (969,016 bytes)
    * `resourcefullib-fabric-1.20.1-2.1.29.jar` (431,646 bytes)
    * `ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar` (396,628 bytes)
    * `sayitenglish-fabric-1.20.1-1.1.0.jar` (30,782,292 bytes)
  - Client-only mods present:
    * `sodium-fabric-0.5.13+mc1.20.1.jar`, `iris-1.7.6+mc1.20.1.jar`, `ModernUI-Fabric-1.20.1-3.12.0.1-universal.jar`, `ImmediatelyFast-Fabric-1.5.5+1.20.4.jar`, `CustomSkinLoader_Universal-15.1.jar`, `e4all-fabric-1.6.4.jar`, `AmbientSounds_FABRIC_v6.3.11_mc1.20.1.jar`, `PresenceFootsteps-1.10.1+1.20.1.jar`, `omnimovement-fabric-1.0+mc1.20.1.jar`, `stack-to-nearby-chests-mc1.20.1-0.5.10.jar`, `entityculling-fabric-1.11.3-mc1.20.1.jar`, `appleskin-fabric-mc1.20.1-2.5.2.jar`.
  - Missing mods: `Farmer's Respite` and `Comforts` are currently absent.
- **Dedicated Server Mods Directory (`F:\Jogos\Mine\Servidor_Dedicado\mods\`):**
  - Contains **11 JAR files** strictly respecting client/server segregation (AGENTS.md L13):
    * `FarmersDelight-1.20.1-2.1.1+refabricated.jar`
    * `handcrafted-fabric-1.20.1-3.0.6.jar`
    * `naturalist-fabric-4.0.3-1.20.1.jar`
    * `geckolib-fabric-1.20.1-4.8.4.jar`
    * `resourcefullib-fabric-1.20.1-2.1.29.jar`
    * `InventorySorter-1.9.0-1.20.jar`
    * `fabrictailor-2.1.2.jar`
    * `fabric-api-0.92.12+1.20.1.jar`
    * `lithium-fabric-mc1.20.1-0.11.4.jar`
    * `ferritecore-6.0.1-fabric.jar`
    * `sayitenglish-fabric-1.20.1-1.1.0.jar`
  - Zero client-only rendering/sound mods present in the dedicated server.

### 1.2 Verification of Candidate Mods for Fabric 1.20.1
1. **Farmer's Respite (Fabric):**
   - Mod Identification: `Farmersrespite-Fabric-3.0.2 [Refabricated]` (id: `farmersrespite`, version: `3.0.2`).
   - Source: CurseForge Project ID `811003`, File ID `5252387`.
   - Download Endpoint: `https://www.curseforge.com/api/v1/mods/811003/files/5252387/download`
   - Verified size: 733,624 bytes.
   - Inspected `fabric.mod.json`:
     ```json
     {
       "schemaVersion": 1,
       "id": "farmersrespite",
       "version": "3.0.2",
       "environment": "*",
       "depends": {
         "fabricloader": ">=0.14.9",
         "fabric": "*",
         "minecraft": ">=1.20.1",
         "java": ">=17",
         "forgeconfigapiport": "*"
       },
       "jars": [{ "file": "META-INF/jars/forge-config-api-port-fabric-547434-4583000.jar" }]
     }
     ```
   - Observations:
     * `environment: "*"` means it is a universal mod with blocks, items, recipes, tile entities, and network packets.
     * Bundles `forge-config-api-port` internally via Jar-in-Jar (`META-INF/jars/`).
     * Depends on `farmersdelight`, which is already installed.
     * Loader `>=0.14.9` is 100% satisfied by our Fabric Loader `0.16.14`.
     * Does NOT include native `ru_ru.json` or `pt_br.json` (only `en_us.json` and `uk_ua.json`), making external language mapping mandatory.

2. **Comforts (Fabric):**
   - Mod Identification: `comforts-fabric-6.4.0+1.20.1.jar` (id: `comforts`, version: `6.4.0+1.20.1`).
   - Source: Modrinth Project `comforts`, Version ID `pMr60Kkq`.
   - Direct Download URL: `https://cdn.modrinth.com/data/SaCpeal4/versions/pMr60Kkq/comforts-fabric-6.4.0%2B1.20.1.jar`
   - Verified size: 693,624 bytes.
   - SHA-512: `c1796378b47aa03544b772d2b631d7387e4365ba0c0a3468dc5e78348d26e0406bd015b530e0acab679e090310aafd201a106e519d3b9e32b26e0db9928b04be`.
   - Inspected `fabric.mod.json`:
     ```json
     {
       "schemaVersion": 1,
       "id": "comforts",
       "version": "6.4.0+1.20.1",
       "environment": "*",
       "depends": {
         "fabricloader": ">=0.14",
         "fabric-api": "*",
         "minecraft": "1.20.x",
         "java": ">=17",
         "cardinal-components-base": "*",
         "cardinal-components-entity": "*"
       },
       "jars": [
         { "file": "META-INF/jars/cardinal-components-base-5.2.1.jar" },
         { "file": "META-INF/jars/cardinal-components-entity-5.2.1.jar" },
         { "file": "META-INF/jars/spectrelib-fabric-0.13.15+1.20.1.jar" }
       ]
     }
     ```
   - Observations:
     * `environment: "*"` means it is universal (Client + Server required).
     * Bundles Cardinal Components (`cardinal-components-base-5.2.1.jar`, `cardinal-components-entity-5.2.1.jar`) and `spectrelib` via Jar-in-Jar. Zero missing dependencies.
     * Contains built-in `ru_ru.json` ("Чёрный гамак", "Белый спальный мешок", "Верёвка и гвоздь") and `pt_br.json` ("Pano de Rede Preto", "Saco de Dormir Branco", "Corda e Prego").

### 1.3 Iris Shader Reload Keybinding 'R'
- Inspecting `F:\Jogos\Mine\Instancia_Cliente\options.txt`:
  - Line 132: `key_iris.keybind.reload:key.keyboard.r`
  - Line 135: `key_iris.keybind.wireframe:key.keyboard.unknown`
- Unbound key representation in Minecraft/Fabric `options.txt` is strictly `key.keyboard.unknown`.

### 1.4 Russian Vocabulary & Tooltip Architecture in SayIt
- **Item Resolution (`Words.java:92-100`):**
  - `Words.word(itemId)` resolves `item` from `class_7923.field_41178` (`Registries.ITEM`).
  - Calls `Words.inTarget(itemStack.method_7964())` which temporarily binds `target` language table (`ru_ru.json`) and extracts the raw Cyrillic translation.
  - Native text `prompt.nativeText()` is evaluated in client language (`pt_br.json`).
- **Phonetics and Letter Progression:**
  - `Romanizer.of(ru, "ru")`: transliterates Cyrillic to Latin phonetic notation.
  - `Alphabet.java`: provides `toSmartHybrid(ru, unlocked, formatCodes)` for gradual letter transformation (gold for Cyrillic, white for Portuguese/Latin), plus letter difficulty ranking and heat thresholds.
  - `Scorer.java`: supports compound nouns (adjective + noun) and subtoken matching (e.g. "Акациевый стул" passes if "стул" is pronounced with $\ge 75\%$ accuracy).
- **Tooltip Script (`F:\Jogos\Mine\Instancia_Cliente\kubejs\client_scripts\russian_tooltips.js`):**
  - Lines 121-474: `ruCurriculum` dictionary containing curated items with fields `ru`, `phon`, `pt`, `gen`, `tier`, `note`.
  - Lines 546-684: `event.addAdvancedToAll` fallback dynamically interrogates `Words.word(itemId)` and `Romanizer.of(ru, 'ru')`, rendering dynamic bilingual progression tooltips for any item with valid Cyrillic translations (`[\u0400-\u04FF]`).
  - Namespace assets:
    * `F:\Jogos\Mine\Instancia_Cliente\kubejs\assets\<namespace>\lang\ru_ru.json`
    * `F:\Jogos\Mine\Instancia_Cliente\kubejs\assets\<namespace>\lang\pt_br.json`
    * Mirrored in `F:\Jogos\Mine\Instancia_Cliente\resourcepacks\Russian_Bilingual_Educational_Pack\assets\<namespace>\lang\`.

### 1.5 Baseline E2E Test Suite Status
- Command executed:
  `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`
- Output:
  ```
  Total Test Cases Executed : 55
  Tests Passed              : 55
  Pending Milestones        : 0
  Assertion / Mod Failures  : 0
  Total Duration            : 3871 ms
  [SUCCESS] Test harness verified successfully. 0 test defects detected!
  ```
- Line 524 of `tests\run_e2e_tests.ps1`:
  `if ($TestRegistry.Count -lt 55) { throw "Test registry contains insufficient test coverage: ... (minimum 55 required)" }`

---

## 2. Logic Chain

### 2.1 Mod Segregation and Installation Reasoning
1. *Observation:* Both `farmersrespite` and `comforts` declare `environment: "*"` in their respective `fabric.mod.json`.
2. *Observation:* In Fabric, any mod declaring `environment: "*"` registers gameplay items, blocks, and world interactions. Omitting them from either client or dedicated server causes packet mismatches, missing block state IDs, or instant disconnects (`Internal Exception: io.netty.handler.codec.DecoderException`).
3. *Observation:* Both mods bundle their sub-dependencies internally (Jar-in-Jar): `comforts` embeds `spectrelib` and `cardinal-components`; `farmersrespite` embeds `forge-config-api-port`.
4. *Conclusion:* Both JARs must be deployed symmetrically to BOTH `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`. `ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar` should also be mirrored to `Servidor_Dedicado\mods\` to avoid JiJ version divergence.

### 2.2 Russian Adaptation Strategy for Everyday / Furniture Mods
1. *Observation:* `Words.word(itemId)` queries `ru_ru.json` via the vanilla component system. If `assets/<namespace>/lang/ru_ru.json` lacks an entry, `Words.word()` falls back to English or the raw translation key (`item.farmersrespite.kettle`), breaking Vosk recognition and dropping the item.
2. *Observation:* `russian_tooltips.js` already features `event.addAdvancedToAll` (lines 546-684), which dynamically reads from `Words.word(itemId)` and `Romanizer.of(ru, 'ru')`.
3. *Observation:* Primary curriculum items (kettle, coffee, green tea, sleeping bag, hammock, chair, table, skillet) benefit from handcrafted pedagogical notes in `ruCurriculum` (lines 121-474) detailing phonetic stress, grammatical gender, declensions, and cultural context.
4. *Conclusion:* Adaptation requires two synchronized tiers:
   - Tier A: Create/update `assets/farmersrespite/lang/` and `assets/comforts/lang/` (`ru_ru.json` and `pt_br.json`) in `kubejs/assets/` and `resourcepacks/Russian_Bilingual_Educational_Pack/assets/`.
   - Tier B: Register the cardinal everyday items in `ruCurriculum` in `russian_tooltips.js` for enhanced linguistic breakdown, while allowing `addAdvancedToAll` to cover all color/wood permutations seamlessly.

### 2.3 Iris 'R' Keybind Decoupling
1. *Observation:* `options.txt` stores key assignments as `<name>:<key>`. Unbound keys use `key.keyboard.unknown`.
2. *Observation:* Iris registers `key_iris.keybind.reload:key.keyboard.r`.
3. *Conclusion:* Replacing `key_iris.keybind.reload:key.keyboard.r` with `key_iris.keybind.reload:key.keyboard.unknown` in `Instancia_Cliente\options.txt` disables the reload shortcut. Furthermore, a startup check in SayIt or setup script guarantees idempotence against options file regeneration.

### 2.4 E2E Test Suite Extension Architecture
1. *Observation:* The current test runner contains 55 tests and self-checks coverage (`$TestRegistry.Count -lt 55`).
2. *Observation:* New requirements R1, R2, R3, R4 require strict assertions for:
   - Radial selection UI ('V' keybinding, screen layout, 6 actions)
   - Strict speech challenge gating (cue dispatch strictly conditioned on $\ge 85\%$ score)
   - Receiver visual notification (initiator head skin + request icon with '?' badge)
   - Iris shader reload 'R' unbinding (`key.keyboard.unknown`)
   - Farmer's Respite + Comforts mod presence, server boot, and bilingual dictionary entries.
3. *Conclusion:* Adding 7 new test cases (6 Tier 1 tests + 1 Tier 4 scenario) brings the registry to 62 tests. Updating the self-check threshold to `62` guarantees 100% strict automated enforcement.

---

## 3. Caveats

1. **CurseForge Direct Downloads:** CurseForge CDN imposes HTTP 403 on unauthenticated raw scraper agents if user-agent headers are missing or if using old `edge.forgecdn.net` URLs. The official API v1 endpoint `https://www.curseforge.com/api/v1/mods/811003/files/5252387/download` with a standard User-Agent header successfully returns the full 733,624 byte JAR.
2. **Compound Noun Accents:** Items like "Зелёный чай" or "Спальный мешок" are multi-word compounds. `Scorer.java` already handles subtoken matching for base nouns; however, the dictionary files must provide canonical clean Cyrillic without diacritics in `ru_ru.json` to prevent STT mismatch (AGENTS.md L23/L45).
3. **NTFS Junctions:** `Launcher\instances\Minecraft_Russo\.minecraft\mods` is an NTFS junction to `Instancia_Cliente\mods`. Deploying mods to `Instancia_Cliente\mods` instantly updates the launcher without redundant copying.

---

## 4. Conclusion & Concrete Recommendations

### 4.1 Mod Deployment Plan
| Mod | Version | Jar Name | Scope | Direct URL | Size |
|---|---|---|---|---|---|
| **Farmer's Respite** | 3.0.2 [Refabricated] | `Farmersrespite-Fabric-3.0.2.jar` | Client + Server | `https://www.curseforge.com/api/v1/mods/811003/files/5252387/download` | 733,624 B |
| **Comforts** | 6.4.0+1.20.1 | `comforts-fabric-6.4.0+1.20.1.jar` | Client + Server | `https://cdn.modrinth.com/data/SaCpeal4/versions/pMr60Kkq/comforts-fabric-6.4.0%2B1.20.1.jar` | 693,624 B |
| **ForgeConfigAPIPort** | 8.0.3 | `ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar` | Copy to Server | Local (`Instancia_Cliente\mods`) | 396,628 B |

### 4.2 Language & Curriculum Schema
Create `assets/farmersrespite/lang/` and update `assets/comforts/lang/`, `assets/handcrafted/lang/`, and `assets/farmersdelight/lang/` in `kubejs/assets/` and `resourcepacks/Russian_Bilingual_Educational_Pack/assets/`:

```json
/* kubejs/assets/farmersrespite/lang/ru_ru.json */
{
  "block.farmersrespite.kettle": "Чайник",
  "item.farmersrespite.coffee": "Кофе",
  "item.farmersrespite.green_tea": "Зелёный чай",
  "item.farmersrespite.yellow_tea": "Жёлтый чай",
  "item.farmersrespite.black_tea": "Чёрный чай",
  "item.farmersrespite.rose_hip_tea": "Чай из шиповника",
  "item.farmersrespite.dandelion_tea": "Чай из одуванчиков",
  "item.farmersrespite.coffee_berries": "Кофейные ягоды",
  "block.farmersrespite.coffee_cake": "Кофейный пирог",
  "item.farmersrespite.coffee_cake_slice": "Кусок кофейного пирога",
  "item.farmersrespite.green_tea_cookie": "Печенье с зелёным чаем",
  "item.farmersrespite.tea_seeds": "Семена чая",
  "item.farmersrespite.green_tea_leaves": "Листья зелёного чая",
  "item.farmersrespite.yellow_tea_leaves": "Листья жёлтого чая",
  "item.farmersrespite.black_tea_leaves": "Листья чёрного чая",
  "item.farmersrespite.rose_hips": "Шиповник"
}
```

```json
/* kubejs/assets/farmersrespite/lang/pt_br.json */
{
  "block.farmersrespite.kettle": "Chaleira",
  "item.farmersrespite.coffee": "Café",
  "item.farmersrespite.green_tea": "Chá Verde",
  "item.farmersrespite.yellow_tea": "Chá Amarelo",
  "item.farmersrespite.black_tea": "Chá Preto",
  "item.farmersrespite.rose_hip_tea": "Chá de Rosa Mosqueta",
  "item.farmersrespite.dandelion_tea": "Chá de Dente-de-Leão",
  "item.farmersrespite.coffee_berries": "Grãos de Café",
  "block.farmersrespite.coffee_cake": "Bolo de Café",
  "item.farmersrespite.coffee_cake_slice": "Fatia de Bolo de Café",
  "item.farmersrespite.green_tea_cookie": "Biscoito de Chá Verde",
  "item.farmersrespite.tea_seeds": "Sementes de Chá",
  "item.farmersrespite.green_tea_leaves": "Folhas de Chá Verde",
  "item.farmersrespite.yellow_tea_leaves": "Folhas de Chá Amarelo",
  "item.farmersrespite.black_tea_leaves": "Folhas de Chá Preto",
  "item.farmersrespite.rose_hips": "Rosa Mosqueta"
}
```

Add curriculum definitions to `ruCurriculum` in `russian_tooltips.js`:
- `farmersrespite:kettle`: `{ ru: 'Чáйник', phon: '[CHAY-neek]', pt: 'Chaleira', gen: 'Masculino (м.р.)', tier: 2, note: 'Utensílio para ferver água e preparar infusões. Raiz Чай (chá).' }`
- `farmersrespite:coffee`: `{ ru: 'Кóфе', phon: '[KOH-fye]', pt: 'Café', gen: 'Masculino (м.р.)', tier: 2, note: 'Substantivo indeclinável de gênero masculino. Bebida energizante.' }`
- `farmersrespite:green_tea`: `{ ru: 'Зелëный чай', phon: '[zye-LYO-niy CHAY]', pt: 'Chá Verde', gen: 'Masculino (м.р.)', tier: 2, note: 'Adjetivo + substantivo. A vogal \'ë\' é sempre tônica.' }`
- `comforts:sleeping_bag_white`: `{ ru: 'Спáльный мешóк', phon: '[SPAHL-niy mye-SHOK]', pt: 'Saco de Dormir', gen: 'Masculino (м.р.)', tier: 2, note: 'Composto: Спальный (para dormir) + Мешок (saco). Permite dormir sem alterar o ponto de renascimento.' }`
- `comforts:hammock_white`: `{ ru: 'Гамáк', phon: '[ga-MAHK]', pt: 'Rede', gen: 'Masculino (м.р.)', tier: 2, note: 'Rede de descanso suspensa. Permite avançar o dia até o anoitecer.' }`
- `comforts:rope_and_nail`: `{ ru: 'Верëвка и гвоздь', phon: '[vye-RYOV-ka ee GVOST\']', pt: 'Corda e Prego', gen: 'Feminino + Masculino', tier: 2, note: 'Suporte de fixação de rede. A palavra Гвоздь tem \'дь\' palatizado.' }`
- `handcrafted:chair` & `handcrafted:table`: Handcrafted furniture entries with phonetics and grammar notes.

### 4.3 Proposed E2E Test Extensions for `run_e2e_tests.ps1`
Propose 7 new tests extending the suite from 55 to 62 tests:

1. **`T1_F33` (Tier 1 / M1) — Radial Wheel UI Keybinding ('V') and Action Registry**
   - Asserts keybinding `key.sayitenglish.radial` mapped to key 86 (V).
   - Asserts radial wheel class `RadialWheelScreen.java` exists, compiles, and defines 6 cooperative action sectors with respective icons and target phrases.
2. **`T1_F34` (Tier 1 / M1) — Strict Voice Challenge Gating on Radial Selection**
   - Verifies that radial selection initiates a voice challenge session (`ChallengeSession.start`) rather than immediately emitting network cues.
   - Asserts strict conditional dispatch: speech score $\ge 85\%$ authorizes network cue packet dispatch; score $< 85\%$ or timeout emits ZERO packets to teammates.
3. **`T1_F35` (Tier 1 / M2) — Receiver Visual Notification with Initiator Head & Item '?' Icon**
   - Asserts HUD receiver renderer (`ChallengeHud.java` / notification overlay) reacts to `CoopCue` packets.
   - Verifies rendering of initiator skin head texture, item request icon, distinctive question mark **'?'** badge overlay, and bilingual text subtitle.
4. **`T1_F36` (Tier 1 / M2) — Iris Shader Reload Key 'R' Unbinding**
   - Inspects `Instancia_Cliente\options.txt` and verifies `key_iris.keybind.reload:key.keyboard.unknown`.
   - Verifies key 'R' is not bound to `key.iris.reloadShaders`.
5. **`T1_F37` (Tier 1 / M3) — Farmer's Respite and Comforts Mod Integration & Segregation**
   - Asserts `Farmersrespite-Fabric-3.0.2*.jar` and `comforts-fabric-6.4.0*.jar` exist and $\ge 500$ KB in both `Instancia_Cliente\mods` and `Servidor_Dedicado\mods`.
   - Asserts dedicated server headless dry-run (`fabric-server-launch.jar --help`) runs cleanly with both mods without mixin collisions or `ClassNotFoundException`.
6. **`T1_F38` (Tier 1 / M3) — Russian Vocabulary & Tooltips for Furniture and Daily Life**
   - Asserts `kubejs/assets/farmersrespite/lang/ru_ru.json` and `comforts` lang files are valid JSON with required Cyrillic keys (kettle, coffee, tea, sleeping bag, hammock).
   - Asserts `russian_tooltips.js` contains entries or valid dynamic fallback for Handcrafted, Farmer's Delight, Farmer's Respite, and Comforts.
   - Evaluates pronunciation scoring oracle for all newly integrated vocabulary, ensuring 100% recognition pass.
7. **`T4_08` (Tier 4 / M4) — End-to-End Cooperative Radial -> Voice Gating -> Visual Cue -> Mod Item Interaction Scenario**
   - Simulates multi-player flow: Player A opens radial with 'V' -> selects wood request -> speech gating tests wrong word (cue blocked) -> speech gating tests correct phrase (cue approved and dispatched) -> Player B displays head+'?' notification -> Player B interacts with Farmer's Respite kettle or Comforts hammock, displaying Cyrillic tooltips.
8. **Update `T1_F22` Self-Check Threshold:**
   - Update line 524 from `$TestRegistry.Count -lt 55` to `$TestRegistry.Count -lt 62` to enforce strict coverage.

---

## 5. Verification Method

To independently verify the facts and findings in this report:

1. **Verify Baseline Test Suite:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
   ```
   *Expected:* 55 tests passed, 0 failures, exit code 0.

2. **Verify Download URLs for New Mods:**
   ```powershell
   # Farmer's Respite (733,624 bytes)
   [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
   $wc = New-Object System.Net.WebClient
   $wc.Headers.Add('User-Agent', 'Mozilla/5.0')
   $bytes = $wc.DownloadData('https://www.curseforge.com/api/v1/mods/811003/files/5252387/download')
   Write-Host "Farmer's Respite downloaded bytes: $($bytes.Length)"

   # Comforts (693,624 bytes)
   $bytes2 = $wc.DownloadData('https://cdn.modrinth.com/data/SaCpeal4/versions/pMr60Kkq/comforts-fabric-6.4.0%2B1.20.1.jar')
   Write-Host "Comforts downloaded bytes: $($bytes2.Length)"
   ```
   *Expected:* Exact non-zero byte downloads matching verified sizes.

3. **Verify Iris Reload Key 'R' in `options.txt`:**
   ```powershell
   Select-String -Path "F:\Jogos\Mine\Instancia_Cliente\options.txt" -Pattern "key_iris.keybind.reload"
   ```
   *Expected:* Shows current binding (`key_iris.keybind.reload:key.keyboard.r`), confirming that modification to `key.keyboard.unknown` is required.

4. **Verify Mod Segregation & Server Compatibility:**
   ```powershell
   # Headless server check
   & "F:\Jogos\Mine\Java17\bin\java.exe" -Dfabric.dli.main=net.fabricmc.loader.impl.launch.server.FabricServerLauncher -jar "F:\Jogos\Mine\Servidor_Dedicado\fabric-server-launch.jar" --help
   ```
   *Expected:* Clean exit with code 0 and `--help` output.

5. **Invalidation Conditions:**
   - If Farmer's Respite or Comforts is placed only in client, multiplayer server will fail or reject block states.
   - If Iris keybind is unbound in `options.txt` with invalid string instead of `key.keyboard.unknown`, Minecraft may reset options to defaults.
   - If new test count is less than 62, `T1_F22` self-verification threshold will fail.
