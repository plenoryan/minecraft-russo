# Survey Analysis: Requirements R4 (QoL & Dynamism Mods) & R5 (Build, Deployment & E2E Testing)

**Date**: 2026-11-04  
**Author**: teamwork_explorer_survey_3  
**Working Directory**: `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_3\`  
**Target Repository**: `F:\Jogos\Mine`  

---

## 1. Executive Summary

This investigation analyzed the SayIt Minecraft Russian Learning modpack environment at `F:\Jogos\Mine` to establish the architectural blueprint for:
1. **Requirement R4**: Integration of compatible, high-impact Quality of Life (QoL) and dynamism mods for Fabric 1.20.1 that do not pollute or overload the language learning vocabulary.
2. **Requirement R5**: Audit of the compilation toolchain, artifact packaging (`sayitenglish-fabric-1.20.1-1.1.0.jar`), dual deployment targets, and extension of the canonical E2E test suite (`tests\run_e2e_tests.ps1`) to comprehensively validate all five project requirements (R1 through R5).

### Key Discoveries:
1. **Build Toolchain Reality Check**: The project does not currently utilize an external Gradle/Fabric Loom build system. Instead, it maintains CFR-decompiled Java sources in `src/com/spicesfire/sayitenglish/`, compiling them directly with the bundled OpenJDK 17 runtime (`Java17\bin\javac.exe`) against Fabric remapped intermediary JARs (`client-intermediary.jar`) and runtime libraries, followed by bytecode patching via `Java17\bin\jar.exe uf` into `sayitenglish-fabric-1.20.1-1.1.0.jar`.
2. **Mod Segregation Compliance**: Existing mods follow strict client vs server segregation (documented in `AGENTS.md` Lesson L13). Client-only rendering/font mods (`Modern UI`, `Sodium`, `ImmediatelyFast`, `CustomSkinLoader`) must never be placed on the dedicated server, while content and universal logic mods (`FarmersDelight`, `naturalist`, `sayitenglish`, `lithium`, `ferritecore`) reside on both.
3. **Selected Candidate QoL Mods**: Identified 6 candidate mods on Modrinth API for Fabric 1.20.1 that strictly introduce zero new item/block identifiers, guaranteeing zero vocabulary interference with SayIt's speech recognition engine:
   - **Movement Dynamism**: `omnimovement` (Omnidirectional sprinting & auto step-up)
   - **Chest & Inventory Organization**: `inventory-sorting` (kyrptonaught - middle-click & container sorting, 0 dependencies)
   - **Quick Deposit**: `stack-to-nearby-chests` (xiaocihua1337 - Terraria-style quick stack, client-side only)
   - **Rendering Optimization**: `entityculling` (occlusion culling for dense animal/furniture entities)
   - **Networking Optimization**: `krypton` (Netty pipeline optimization for P2P/LAN sessions)
   - **Nutritional Feedback**: `appleskin` (food saturation & hunger HUD for Farmer's Delight)
4. **E2E Testing Harness**: `tests\run_e2e_tests.ps1` currently executes 45 tests across 4 tiers with 100% pass rate in strict mode (3.99 seconds). We mapped an expansion to 55 tests covering R1 (multiplayer coop & ranking), R2 (speech feedback HUD suppression & header formatting), R3 (Cyrillic alphabet non-identical progression), R4 (QoL mod integration & server boot), and R5 (build pipeline & dual deployment).

---

## 2. Existing Modpack Environment Audit

### 2.1. Client Mod Directory (`Instancia_Cliente\mods\`)
Contains 27 JAR files (totaling ~158 MB):

| Filename | Size (bytes) | Scope | Function |
|---|---|---|---|
| `AmbientSounds_FABRIC_v6.3.11_mc1.20.1.jar` | 53,436,017 | Client-only | Environmental audio immersion |
| `CreativeCore_FABRIC_v2.12.41_mc1.20.1.jar` | 1,207,852 | Client-only | Library for AmbientSounds |
| `CustomSkinLoader_Universal-15.1.jar` | 222,917 | Client-only | Offline skin system (LocalSkin + Ely.by API) |
| `FarmersDelight-1.20.1-2.1.1+refabricated.jar`| 6,723,291 | Universal | Farming & cooking mechanics, food items |
| `ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar` | 396,628 | Client-only | Forge configuration bridge for ModernUI |
| `Heracles-fabric-1.20.1-1.1.13.jar` | 1,411,990 | Client/Server | 4-Tier Russian Curriculum Quest Engine |
| `ImmediatelyFast-Fabric-1.5.5+1.20.4.jar` | 348,818 | Client-only | Immediate mode UI & Cyrillic font rendering optimization |
| `Jade-1.20-Fabric-11.13.3.jar` | 553,868 | Client-only | WAILA block/entity Russian crosshair tooltip overlay |
| `ModernUI-Fabric-1.20.1-3.12.0.1-universal.jar`| 24,987,755 | Client-only | TrueType/DirectWrite font rendering engine |
| `PresenceFootsteps-1.10.1+1.20.1.jar` | 5,664,952 | Client-only | Acoustic footstep sound dynamics |
| `architectury-9.2.14-fabric.jar` | 585,207 | Universal | Multi-platform abstraction library for KubeJS |
| `e4all-fabric-1.6.4.jar` | 913,004 | Client-only | Zero-port-forwarding P2P LAN tunneling with cracked support |
| `fabric-api-0.92.12+1.20.1.jar` | 2,137,232 | Universal | Core Fabric lifecycle and networking hooks |
| `ferritecore-6.0.1-fabric.jar` | 125,197 | Universal | RAM reduction via fastutil and model data optimization |
| `geckolib-fabric-1.20.1-4.8.4.jar` | 969,016 | Universal | Entity animation engine (Naturalist dep) |
| `handcrafted-fabric-1.20.1-3.0.6.jar` | 7,106,384 | Universal | Slavic domestic furniture and decorative blocks |
| `heraclesbutton-1.0.0.jar` | 138,429 | Client-only | Inventory HUD quest book button |
| `iris-1.7.6+mc1.20.1.jar` | 2,726,899 | Client-only | Modern shaders engine (Sodium accelerated) |
| `kubejs-fabric-2001.6.5-build.26.jar` | 1,608,326 | Client-only | Dynamic event scripting & pedagogical tooltips |
| `language-reload-1.7.5+1.20.1.jar` | 73,012 | Client-only | Shift+F3+J instant language reload & multilingual search |
| `lithium-fabric-mc1.20.1-0.11.4.jar` | 691,483 | Universal | Physics, chunk loading, and entity ticking optimization |
| `modernfix-fabric-5.25.2+mc1.20.1.jar` | 545,175 | Client-only | Memory leak patches and ultra-fast game launch |
| `naturalist-fabric-4.0.3-1.20.1.jar` | 4,310,075 | Universal | Forest wildlife entities (bears, deer, birds) |
| `resourcefullib-fabric-1.20.1-2.1.29.jar` | 431,646 | Universal | Core data and serialization library for Heracles |
| `rhino-fabric-2001.2.3-build.10.jar` | 1,743,587 | Client-only | Embedded ECMAScript engine for KubeJS |
| `sayitenglish-fabric-1.20.1-1.1.0.jar` | 30,752,654 | Universal | SayIt core speech recognition & challenge engine |
| `sodium-fabric-0.5.13+mc1.20.1.jar` | 971,552 | Client-only | Next-gen OpenGL rendering engine for AMD RDNA 2 |

### 2.2. Dedicated Server Mod Directory (`Servidor_Dedicado\mods\`)
Contains strictly 10 universal JAR files (totaling ~54 MB):

| Filename | Size (bytes) | Function on Dedicated Server |
|---|---|---|
| `FarmersDelight-1.20.1-2.1.1+refabricated.jar` | 6,723,291 | Server block registry, crops, and recipes |
| `fabric-api-0.92.12+1.20.1.jar` | 2,137,232 | Server lifecycle events & packet dispatch |
| `fabrictailor-2.1.2.jar` | 112,497 | Server-side offline skin synchronizer for connected clients |
| `ferritecore-6.0.1-fabric.jar` | 125,197 | Server JVM memory footprint minimization |
| `geckolib-fabric-1.20.1-4.8.4.jar` | 969,016 | Server entity synchronization |
| `handcrafted-fabric-1.20.1-3.0.6.jar` | 7,106,384 | Server block registry for furniture |
| `lithium-fabric-mc1.20.1-0.11.4.jar` | 691,483 | Server TPS stabilization, mob AI pathing optimization |
| `naturalist-fabric-4.0.3-1.20.1.jar` | 4,310,075 | Server entity spawning, ticking and behaviors |
| `resourcefullib-fabric-1.20.1-2.1.29.jar` | 431,646 | Server quest state serialization |
| `sayitenglish-fabric-1.20.1-1.1.0.jar` | 30,752,654 | Server authoritative challenge validation & inventory penalties |

### 2.3. NTFS Junction Topography
The launcher instances at `Launcher\instances\Minecraft_Russo\.minecraft\` and `Launcher\data\instances\Minecraft_Russo\.minecraft\` use NTFS Junctions pointing directly to `Instancia_Cliente\mods`, `config`, `resourcepacks`, `shaderpacks`, and `kubejs`. As documented in Lesson L11, file synchronization scripts must detect junction links to prevent redundant circular overwrites and `IOException` file locking.

---

## 3. Requirement R4: Evaluation of Candidate QoL & Dynamism Mods

### 3.1. Design Criteria for Mod Selection
Per Requirement R4 and Acceptance Criteria:
1. **Dynamic & Fluid Gameplay**: Eliminate clunky vanilla limitations (sprinting bottlenecks, tedious chest management, manual inventory dumping).
2. **Zero Vocabulary Pollution**: Must NOT register new items, blocks, or entities in Minecraft registries (`net.minecraft.registry.Registries.ITEM`). If a mod introduces dozens of new items, SayIt's item listener would trigger voice challenges for unvetted foreign words, breaking the curated 4-Tier Russian curriculum.
3. **Strict Fabric 1.20.1 Compatibility**: Must support Fabric Loader 0.16.14 and Minecraft 1.20.1 without mixin conflicts or crash reports.
4. **Architectural Segregation**: Clear distinction between client-only UI/rendering mods and universal/server mods to prevent headless server crashes (Lesson L13).

### 3.2. Evaluated Candidate Mods (Live Modrinth Metadata)

All metadata was directly queried and verified via Modrinth API v2:

| Mod Name | Slug | Version | Recommended Filename | Direct Download URL | Size (Bytes) | Env Scope | Dependencies |
|---|---|---|---|---|---|---|---|
| **Omnimovement** | `omnimovement` | `1.0` | `omnimovement-fabric-1.0+mc1.20.1.jar` | `https://cdn.modrinth.com/data/ZmKZ0vTP/versions/zDpV6cM8/omnimovement-fabric-1.0%2Bmc1.20.1.jar` | 29,222 | Client | None |
| **Inventory Sorting** | `inventory-sorting` | `1.9.0-1.20` | `InventorySorter-1.9.0-1.20.jar` | `https://cdn.modrinth.com/data/5ibSyLAz/versions/tr2ZGtz4/InventorySorter-1.9.0-1.20.jar` | 222,894 | Client + Server | None |
| **Stack to Nearby Chests** | `stack-to-nearby-chests` | `0.5.10` | `stack-to-nearby-chests-mc1.20.1-0.5.10.jar` | `https://cdn.modrinth.com/data/HtGckJVc/versions/wMJlsfCI/stack-to-nearby-chests-mc1.20.1-0.5.10.jar` | 559,703 | Client | Fabric API |
| **Entity Culling** | `entityculling` | `1.11.3` | `entityculling-fabric-1.11.3-mc1.20.1.jar` | `https://cdn.modrinth.com/data/NNAgCjsB/versions/xSrd3qsM/entityculling-fabric-1.11.3-mc1.20.1.jar` | 1,516,696 | Client | Fabric API |
| **Krypton** | `krypton` | `0.2.3` | `krypton-0.2.3.jar` | `https://cdn.modrinth.com/data/fQEb0iXm/versions/jiDwS0W1/krypton-0.2.3.jar` | 185,895 | Client + Server | None |
| **AppleSkin** | `appleskin` | `2.5.2+mc1.20.1` | `appleskin-fabric-mc1.20.1-2.5.2.jar` | `https://cdn.modrinth.com/data/EsAfCjCV/versions/N5XeV21r/appleskin-fabric-mc1.20.1-2.5.2.jar` | 1,097,240 | Client | Fabric API |

### 3.3. Technical Evaluation by Category

#### A. Movement Dynamism & Faster Sprinting
- **Selected**: `omnimovement` (`omnimovement-fabric-1.0+mc1.20.1.jar`).
- **Rationale**: Vanilla Minecraft restricts sprinting strictly to forward movement. `Omnimovement` enables 360-degree omnidirectional sprinting (sideways strafing, backward retreat) and introduces **Auto Step-Up** (scaling 1-block ledges smoothly without jumping). This makes wilderness exploration, mountain traversing, and survival maneuvering dramatically faster and more fluid without altering vanilla jump/fall physics.
- **Vocabulary Impact**: Zero items, blocks, or GUI text strings added.

#### B. Inventory & Chest Sorting
- **Selected**: `inventory-sorting` by kyrptonaught (`InventorySorter-1.9.0-1.20.jar`).
- **Rationale**: Evaluated alternatives: `Inventory Profiles Next` (rejected: requires `libIPN` and `Fabric Language Kotlin`, causing heavy runtime bloat and potential loader conflicts); `Client Sort` (rejected: requires `Cloth Config API`). Kyrptonaught's `Inventory Sorting` requires zero external dependencies, adds middle-click slot sorting, works seamlessly across player inventories, double chests, and barrels.
- **Vocabulary Impact**: Zero items or blocks added.

#### C. Quick Deposit to Nearby Chests
- **Selected**: `stack-to-nearby-chests` by xiaocihua1337 (`stack-to-nearby-chests-mc1.20.1-0.5.10.jar`).
- **Rationale**: Implements the beloved *Terraria* quick-stack mechanic. Pressing a single hotkey scans all openable containers within reach (chests, barrels, shulker boxes) and dumps matching items from the inventory into containers that already hold at least one of those items. Includes item favoriting (`Left Alt + Right Click`) to protect weapons and tools from being dumped.
- **Vocabulary Impact**: Zero items or blocks added. Strictly client-side.

#### D. Performance & Rendering Optimization
- **Selected**: `entityculling` (`entityculling-fabric-1.11.3-mc1.20.1.jar`).
- **Rationale**: The modpack includes `Naturalist` (dozens of birds, bears, deer) and `Handcrafted` (detailed furniture). Vanilla Minecraft renders entity models even when hidden behind solid stone walls or underground. `Entity Culling` uses raycasting/bounding-box occlusion to skip rendering hidden entities, recovering 15-30% FPS in developed bases.
- **Vocabulary Impact**: Pure client-side OpenGL optimization, zero vocabulary impact.

#### E. Multiplayer Networking Optimization
- **Selected**: `krypton` (`krypton-0.2.3.jar`).
- **Rationale**: Replaces the vanilla Netty networking pipeline with optimized Netty direct buffers and packet consolidation. In multiplayer setups using `e4all` P2P LAN tunnels or dedicated servers, Krypton significantly reduces tick desync, packet lag, and CPU overhead.
- **Vocabulary Impact**: Zero vocabulary impact. Installed on both client and dedicated server.

#### F. Nutritional Feedback (Farmer's Delight Integration)
- **Selected**: `appleskin` (`appleskin-fabric-mc1.20.1-2.5.2.jar`).
- **Rationale**: `Farmer's Delight` adds varied meals with complex saturation values. AppleSkin renders saturation bars and food restoration previews directly over the hunger bar when hovering over food.
- **Vocabulary Impact**: Zero new items. Merely decorates vanilla food tooltips.

---

## 4. Requirement R5: Build Pipeline, Artifacts & Deployment

### 4.1. Build System Reality Audit
The dispatch mission requested an inspection of `build.gradle`, `settings.gradle`, and Fabric Loom configuration. Investigation of the entire repository reveals:
- **No Gradle files exist**: There is no `build.gradle`, `settings.gradle`, or `.gradle/` directory in the repository.
- **Actual Build Toolchain**: The project employs an isolated CFR decompilation and bytecode injection pipeline:
  1. Mod sources are located in `src/com/spicesfire/sayitenglish/` (7 Java files: `Alphabet.java`, `ChallengeManager.java`, `Progress.java`, `client/ChallengeHud.java`, `client/ChallengeSession.java`, `client/DictionaryScreen.java`, `client/SayItJadePlugin.java`).
  2. Classes compile cleanly via `Java17\bin\javac.exe` against:
     - `Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar` (client Minecraft intermediary classes like `class_310`, `class_327`, `class_332`)
     - `Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar` (base mod classes)
     - `Instancia_Cliente\mods\Jade-1.20-Fabric-11.13.3.jar` (Jade API)
     - `Instancia_Cliente\mods\fabric-api-0.92.12+1.20.1.jar`
     - Runtime libraries in `Launcher\data\libraries\` (Gson, Brigadier, DataFixerUpper, SLF4J)
  3. Classes are packaged into the base JAR using `Java17\bin\jar.exe uf`.

### 4.2. Verification of Compilation Command
We experimentally verified clean 100% compilation of all 7 Java sources using:
```powershell
$javac = "F:\Jogos\Mine\Java17\bin\javac.exe"
$libJars = Get-ChildItem -Path "F:\Jogos\Mine\Launcher\data\libraries", "F:\Jogos\Mine\Servidor_Dedicado\libraries" -Filter "*.jar" -Recurse | Select-Object -ExpandProperty FullName
$cpList = @(
    "F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar",
    "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar",
    "F:\Jogos\Mine\Instancia_Cliente\mods\Jade-1.20-Fabric-11.13.3.jar",
    "F:\Jogos\Mine\Instancia_Cliente\mods\fabric-api-0.92.12+1.20.1.jar"
) + $libJars
$cp = ($cpList | Select-Object -Unique) -join ";"
$srcFiles = Get-ChildItem -Path "F:\Jogos\Mine\src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

& $javac -encoding UTF-8 -cp $cp -d "$env:TEMP\sayit_build" $srcFiles
```
**Result**: 0 errors, 11 class files generated (including nested classes like `ChallengeHud$SpeechFeedback.class`, `ChallengeSession$Banner.class`, `ChallengeManager$State.class`).

### 4.3. Deployment Targets & Synchronization Architecture
When the updated JAR is packaged, it must be deployed atomically to:
1. `F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
2. `F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
3. Non-junction launcher folders (if any):
   - `Launcher\instances\Minecraft_Russo\.minecraft\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
   - `Launcher\data\instances\Minecraft_Russo\.minecraft\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`

### 4.4. Proposed Automated Build & Deploy Script (`build_and_deploy.ps1`)
To replace manual ad-hoc compilation, a standardized script should be added:
- **Location**: `F:\Jogos\Mine\build_and_deploy.ps1`
- **Functionality**:
  1. Compiles `src/**/*.java` with strict UTF-8 encoding.
  2. Updates `sayitenglish-fabric-1.20.1-1.1.0.jar` with new bytecode and language files.
  3. Deploys the updated JAR to client, server, and launcher instances.
  4. Runs byte-level validation across all target files.

---

## 5. Requirement R5: E2E Test Suite Extension Architecture

### 5.1. Existing Test Harness Audit (`tests\run_e2e_tests.ps1`)
- **Language**: Pure 7-bit ASCII PowerShell script compatible with PowerShell 5.1 and 7+.
- **Execution**: `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`
- **Current Coverage**: 45 registered tests across 4 tiers:
  - **Tier 1 (F1-F23)**: Engine, loaders, mods, acoustic models, dictionaries, font engines, quests, server configs, STT isolation, test harness self-test, zero-DRM audits.
  - **Tier 2 (T2.1-T2.10)**: Diacritics, yo/ye equivalence, Levenshtein tolerance, multi-word tokens, malformed audio transcripts, offline UUID RFC 4122 v3 conformity, chat signature prevention, whitespace paths.
  - **Tier 3 (T3.1-T3.6)**: Offline skins synergy, quest-to-dictionary alignment, adaptive timer progression, server mod segregation, UDP port proxying, concurrent player isolation.
  - **Tier 4 (T4.1-T4.6)**: Scenarios for first launch, item voice challenge, timeout item drop, multiplayer e4all session, dedicated server headless boot, keyboard fallback.
- **Current Performance**: 45/45 PASSED in 3,991 ms.

### 5.2. Test Suite Extension Plan (10 New Canonical Tests)

To achieve 100% coverage of all acceptance criteria for R1 to R5, `tests\run_e2e_tests.ps1` should be extended from 45 to 55 tests:

```
Existing Tests (45) + New Tests (10) = 55 Canonical E2E Tests
```

#### New Test Case Specifications:

| Test ID | Tier | Milestone / Req | Test Name | Assertion & Oracle Specification |
|---|---|---|---|---|
| **`T1_F24`** | Tier 1 | M5 / R1 | Cooperative Phrase Invocation & Protocol Schema | Asserts that `ChallengeManager.java` / `Packets.java` defines cooperative phrase types: item requests (`дай мне`), spatial pointing (`иди сюда`, `смотри туда`), follow prompts (`следуй за мной`), and dynamic challenges (`пол - это лава`). Verifies payload schema includes sender UUID, target phrase in Russian, and Portuguese translation hint. |
| **`T1_F25`** | Tier 1 | M5 / R1 | Cooperative Ranking & Scoring System | Asserts that `Progress.java` tracks cooperative challenge completions, calculates score points, and stores persistent ranking data. Verifies ranking calculation oracle matches expected leaderboard order. |
| **`T1_F26`** | Tier 1 | M5 / R2 | Single-Word Item Challenge Speech HUD Suppression | Inspects `ChallengeHud.java` to verify that when `current.type()` is single word/item, `SpeechFeedback` is suppressed (`null`). Prevents HUD clutter during basic mining/gathering. |
| **`T1_F27`** | Tier 1 | M5 / R2 | Sentence Speech Feedback HUD Header & Dimension Bounds | Asserts that `ChallengeHud.java` renders speech feedback exclusively on multi-word sentences, uses strict header `"disse: "` (or `\u0064\u0069\u0073\u0073\u0065\u003a\u0020`), and maintains reduced dimensions (width <= 180, height <= 45). |
| **`T1_F28`** | Tier 1 | M5 / R3 | Non-Identical Cyrillic Letters Slow Progression Calibration | Asserts that non-identical letters (`Д`, `Б`, `Г`, `Ж`, `И`, `Л`, `П`, `Ф`, `Ц`, `Ч`, `Ш`, `Щ`, `Ю`, `Я`, `В`, `Н`, `Р`, `С`, `У`, `Х`) are NOT permanently unlocked by initial basic item collection (e.g. collecting 1 oak log). Only identical letters (`А`, `О`, `Е`, `М`, `Т`, `К`) allow early substitution. |
| **`T1_F29`** | Tier 1 | M5 / R4 | Client QoL Modpack Manifest & Byte Integrity | Verifies that `Instancia_Cliente\mods\` contains all 6 selected candidate QoL mods (`omnimovement`, `InventorySorter`, `stack-to-nearby-chests`, `entityculling`, `krypton`, `appleskin`) and that each file exceeds minimum byte size thresholds. |
| **`T1_F30`** | Tier 1 | M5 / R4 | Server QoL Modpack Segregation & Safety | Verifies that `Servidor_Dedicado\mods\` includes universal mods (`InventorySorter`, `krypton`) and strictly excludes client-only mods (`omnimovement`, `stack-to-nearby-chests`, `entityculling`, `appleskin`). |
| **`T1_F31`** | Tier 1 | M5 / R5 | Java Source Build Pipeline Compilation Integrity | Executes headless compilation of `src/**/*.java` using `Java17\bin\javac.exe` against the remapped classpath to verify 100% clean compilation with exit code 0. |
| **`T1_F32`** | Tier 1 | M5 / R5 | Dual-Target Mod Deployment Verification | Asserts that `sayitenglish-fabric-1.20.1-1.1.0.jar` exists in both `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`, and verifies that both archives contain the updated compiled `.class` files (`ChallengeManager.class`, `Alphabet.class`, `ChallengeHud.class`). |
| **`T4_07`** | Tier 4 | M5 / R4 | Scenario: Headless Dedicated Server Boot with New QoL Mods | Executes a real headless dry-run (`java.exe -jar fabric-server-launch.jar --help`) on the dedicated server with the newly integrated mods to prove zero `ClassNotFoundException`, zero mixin collision, and zero formatted loader crashes. |

---

## 6. Implementation Roadmap for Subsequent Phases

```
Phase 1: Synthesis & Master Plan (Orchestrator)
  ↓
Phase 2: Workstreams Dispatch
  ├── Worker 1: R1 Multiplayer Mechanics & Cooperative Phrases
  ├── Worker 2: R2 Speech HUD Calibration ("disse: ") & R3 Alphabet Calibration
  ├── Worker 3: R4 QoL Mods Download & Provisioning Script (`download_qol_mods.ps1`)
  └── Worker 4: R5 Build Pipeline Script (`build_and_deploy.ps1`) & E2E Extension (`run_e2e_tests.ps1`)
  ↓
Phase 3: Verification & Milestone Gates (Reviewer, Challenger, Forensic Auditor)
  ↓
Phase 4: Final Strict E2E Execution (55/55 PASSED with Exit Code 0)
```
