# Scope: Round 2 — Radial Wheel, Voice Challenge Gate, Receiver HUD Cue, Iris Unbind & Mods Integration

## Architecture
- **Input & Radial Wheel**: Fabric client keybinding `key.sayitenglish.radial` ('V', GLFW 86) opening `RadialMenuScreen` (non-pausing `method_25421() == false`), rendering 6 interactive sectors (wood, food, help, lava, follow, waypoint) with item icons and Russian/Portuguese labels.
- **Pedagogical Voice Challenge Gating**: Selection of any radial sector starts a client `ChallengeSession` for the target Russian sentence (`pendingCoopAction`). Packet `CoopInvoke` is strictly blocked and ONLY sent to server if speech validation succeeds (`best >= passScore`). On speech failure or cancellation, `pendingCoopAction` is discarded and 0 packets are transmitted.
- **Receiver Visual Notification**: Client reception of `Packets.CoopCue` in `ChallengeHud` rendering a HUD notification card with the initiator's player head (`PlayerFaceRenderer` / player skin / UUID fallback) alongside the action item icon with a bold question mark `'?'` badge overlay (`class_332.method_51432`), bilingual text and duration countdown.
- **Shader Shortcut Decoupling**: Static unbind of `key_iris.keybind.reload:key.keyboard.unknown` in `Instancia_Cliente\options.txt` (ASCII / No-BOM) reinforced by runtime programmatic check in `SayItClient`.
- **Modpack & Vocabulary Integration**: Symmetrical integration of universal Fabric 1.20.1 mods `Farmer's Respite` and `Comforts` into `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`, with bilingual assets (`ru_ru.json`, `pt_br.json`) and curriculum definitions in `russian_tooltips.js`.
- **Verification & E2E Testing**: `build_and_deploy.ps1` compiling and updating JARs; `tests\run_e2e_tests.ps1` extended to 62 tests (T1_F33-T1_F38, T4_08) running 100% green in `-Strict` mode.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Iris Shader Key 'R' Unbind | Set key_iris.keybind.reload to key.keyboard.unknown in options.txt and add client runtime check | M1 | Survey Exp 2, 3 / R3 |
| 2 | Farmer's Respite Mod Provisioning | Download and deploy Farmersrespite-Fabric-3.0.2 to client and server | M1 | Survey Exp 3 / R4 |
| 3 | Comforts Mod Provisioning | Download and deploy comforts-fabric-6.4.0+1.20.1 to client and server | M1 | Survey Exp 3 / R4 |
| 4 | Everyday / Furniture Russian Vocabulary | Add ru_ru and pt_br lang files for farmersrespite & comforts, and update ruCurriculum in russian_tooltips.js | M1 | Survey Exp 3 / R4 |
| 5 | Radial Wheel Keybinding 'V' | Register key.sayitenglish.radial (key 86) in SayItClient and ClientCompat | M2 | Survey Exp 1 / R1 |
| 6 | Radial Wheel UI Screen | Create RadialMenuScreen with 6 action sectors, mouse angle math, item icons, hold/tap modes | M2 | Survey Exp 1 / R1 |
| 7 | Strict Russian Voice Challenge Gating | ChallengeSession.startCoopChallenge sets pendingCoopAction; CoopInvoke only sent on speech approval | M2 | Survey Exp 1 / R1 |
| 8 | Client Packet Reception for CoopCue | SayItClient registers receivePacket for Packets.CoopCue.TYPE -> ChallengeHud::onCoopCue | M3 | Survey Exp 2 / R2 |
| 9 | Receiver Visual Notification HUD | ChallengeHud renders HUD banner with initiator player head, item icon + '?' badge, bilingual text | M3 | Survey Exp 2 / R2 |
| 10 | Standardized Build & Deployment Pipeline | Run build_and_deploy.ps1 to compile updated classes and deploy JARs | M4 | Survey Exp 1, 2, 3 / R5 |
| 11 | Extended E2E Test Suite (62 tests) | Extend run_e2e_tests.ps1 with T1_F33-T1_F38 and T4_08, passing 100% in -Strict mode | M4 | Survey Exp 3 / R5 |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Iris Key Unbind & Everyday Mods Integration | options.txt, Instancia_Cliente\mods, Servidor_Dedicado\mods, kubejs\assets, russian_tooltips.js | none | DONE (worker_r2_m1: b7702c96-3ad7-4115-906a-f7ebb32e53bd; options.txt unbound No-BOM, Farmersrespite + comforts deployed symmetrically, 28 JSON lang files valid, ruCurriculum updated, server dry-run clean) |
| M2 | Radial Selection Wheel ('V') & Voice Challenge Gate | RadialMenuScreen.java, SayItClient.java, ChallengeSession.java | none | DONE (worker_r2_m2: 858e65e1-c36e-4f40-9d95-d5fd60039644; RadialMenuScreen 6 sectors non-pausing, key 'V' GLFW 86, strict speech gate pendingCoopAction) |
| M3 | Receiver Visual Notification (Head + Item '?') | ChallengeHud.java, SayItClient.java | M2 | DONE (worker_r2_m3: 985e2a58-4992-4e1f-b2ab-a4dc3e758312; ChallengeHud receiver with PlayerFaceRenderer, action item + '?' badge DrawContext.method_51432, bilingual text, SayItClient packet handler) |
| M4 | Build Pipeline, Dual Deploy & E2E Validation (62 tests) | build_and_deploy.ps1, tests\run_e2e_tests.ps1 | M1, M2, M3 | DONE (test_writer_r2_m4: 881c8fd4-1486-4f38-873e-5e32b84012ea; build_and_deploy.ps1 compiled 28 classes, run_e2e_tests.ps1 extended to 62 tests passing 100% in -Strict mode) |

## Interface Contracts
### M1: Iris Key & Mods Integration ↔ Game Runtime - VERIFIED
- `Instancia_Cliente\options.txt`: `key_iris.keybind.reload:key.keyboard.unknown` (ASCII, No BOM verified).
- `Farmersrespite-Fabric-3.0.2.jar` & `comforts-fabric-6.4.0+1.20.1.jar`: Present and SHA-identical in `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`.
- `kubejs/assets/farmersrespite/lang/ru_ru.json` & `kubejs/assets/comforts/lang/ru_ru.json`: Cyrillic translations for kettle, coffee, tea, sleeping bag, hammock (Zero-BOM verified).
- `russian_tooltips.js`: `ruCurriculum` populated with phonetic, grammatical, and pedagogical notes.

### M2: Radial Menu ↔ ChallengeSession ↔ Networking - VERIFIED
- Keybinding: `key.sayitenglish.radial`, default keycode 86 (GLFW_KEY_V).
- Screen: `RadialMenuScreen extends Screen`, `shouldPause() == false`.
- 6 Sectors: `wood` (oak_log), `food` (bread), `help` (golden_apple), `lava` (lava_bucket), `follow` (compass), `waypoint` (spyglass/recovery_compass).
- Gating method: `ChallengeSession.startCoopChallenge(action, ruText, ptText, icon, seconds, passScore)`.
- Gating contract: `pendingCoopAction` stored in `ChallengeSession`. When `finish()` executes, if `best >= passScore`, dispatches `ClientPlatform.I.sendToServer(new Packets.CoopInvoke(coopAction, ""))`. If `best < passScore` or cancelled, `pendingCoopAction` is nullified and NO packet is sent.

### M3: CoopCue Packet ↔ Receiver HUD - VERIFIED
- Registration: `ClientPlatform.I.receivePacket(Packets.CoopCue.TYPE, ChallengeHud::onCoopCue)`.
- Record: `CoopCueNotification(initiatorName, action, ruText, phonetic, ptText, objective, receivedAt, durationMs)`.
- Rendering: `PlayerFaceRenderer.method_44443(g, skin, headX, headY, 24)` with skin from `class_634.method_2874(initiatorName)` or offline UUID fallback.
- Badge: `ClientCompat.item(g, actionStack, iconX, iconY)` overlaid with `g.method_51432(font, actionStack, iconX, iconY, "?")`.

### M4: Build & Tests ↔ Acceptance - VERIFIED
- `build_and_deploy.ps1`: Compiles with Java 17 `javac`, packages `sayitenglish-fabric-1.20.1-1.1.0.jar`, updates client and server mods (exit code 0).
- `run_e2e_tests.ps1 -Strict`: 62 tests passing with exit code 0.

## Code Layout
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\`:
  - `client\RadialMenuScreen.java` (NEW)
  - `client\SayItClient.java` (MODIFIED: key 'V', receive CoopCue, runtime iris unbind)
  - `client\ChallengeSession.java` (MODIFIED: coop challenge start, strict voice gating)
  - `client\ChallengeHud.java` (MODIFIED: onCoopCue, player head + item '?' HUD rendering)
- `F:\Jogos\Mine\Instancia_Cliente\options.txt` (MODIFIED: iris reload key unknown)
- `F:\Jogos\Mine\Instancia_Cliente\mods\` & `Servidor_Dedicado\mods\`:
  - `Farmersrespite-Fabric-3.0.2.jar` (NEW)
  - `comforts-fabric-6.4.0+1.20.1.jar` (NEW)
- `F:\Jogos\Mine\Instancia_Cliente\kubejs\`:
  - `assets\farmersrespite\lang\` (NEW)
  - `assets\comforts\lang\` (NEW)
  - `client_scripts\russian_tooltips.js` (MODIFIED)
- `F:\Jogos\Mine\tests\run_e2e_tests.ps1` (MODIFIED: 62 tests)
