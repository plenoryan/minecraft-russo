# DISPATCH — Worker M3 (Receiver Visual Notification HUD with Player Head & Item '?')

**Milestone**: M3 (Requirement R2)
**Working Directory**: `F:\Jogos\Mine\.agents\teamwork\worker_r2_m3`
**Authoritative Request**: `F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md`
**Scope & Architecture**: `F:\Jogos\Mine\.agents\teamwork\orchestrator_r2\SCOPE.md`
**Survey Reports to Consult**:
- `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_2\handoff.md` (Sections 1.1, 2, 4.1, 4.2)
- `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_1\handoff.md` (Section 2.3)

## MANDATORY INTEGRITY WARNING
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A teamwork_auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.

## Write Ownership (Exclusive)
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java` (edit)
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\SayItClient.java` (edit to register packet listener)

## Concrete Tasks
1. **Implement `ChallengeHud.java` Receiver Notification**:
   - Record: `CoopCueNotification(String initiatorName, String action, String ruText, String phonetic, String ptText, String objective, long receivedAt, long durationMs)`.
   - Field: `public static volatile CoopCueNotification activeCoopCue = null;`.
   - Handler: `public static void onCoopCue(Packets.CoopCue cue)`:
     * Extracts `initiatorName`, `cueType` (action), `ruText`, `phonetic`, `ptText`, `objective`, `durationTicks` (fallback to 10s if <=0).
     * Populates `activeCoopCue`.
   - In `ChallengeHud.render(class_332 g, float partialTick)`:
     * Check if `activeCoopCue != null`. Check if duration expired (`age > durationMs`).
     * If active, call `renderCoopCueNotification(g, mc, font, cue, age)`.
   - Rendering Implementation (`renderCoopCueNotification`):
     * Card dimensions: ~240w x 38h, centered at top (`(screenW - 240) / 2`, y = 12), dark panel with gold border accent.
     * Initiator player face: `PlayerFaceRenderer.method_44443(g, skin, headX, headY, 24)` resolving skin from `mc.method_1562().method_2874(initiatorName)` or offline UUID fallback `DefaultSkinHelper.method_4648`.
     * Evocative item icon: map action (`wood` -> oak_log, `food` -> bread, `help` -> golden_apple, `follow` -> compass, `waypoint` -> spyglass/recovery_compass, `lava` -> lava_bucket).
     * Question mark `'?'` badge overlay: overlay `'?'` directly on the item icon via `g.method_51432(font, actionStack, iconX, iconY, "?")` (`DrawContext.drawItemInSlot`).
     * Bilingual request texts: e.g. `[initiator] pediu [?]:` + Russian phrase and Portuguese translation in brackets.
     * Duration countdown progress bar.
2. **Register Packet Handler in `SayItClient.java`**:
   - In `SayItClient.init()`:
     `c.receivePacket(Packets.CoopCue.TYPE, ChallengeHud::onCoopCue);`
3. **Verification**:
   - Compile and deploy with `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\build_and_deploy.ps1"`.
   - Verify that `ChallengeHud.class` and `SayItClient.class` compile cleanly with Java 17 and are injected into client and server JARs.
   - Run `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`.
   - Document verification commands and output in `handoff.md`.
