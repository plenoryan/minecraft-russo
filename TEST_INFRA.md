# TEST_INFRA.md - E2E Testing Infrastructure Specification

## Minecraft Russian Language Learning Modpack ("Melty Voice" Edition)
- **Target Project Directory**: `F:\Jogos\Mine\`
- **Mod Loader & Runtime**: Fabric Loader 0.16.14 / Fabric API 0.92.12+1.20.1 on Minecraft 1.20.1 Java Edition
- **Test Writer & Track**: `test_writer_e2e` (Milestones 1-4 Verification Track)
- **Document Version**: 2.0.0 (Extended Canonical Suite: 55 Tests)
- **Document Date**: 2026-11-04
- **Testing Philosophy**: Opaque-box, requirement-driven, 4-tier verification with progressive milestone support, Category-Partition, BVA, Pairwise, and Workload testing.

---

## 1. Architectural Overview & Testing Philosophy

This document defines the canonical End-to-End (E2E) testing framework for the Minecraft Russian Language Learning Modpack project, governing all functional, pedagogical, architectural, and multiplayer requirements (R1 through R5).

### 1.1 Core Verification Philosophies

1. **Requirement-Driven & Opaque-Box**:
   Tests treat the target system, modpack, and launcher as an opaque box with well-defined inputs, outputs, and side-effects. Verification inspects real bytecode outputs, configuration files, network packets, live headless server execution, and deterministic mathematical oracles without depending on non-exported implementation trivia.

2. **Category-Partition Method (TSL)**:
   The functional domain is divided into disjoint functional categories:
   - *Challenge Types*: Single-word item (`word`), review (`practice`), multi-word environmental (`sentence`), cooperative invocation (`coop.phrase`), dynamic mini-challenge (`coop.dynamic`).
   - *Phonetic Glyphs*: Transparent identical letters (`А, О, Е, Т, К, М`), false friends (`В, Р, С, Н, У, Х`), Greek/tool builders (`Д, П, Б, Л...`), sibilants/soft signs.
   - *Multiplayer Roles*: Initiator (speaker), receiver (follower/cue observer), dedicated headless server.
   - *Mod Environments*: Client-only (`environment: client`), universal/server (`environment: *`).

3. **Boundary Value Analysis (BVA)**:
   Rigorous boundary testing is applied across:
   - *Pronunciation Scoring Thresholds*: Exact match ($1.00$), slight vowel accent ($0.875$), reduction ($0.833$), passing margin ($0.650$), sub-threshold fail ($0.640$), zero-length input ($0.00$).
   - *Adaptive Timers*: Monotonic decrease ($18\text{s} > 14\text{s} > 9\text{s} > 5\text{s}$).
   - *Network Latency Buffer*: $1500\text{ms}$ grace period protecting players up to $1500\text{ms}$ ping.
   - *HUD Dimension Bounds*: Width constrained to $\ge 150\text{px}$ and $\le 180\text{px}$; height constrained to $28\text{px}$ - $38\text{px}$ (max $45\text{px}$).
   - *Pedagogical Progression Letter Levels*: Level $\le 6$ (immediate substitution) vs Level $> 6$ (requiring globalLevel $\ge \text{lvl}$ and stars $\ge 3$).

4. **Pairwise / Combinatorial Testing**:
   Combines orthogonal parameters to detect unexpected interactions:
   - Client mods vs Server mods against Headless vs GUI execution.
   - Single-word challenge vs Multi-word sentence against Speech HUD rendering state.
   - Offline UUID Version 3 generation across upper/lower/special characters.

5. **Workload & Stress Testing**:
   - Verification network packets constrained to $\le 64$ bytes to prevent Netty buffer exhaustion.
   - Headless dedicated server boot stress test invoking `FabricServerLauncher` with all universal mods loaded.
   - Multi-player offline UUID isolation preventing state collision under concurrent play.

6. **Progressive Testability & Zero Facades**:
   - Tests execute real computations and assertions. Dummy facades that unconditionally return success are strictly prohibited.
   - During active development, tests for upcoming milestones gracefully report `PENDING (Milestone)` in progressive execution mode, while `-Strict` enforces 100% completion across all 55 tests.

---

## 2. Expected Output Derivation & Authoritative Oracles

Every test assertion is anchored in an authoritative reference oracle:

### 2.1 RFC 4122 Version 3 Offline UUID Oracle
- **Source**: RFC 4122 §4.3 and Java `UUID.nameUUIDFromBytes`.
- **Derivation**: UTF-8 bytes of `"OfflinePlayer:" + username` hashed with MD5. Nibble 4 of byte 6 set to `0x3` (Version 3); top 2 bits of byte 8 set to `0b10` (RFC 4122 Variant).
- **Test Vectors**:
  - `"Steve"` $\rightarrow$ `5627dd98-e6be-3c21-b8a8-e92344183641`
  - `"Alex"` $\rightarrow$ `36532b5e-c442-3dbb-a24c-c7e55d0f979a`
  - `"AmigoRusso"` $\rightarrow$ `c705fe56-3283-3c28-a50c-360444790d19`
  - `"Player1"` $\rightarrow$ `681f539b-8bb8-3f85-85e5-a2945f6c6539`

### 2.2 Cyrillic Text Normalization & Yo/Ye Equivalence Oracle
- **Source**: Russian orthography rules, Vosk speech model vocabulary (`words.txt`), and `Scorer.java`.
- **Rules**: Lowercasing under `ru-RU`; stripping of combining diacritics (`\u0300-\u036F`); precomposed acute removal; universal equivalence of `ё` (U+0451) and `е` (U+0435); stripping of formatting codes (`§[0-9a-fk-or]`) and non-Cyrillic punctuation.

### 2.3 Levenshtein Pronunciation Accuracy Oracle
- **Formula**:
  $$\text{Score} = \max\left(0.0, 1.0 - \frac{\text{LevenshteinDistance}(\text{spoken}, \text{target})}{\max(\text{len}(\text{spoken}), \text{len}(\text{target}))}\right)$$
- Exact match = $1.00$; Pass threshold = $0.65$ (or $0.85$ for full cooperative sentences).

### 2.4 Cyrillic Two-Axis Progression Calibration Oracle (R3)
- **Source**: `Alphabet.java` two-axis algorithm.
- **Axis 1 (Identical Letters)**: $\text{LEVEL} \le 6$ (`А, О, Т, К, М, Е`).
  - Condition: `isIdentical(ch) == true`.
  - Behavior: Immediate natural substitution on word stars ($\ge 1$). Zero friction.
- **Axis 2 (Non-Identical Letters)**: $\text{LEVEL} > 6$ (`В, Р, С, Н, У, Х, Д, П, Б, Л...`).
  - Condition: `isIdentical(ch) == false`.
  - Gating: Requires both $\text{globalLevel} \ge \text{lvl}$ AND word $\text{stars} \ge 3$.
  - Anti-Premature Locking: Collecting basic oak wood (`Дерево`) unlocks only `о` and `е`; letters `д` (13), `в` (7), `р` (8) remain strictly locked.

### 2.5 Speech HUD Suppression & Header Oracle (R2)
- **Source**: `ChallengeSession.java` and `ChallengeHud.java`.
- **Suppression**: When challenge is single-word (`kind.equals("word")` or $\text{words}(\text{target}).\text{size} == 1$), `speechFeedback` is `null`.
- **Header**: String must strictly equal `"disse: "` (no `"O Que Eu Disse"`).
- **Dimensions**: $\text{width} \ge 150\text{px}$ (bounded, max $180\text{px}$), $\text{height} = 28\text{px} - 38\text{px}$ (max $45\text{px}$).

### 2.6 Multiplayer Cooperative Protocol & Ranking Oracle (R1)
- **Source**: `Packets.java`, `ChallengeManager.java`, and `Progress.java`.
- **Invocations**: Item requests (`coop.request_wood`, `coop.request_food`), Waypoint beacon (`coop.waypoint`), Follow me (`coop.follow`), Help (`coop.help`).
- **Dynamic Challenges**: "The floor is lava" (`coop.lava` / `пол - это лава`).
- **5 Pedagogical Tiers**:
  1. `Новичок` ($0 - 49$ pts)
  2. `Ученик` ($50 - 149$ pts)
  3. `Знаток` / `Подмастерье` ($150 - 299$ pts)
  4. `Мастер` ($300 - 499$ pts)
  5. `Легенда` ($500+$ pts)

### 2.7 Mod Segregation & Headless Boot Oracle (R4, AGENTS.md L13)
- **Source**: `AGENTS.md` Lesson L13.
- **Client-Only Mods**: `omnimovement`, `stack-to-nearby-chests`, `entityculling`, `appleskin` (deployed exclusively to `Instancia_Cliente\mods\`).
- **Universal Mods**: `InventorySorter`, `krypton` (deployed to both `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`).
- **Dedicated Server Dry-Run**: Clean boot with `--help`, zero `ClassNotFoundException`, zero `MixinApplyException`.

---

## 3. Feature Inventory & 4-Tier Test Matrix (55 Tests)

The canonical test suite comprises **55 tests** across 4 tiers:

| ID | Tier | Name | Req | Milestone | Method | Oracle / Specification Summary |
|---|---|---|---|---|---|---|
| `T1_F01` | Tier 1 | F1: Minecraft 1.20.1 Fabric Engine & Manifest | R5 | M1 | CP | Validates Fabric Loader 0.16.x & MC 1.20.1 in mmc-pack.json |
| `T1_F02` | Tier 1 | F2: Client Optimization Stack | R4 | M1 | CP | Asserts sodium, lithium, ferritecore presence in client mods |
| `T1_F03` | Tier 1 | F3: Portable OpenJDK 17 Runtime | R5 | M1 | CP | Validates 64-bit Java 17 binary in `Java17\bin\java.exe` |
| `T1_F04` | Tier 1 | F4: 1-Click Client Launcher (Jogar.bat) | R5 | M1 | CP | Asserts UTF-8 chcp 65001, Java auto-detection, and launcher exec |
| `T1_F05` | Tier 1 | F5: Portable UltimMC Launcher Layout | R5 | M1 | CP | Asserts isolated data directory and UltimMC.exe binary |
| `T1_F06` | Tier 1 | F6: SayIt Mod Integration & Penalty Config | R5 | M2 | CP | Verifies sayit JAR and challenge item penalty configuration |
| `T1_F07` | Tier 1 | F7: Vosk Offline Russian Acoustic Model | R5 | M2 | CP | Verifies HCLG.fst and words.txt in small-ru-0.22 model |
| `T1_F08` | Tier 1 | F8: Cyrillic Dictionary Parsing & Normalization | R5 | M2 | BVA | Verifies normalization, accents, formatting, and yo->ye |
| `T1_F09` | Tier 1 | F9: Voice HUD & Scoring Formula Spec | R2 | M2 | BVA | Validates pronunciation scoring formula on reference tokens |
| `T1_F10` | Tier 1 | F10: Persistent Pocket Dictionary (UUID) | R5 | M2 | CP | Validates dictionary indexing by persistent offline UUID |
| `T1_F11` | Tier 1 | F11: TrueType Cyrillic Font Rendering | R5 | M3 | CP | Validates ModernUI and ImmediatelyFast mods |
| `T1_F12` | Tier 1 | F12: Language Reload & Multilingual Search | R5 | M3 | CP | Validates language-reload mod integration (Shift+F3+J) |
| `T1_F13` | Tier 1 | F13: Heracles 4-Tier Russian Curriculum | R5 | M3 | CP | Validates 73 Heracles JSON quest schemas across 4 tiers |
| `T1_F14` | Tier 1 | F14: Bilingual Tooltips Schema | R5 | M3 | CP | Validates russian_tooltips.js KubeJS script |
| `T1_F15` | Tier 1 | F15: Tier-Gated Voice Penalty & Adaptive Timers| R5 | M3 | BVA | Validates monotonic decrease of countdown timers in sayit.json |
| `T1_F16` | Tier 1 | F16: Offline Skin System & LocalSkin Config | R5 | M4 | CP | Validates CustomSkinLoader.json with LocalSkin and ElyBy |
| `T1_F17` | Tier 1 | F17: e4all P2P LAN Tunneling Mod | R5 | M4 | CP | Validates e4all JAR for zero-port-forwarding P2P play |
| `T1_F18` | Tier 1 | F18: Dedicated Server Config (online-mode=false)| R5 | M4 | CP | Asserts online-mode=false and enforce-secure-profile=false |
| `T1_F19` | Tier 1 | F19: Server 1-Click Launcher (Iniciar_Servidor) | R5 | M4 | CP | Asserts Aikar G1GC flags and automated EULA acceptance |
| `T1_F20` | Tier 1 | F20: Portuguese Distribution Manual | R5 | M4 | CP | Asserts GUIA_DISTRIBUICAO_AMIGOS.md covers all core topics |
| `T1_F21` | Tier 1 | F21: Client-Side STT Architecture & Bandwidth | R5 | M4 | Workload | Asserts offlineOnly STT and verification packet <= 64 bytes |
| `T1_F22` | Tier 1 | F22: Canonical E2E Test Runner Self-Verification| R5 | M5 | CP | Asserts test registry self-consistency and coverage integrity |
| `T1_F23` | Tier 1 | F23: Forensic Zero-DRM Audit | R5 | M5 | CP | Confirms zero Mojang auth endpoints or cloud DRM hooks |
| `T1_F24` | Tier 1 | F24: Speech HUD Single-Word Suppression | R2 | M1 | CP | Asserts SpeechFeedback null on single-word item challenges |
| `T1_F25` | Tier 1 | F25: Speech HUD Header "disse: " & Dimensions | R2 | M1 | BVA | Asserts "disse: " header, width <= 180px, height <= 45px |
| `T1_F26` | Tier 1 | F26: Cyrillic Identical Letters Substitution | R3 | M1 | CP | Validates natural substitution for А, О, Е, Т, К, М (lvl <= 6) |
| `T1_F27` | Tier 1 | F27: Cyrillic Non-Identical Slow Progression | R3 | M1 | BVA | Asserts oak_log does NOT prematurely unlock non-identical letters |
| `T1_F28` | Tier 1 | F28: Cooperative Phrase Invocations | R1 | M3 | CP | Validates coop phrases (follow, waypoint, request) & schema |
| `T1_F29` | Tier 1 | F29: Dynamic Kamikaze Mini-Challenges | R1 | M3 | Workload | Validates freely startable "The Floor is Lava" mechanics |
| `T1_F30` | Tier 1 | F30: Cooperative Ranking & Scoring (5 Tiers) | R1 | M3 | BVA | Validates 5 tiers (Новичок to Легенда) and score accumulation |
| `T1_F31` | Tier 1 | F31: QoL Mods Manifest & Segregation (L13) | R4 | M2 | Pairwise | Validates 6 client mods, 2 server mods, 0 client-only on server |
| `T1_F32` | Tier 1 | F32: Dedicated Server Dry-Run Clean Execution | R4, R5| M2 | Workload | Asserts clean headless server execution without mod crash |
| `T2_01` | Tier 2 | T2.1: Cyrillic Stress Mark Stripping | R5 | M2 | BVA | Strips acute stress marks from 5 representative Cyrillic words |
| `T2_02` | Tier 2 | T2.2: Character yo vs ye Universal Equivalence | R5 | M2 | Pairwise | Confirms bidirectional yo <-> ye normalization equivalence |
| `T2_03` | Tier 2 | T2.3: Pronunciation Scoring & Accent Tolerance | R5 | M2 | BVA | Asserts exact match, minor accent, shift, and wrong rejection |
| `T2_04` | Tier 2 | T2.4: Multi-Word Compound Item Token Parsing | R5 | M2 | BVA | Validates full phrase matching and base noun token fallback |
| `T2_05` | Tier 2 | T2.5: Zero-Length & Malformed Audio Transcripts | R5 | M2 | BVA | Rejects empty, whitespace, special chars, and formatting codes |
| `T2_06` | Tier 2 | T2.6: Offline UUID Cryptographic Conformity | R5 | M1 | BVA | Validates RFC 4122 v3 MD5 bits, case sensitivity, and variants |
| `T2_07` | Tier 2 | T2.7: Chat Signature Kick Prevention Check | R5 | M4 | CP | Confirms enforce-secure-profile=false on server.properties |
| `T2_08` | Tier 2 | T2.8: Launcher Error Handling on Missing Java | R5 | M1 | CP | Verifies diagnostic error output and adoptium guide |
| `T2_09` | Tier 2 | T2.9: Server Network Latency Grace Period | R5 | M2 | BVA | Validates 1500ms server grace buffer against timeout drop |
| `T2_10` | Tier 2 | T2.10: Path Whitespace & Non-ASCII Safety | R5 | M1 | BVA | Validates variable quotation and script safety on paths with spaces |
| `T3_01` | Tier 3 | T3.1: Offline Auth ↔ CustomSkinLoader Synergy | R5 | M4 | Pairwise | Verifies username, offline UUID, and LocalSkin path alignment |
| `T3_02` | Tier 3 | T3.2: Heracles Quest ↔ SayIt Dict Alignment | R5 | M3 | Pairwise | Validates 8 core quest items normalize cleanly into dictionary |
| `T3_03` | Tier 3 | T3.3: Quest Difficulty ↔ Adaptive Timer Reduction | R5 | M3 | BVA | Validates strict monotonic decrease: Novice > Apprentice > Master |
| `T3_04` | Tier 3 | T3.4: Server Modpack Filtering (L13 Exclusion) | R4, R5| M4 | Pairwise | Confirms zero client-only rendering mods on dedicated server |
| `T3_05` | Tier 3 | T3.5: e4all LAN Tunnel ↔ Voice Chat Port 24454 | R5 | M4 | Pairwise | Validates UDP port 24454 proxying instructions |
| `T3_06` | Tier 3 | T3.6: Concurrent Multi-Player State Isolation | R1, R5| M4 | Pairwise | Verifies distinct UUIDs prevent player state cache collision |
| `T4_01` | Tier 4 | T4.1: Scenario: Zero-Install Friend First Launch | R5 | M1 | Workload | Simulates first launch with bundled OpenJDK 17 and UltimMC |
| `T4_02` | Tier 4 | T4.2: Scenario: Melty Voice Challenge Acquisition | R5 | M2 | Workload | Simulates oak log acquisition, dictionary lookup, and voice pass |
| `T4_03` | Tier 4 | T4.3: Scenario: Melty Voice Penalty Timeout Drop | R5 | M2 | Workload | Simulates pronunciation failure and DROP penalty enforcement |
| `T4_04` | Tier 4 | T4.4: Scenario: Multiplayer P2P Session via e4all| R1, R5| M4 | Workload | Simulates P2P session setup with cracked offline friends |
| `T4_05` | Tier 4 | T4.5: Scenario: Dedicated Server Survival Boot | R4, R5| M4 | Workload | Executes real headless server dry-run with Aikar flags |
| `T4_06` | Tier 4 | T4.6: Scenario: Microphone Disconnect 'Y' Typing | R5 | M2 | Workload | Simulates keyboard typing fallback on key.keyboard.y |
| `T4_07` | Tier 4 | T4.7: Scenario: Cooperative Session & QoL Synergy | R1, R4| M3 | Workload | Simulates coop phrase invocation, cue sync, score, and QoL mods |

---

## 4. Test Architecture & Runner Specification

### 4.1 Native PowerShell Engine (`run_e2e_tests.ps1`)
- **Location**: `F:\Jogos\Mine\tests\run_e2e_tests.ps1`
- **Compatibility**: Windows 11 PowerShell 5.1 and PowerShell 7+.
- **Encoding**: 100% pure 7-bit ASCII source code; Cyrillic literals stored as Base64 strings decoded in-memory via UTF-8. Eliminates ANSI/OEM code page collisions.
- **Exit Codes**:
  - `0`: All tests passed (or pending in progressive mode).
  - `1`: One or more assertion failures, syntax violations, or strict mode missing dependencies.

### 4.2 CLI Execution Commands

```powershell
# Standard Progressive Mode (Milestone-aware execution)
powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1"

# Strict Verification Mode (Mandatory for complete delivery)
powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict

# Structured JSON Export Mode
powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -ExportJson
```

### 4.3 Output Artifacts
- **Console Log**: Real-time structured progress with test IDs, tier names, execution durations, and color-coded status.
- **Machine-Readable Report**: `F:\Jogos\Mine\tests\test_results_e2e.json` containing timestamp, counters, and detailed evidence per test.

---

## 5. Coverage Thresholds & Quality Gates

To achieve production readiness:
1. **Total Test Count**: Must register and execute at least **55 tests**.
2. **Strict Mode Threshold**: `0` failed tests, `0` pending milestones in final delivery.
3. **Execution Time**: The complete 55-test suite must execute in under **10 seconds** on local NVMe storage.
4. **Zero DRM**: Continuous verification that no cloud authentication or telemetry endpoints exist.
