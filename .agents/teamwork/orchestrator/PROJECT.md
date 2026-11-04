# Project: SayIt Multiplayer & QoL Expansion (Minecraft 1.20.1 Fabric)

## Architecture
- **Mod Base**: SayIt English / Russian learning Fabric mod (`sayitenglish-fabric-1.20.1-1.1.0.jar`)
- **Language / Toolchain**: Java 17 OpenJDK (`F:\Jogos\Mine\Java17\bin\javac.exe`), intermediary remapped Fabric 1.20.1 (`client-intermediary.jar`), JAR packaging (`jar.exe uf`).
- **Networking Architecture**: Fabric 1.20.1 ServerPlayNetworking / ClientPlayNetworking via custom record-based packets (`CoopInvoke`, `CoopCue`, `CoopSync`) in `Packets.java`.
- **UI / HUD Architecture**: Client HUD overlay in `ChallengeHud.java` using compact rendered boxes, strictly formatted headers, and conditional display based on challenge type.
- **Pedagogical Progression Architecture**: Multi-tier Cyrillic substitution in `Alphabet.java` with two-axis calibration (identical letters immediate, non-identical letters requiring global progression maturity).
- **Multiplayer Cooperative Architecture**: `CoopManager` within `ChallengeManager.java` managing phrase invocations, spatial cues, dynamic mini-challenges ("The floor is lava"), 2-turn dialogues, and 5-tier cooperative ranking (`Progress.java`).
- **Modpack & Performance Architecture**: Curated Fabric 1.20.1 QoL utilities (Omnimovement, Inventory Sorting, Stack to Nearby Chests, Entity Culling, Krypton, AppleSkin) strictly segregated between client and server per AGENTS.md L13.
- **Verification Architecture**: Automated 55-test E2E test suite (`tests\run_e2e_tests.ps1 -Strict`) validating syntax, semantics, networking, progression, and deployment.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Speech HUD Suppression on Words | Suppress speech feedback box on single-word/item challenges | M1 | Survey Exp 2 / R2 |
| 2 | Speech HUD Visual Compaction & Header | Reduce box size (min 150px) and enforce strict header "disse: " | M1 | Survey Exp 2 / R2 |
| 3 | Cyrillic Identical Letters Natural Substitution | Maintain natural substitution for identical letters (А, О, Е, Т, К, М) | M1 | Survey Exp 2 / R3 |
| 4 | Cyrillic Non-Identical Letters Calibration | Require global level maturity and multi-step progress before fixing non-identical letters | M1 | Survey Exp 2 / R3 |
| 5 | Curated QoL & Dynamism Mods | Provision 6 zero-vocabulary Fabric 1.20.1 mods via automated downloader | M2 | Survey Exp 3 / R4 |
| 6 | Mod Segregation & Server Dry-Run | Segregate client-only mods from dedicated server per AGENTS.md L13 and verify server launch | M2 | Survey Exp 3 / R4 |
| 7 | Multiplayer Cooperative Packets | Implement CoopInvoke, CoopCue, CoopSync packets in Packets.java | M3 | Survey Exp 1 / R1 |
| 8 | Practical Phrase Invocations | Support practical phrase triggers (request item, waypoint beacon, follow me) | M3 | Survey Exp 1 / R1 |
| 9 | Dynamic / Kamikaze Challenges | Implement freely startable dynamic mini-challenges ("The floor is lava", movement) | M3 | Survey Exp 1 / R1 |
| 10 | Cooperative Dialogues & Cues | Random 2-turn dialogues and bilingual visual/audio cues for teammate learning | M3 | Survey Exp 1 / R1 |
| 11 | Cooperative Ranking & Scoring | 5-tier pedagogical ranking (Новичок to Легенда) and leaderboards | M3 | Survey Exp 1 / R1 |
| 12 | Standardized Build & Dual Deployment | Scripted javac compilation and dual deployment to Instancia_Cliente and Servidor_Dedicado | M4 | Survey Exp 3 / R5 |
| 13 | Canonical E2E Test Suite (55 tests) | Extend run_e2e_tests.ps1 to 55 tests covering all R1-R5 acceptance criteria | M4 | Survey Exp 3 / R5 |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Speech HUD & Cyrillic Calibration | Alphabet.java, ChallengeHud.java, ChallengeSession.java | none | DONE (worker_m1: 124a6391-6ba5-4039-801a-87fcd6b8bbb1; HUD suppressed on words, compact 'disse: ', 2-axis Cyrillic gating) |
| M2 | QoL & Dynamism Mods Integration | Download script, client/server mods folders, server smoke test | none | DONE (worker_m2: 8f02973f-cc40-42c4-9905-941e9df6a338; 6 mods provisioned, server dry-run clean) |
| M3 | Multiplayer Cooperative Mechanics | Packets.java, Progress.java, ChallengeManager.java, CoopManager | M1 | DONE (worker_m3: eb27deb1-4303-4b58-9900-a4cc1969d89e; Packets, Progress, CoopManager, ranking, dialogues, cues) |
| M4 | Build Pipeline, Dual Deploy & E2E Validation | build_and_deploy.ps1, tests\run_e2e_tests.ps1 (55 tests) | M1, M2, M3 | DONE (worker_m4: 68a6b182-5b8c-4690-b814-3f9ba611ad9c; build_and_deploy.ps1 executed, dual deployment verified, 55/55 E2E tests pass in strict mode) |

## Interface Contracts
### HUD & Alphabet (M1) ↔ Challenge Session - VERIFIED
- `ChallengeHud.SpeechFeedback(heard, nativeTranslation, target, score, success, kind, shownAt)`
- `ChallengeSession.finish()` only creates `SpeechFeedback` if `"sentence".equals(c.kind()) || Scorer.words(prompt.target()).size() > 1`.
- `Alphabet.computeUnlockedLetters(wordStars, heatMap)` checks `isIdentical(ch)` and `calculateLevel(totalStars) >= letterLevel`.

### Networking & Coop Mechanics (M3) ↔ Client/Server - VERIFIED
- `Packets.CoopInvoke(actionType, targetPlayerId, subject, arg)`: Client -> Server.
- `Packets.CoopCue(cueType, initiatorName, messageBilingual, x, y, z, durationTicks)`: Server -> Client(s).
- `Packets.CoopSync(score, completedCount, rankTitle, activeChallengeName, timeRemaining)`: Server -> Client.
- `Progress.addCoopScore(points)` and `Progress.getCoopRank()` returning 5 tiers.
- `CoopManager.tick(server, now)` orchestrating practical phrases, dynamic challenges, dialogues.

### Mod Segregation Contract (M2) - VERIFIED
- Client-Only Mods: `omnimovement`, `stack-to-nearby-chests`, `entityculling`, `appleskin` deployed exclusively to `Instancia_Cliente\mods\`.
- Universal Mods: `InventorySorter`, `krypton` deployed to both `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`.
- Dedicated server dry-run passes with exit code 0.

### Build & Test Contract (M4) - VERIFIED
- `build_and_deploy.ps1`: Compiles all `src/**/*.java` with Java 17 `javac`, updates `sayitenglish-fabric-1.20.1-1.1.0.jar` via `jar uf`, copies to `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`.
- `run_e2e_tests.ps1 -Strict`: Runs 55 assertions, exits with code 0 on complete pass (55/55 PASSED).

## Code Layout
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\`: Mod Java source code
  - `Alphabet.java` (Progression calibration)
  - `ChallengeManager.java` (Server challenge & CoopManager)
  - `Packets.java` (Networking & custom packet records)
  - `Progress.java` (Cooperative scoring, ranking, persistence)
  - `client\ChallengeHud.java` (HUD overlay rendering, compact box, "disse: ")
  - `client\ChallengeSession.java` (Session lifecycle & speech feedback filter)
- `F:\Jogos\Mine\Instancia_Cliente\mods\`: Client mod files (33 JARs)
- `F:\Jogos\Mine\Servidor_Dedicado\mods\`: Server mod files (12 JARs)
- `F:\Jogos\Mine\tests\run_e2e_tests.ps1`: Canonical E2E test runner (55 tests)
- `F:\Jogos\Mine\build_and_deploy.ps1`: Build and deployment automation script
