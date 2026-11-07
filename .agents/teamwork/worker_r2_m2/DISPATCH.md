# DISPATCH — Worker M2 (Radial Selection Wheel 'V' & Russian Voice Challenge Gate)

**Milestone**: M2 (Requirement R1)
**Working Directory**: `F:\Jogos\Mine\.agents\teamwork\worker_r2_m2`
**Authoritative Request**: `F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md`
**Scope & Architecture**: `F:\Jogos\Mine\.agents\teamwork\orchestrator_r2\SCOPE.md`
**Survey Reports to Consult**:
- `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_1\handoff.md` (Sections 1.1, 1.2, 1.3, 2.1, 2.2, 4.1, 4.2)
- `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_2\handoff.md` (Section 4.2 programmatic Iris unbind)

## MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

## Write Ownership (Exclusive)
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\RadialMenuScreen.java` (create)
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\SayItClient.java` (move/edit from decompiled into src)
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java` (edit)

## Concrete Tasks
1. **Implement `RadialMenuScreen.java`**:
   - Package `com.spicesfire.sayitenglish.client`.
   - Extends `class_437` (`Screen`). Non-pausing (`method_25421() { return false; }`).
   - 6 sectors centered at (width/2, height/2):
     * Sector 0: `"wood"` (`Мне нужно дерево!`, `Preciso de madeira!`, icon `minecraft:oak_log`)
     * Sector 1: `"food"` (`Мне нужна еда!`, `Preciso de comida!`, icon `minecraft:bread`)
     * Sector 2: `"help"` (`Помоги мне!`, `Me ajude!`, icon `minecraft:golden_apple`)
     * Sector 3: `"lava"` (`Пол - это лава!`, `O chão é lava!`, icon `minecraft:lava_bucket`)
     * Sector 4: `"follow"` (`Следуй за мной!`, `Siga-me!`, icon `minecraft:compass`)
     * Sector 5: `"waypoint"` (`Смотри туда!`, `Olhe lá!`, icon `minecraft:spyglass`)
   - Draw sectors, item icons via `ClientCompat.item`, hover highlight, sector tooltips/labels.
   - Support both Click (mouse click selects sector) and Hold-to-Release (holding 'V', releasing selects highlighted sector). ESC cancels.
   - When sector is selected: close screen (`ClientCompat.openScreen(this.field_22787, null)`) and trigger `ChallengeSession.startCoopChallenge(action, ruText, ptText, icon, 15, Config.get().passScore)`.
2. **Implement Voice Challenge Gating in `ChallengeSession.java`**:
   - Field: `public static String pendingCoopAction = null;`
   - Method: `public static void startCoopChallenge(String action, String ruText, String ptText, String icon, int seconds, int passScore)`:
     * Sets `pendingCoopAction = action;`
     * Creates client `Packets.Challenge` sentence prompt with the exact Russian target phrase and starts listening.
   - In `finish()`:
     * Check `boolean ok = best >= c.passScore();`
     * `String coop = pendingCoopAction; pendingCoopAction = null;`
     * If `coop != null`:
       - If `ok == true`: `ClientPlatform.I.sendToServer(new Packets.CoopInvoke(coop, ""));`
       - If `ok == false`: do NOT send any packet! (Strict pedagogical gate).
   - In `stop()`: `pendingCoopAction = null;` (Cancellation leaves 0 packets sent).
3. **Update `SayItClient.java`**:
   - Register `radialKey = ClientCompat.key("key.sayitenglish.radial", 86);` (GLFW key 'V' = 86).
   - In tick handler, when `radialKey.method_1436()` fires and `ClientCompat.currentScreen(mc) == null`, open `new RadialMenuScreen()`.
   - Add programmatic Iris shader reload unbind guard on first tick (per Survey 2 handoff).
4. **Verification**:
   - Test compilation using Java 17 `javac` with `client-intermediary.jar` classpath.
   - Verify that `pendingCoopAction` logic enforces strict gating.
   - Document verification commands and output in `handoff.md`.


## 2026-11-07T06:08:39Z
You are worker_r2_m2, working as an implementation Worker on Milestone M2 for the SayIt mod (Minecraft 1.20.1 Fabric).
Working directory: F:\Jogos\Mine\.agents\teamwork\worker_r2_m2
Authoritative Requirements: F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md
Your Dispatch Task: F:\Jogos\Mine\.agents\teamwork\worker_r2_m2\DISPATCH.md
Scope Reference: F:\Jogos\Mine\.agents\teamwork\orchestrator_r2\SCOPE.md

DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

Your tasks:
1. Create RadialMenuScreen.java in src/com/spicesfire/sayitenglish/client/: 6 sectors (wood, food, help, lava, follow, waypoint) with item icons, Cyrillic & Portuguese labels, non-pausing Screen, mouse angular math, tap/hold modes.
2. Update ChallengeSession.java: Add pendingCoopAction, implement startCoopChallenge(...), strictly condition CoopInvoke dispatch in finish() ONLY upon speech validation (best >= passScore), clear on cancel/fail.
3. Update SayItClient.java: Register key 'V' (key.sayitenglish.radial, 86), tick handler to open RadialMenuScreen, programmatic Iris unbind guard.
4. Verify compilation via javac with Java 17 and intermediary classpath.

Write a complete handoff report to F:\Jogos\Mine\.agents\teamwork\worker_r2_m2\handoff.md and report back via send_message.
