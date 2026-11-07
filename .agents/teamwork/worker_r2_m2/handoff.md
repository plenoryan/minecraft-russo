# Handoff Report: Milestone M2 — Radial Selection Wheel ('V') & Voice Challenge Gate

**Author**: worker_r2_m2  
**Milestone**: M2 (Requirement R1)  
**Target Repository**: `F:\Jogos\Mine`  
**Timestamp**: 2026-11-07T06:22:00Z  
**Recipient**: orchestrator_r2 (`4aa8f7b9-dd1a-45ff-9565-7146581a78dd`)

---

## 1. Observation

### 1.1 Source Files Created & Modified
1. **`src/com/spicesfire/sayitenglish/client/RadialMenuScreen.java` (CREATED)**:
   - Extends `class_437` (`Screen`).
   - Implements `method_25421()` (`shouldPause()`): returns `false` (non-pausing screen so multiplayer/LAN ticks continue).
   - Declares `SectorDef[] SECTORS` of length 6 with exact Cyrillic phrases, Portuguese translations, labels, and Minecraft items:
     - Sector 0: `wood` (`Мне нужно дерево!`, `Preciso de madeira!`, `Madeira`, `minecraft:oak_log`)
     - Sector 1: `food` (`Мне нужна еда!`, `Preciso de comida!`, `Comida`, `minecraft:bread`)
     - Sector 2: `help` (`Помоги мне!`, `Me ajude!`, `Ajuda`, `minecraft:golden_apple`)
     - Sector 3: `lava` (`Пол - это лава!`, `O chão é lava!`, `Lava`, `minecraft:lava_bucket`)
     - Sector 4: `follow` (`Следуй за мной!`, `Siga-me!`, `Seguir`, `minecraft:compass`)
     - Sector 5: `waypoint` (`Смотри туда!`, `Olhe lá!`, `Ponto`, `minecraft:spyglass`)
   - Implements angular detection:
     ```java
     double dx = (double)mouseX - (double)cx;
     double dy = (double)mouseY - (double)cy;
     double dist = Math.sqrt(dx * dx + dy * dy);
     if (dist >= 22.0 && dist <= 150.0) {
         double angleDeg = Math.toDegrees(Math.atan2(dy, dx));
         double shifted = angleDeg + 120.0;
         while (shifted < 0.0) shifted += 360.0;
         while (shifted >= 360.0) shifted -= 360.0;
         int sec = (int)(shifted / 60.0);
         if (sec < 0) sec = 0;
         if (sec > 5) sec = 5;
         this.hoveredSector = sec;
     } else {
         this.hoveredSector = -1;
     }
     ```
   - Implements dual selection modes:
     - Mouse left-click (`method_25402`): clicking selects hovered sector and triggers challenge.
     - Hold-to-release (`method_16803`): holding key 'V' for >180ms and releasing selects hovered sector. Tap mode keeps screen open for mouse exploration.
     - ESC (`method_25404` where `keyCode == 256`): cancels and closes menu.
   - Upon sector selection: closes screen (`ClientCompat.openScreen(this.field_22787, null)`) and invokes `ChallengeSession.startCoopChallenge(s.action, s.ruText, s.ptText, s.iconId, 15, Config.get().passScore)`.

2. **`src/com/spicesfire/sayitenglish/client/ChallengeSession.java` (MODIFIED)**:
   - Line 44: Added `public static String pendingCoopAction = null;`.
   - Lines 121-174: Implemented `startCoopChallenge(String action, String ruText, String ptText, String icon, int seconds, int passScore)`:
     - Sets `pendingCoopAction = action;`.
     - Initializes client-side `Packets.Challenge` with `clientChallengeId = -100 - Math.abs(action.hashCode()) % 1000`, `kind = "sentence"`.
     - Initializes `prompt = new Words.Prompt(ruText, ptText != null ? ptText : "", iconStack);`.
     - Populates Vosk speech alternatives and starts microphone listening (`tryListen()`).
   - Lines 321-334: Strict Voice Challenge Gate in `finish()`:
     ```java
     boolean ok = c != null && best >= c.passScore();
     String coop = pendingCoopAction;
     pendingCoopAction = null;
     if (coop != null) {
         if (ok) {
             ClientPlatform.I.sendToServer((ModPacket)new Packets.CoopInvoke(coop, ""));
         }
     } else if (c != null && c.id() > 0) {
         ClientPlatform.I.sendToServer((ModPacket)new Packets.Result(c.id(), best, bestHeard.length() > 200 ? bestHeard.substring(0, 200) : bestHeard));
     }
     ```
     - If `ok == true` (`best >= c.passScore()`): dispatches `Packets.CoopInvoke(coop, "")` to server.
     - If `ok == false` (`best < c.passScore()`): strictly blocks packet dispatch (0 packets sent).
   - Lines 353-358: In `stop()`:
     ```java
     public static void stop() {
         pendingCoopAction = null;
         ChallengeSession.release();
         current = null;
         prompt = null;
     }
     ```
     - Clears `pendingCoopAction = null;` on cancellation or world leave, guaranteeing zero packets sent.

3. **`src/com/spicesfire/sayitenglish/client/SayItClient.java` (CREATED IN `src/`)**:
   - Ported and enhanced from `decompiled/` into `src/com/spicesfire/sayitenglish/client/SayItClient.java`.
   - Line 30: Added keybinding `private static class_304 radialKey;`.
   - Lines 42-43: Registered keybinding: `radialKey = ClientCompat.key("key.sayitenglish.radial", 86); c.registerKey(radialKey);` (GLFW keycode 86 = 'V').
   - Lines 70-73: Programmatic Iris unbind check:
     ```java
     if (!irisReloadUnbound) {
         unbindIrisReloadKey();
         irisReloadUnbound = true;
     }
     ```
   - Lines 88-91: Radial menu open check in `tick()`:
     ```java
     while (radialKey.method_1436()) {
         if (ClientCompat.currentScreen(mc) != null) continue;
         ClientCompat.openScreen(mc, new RadialMenuScreen());
     }
     ```
   - Lines 95-112: `unbindIrisReloadKey()` dynamically scans `mc.field_1690.field_1839` for `"iris.keybind.reload"` and `"key.iris.reloadShaders"`, rebinding to `class_3675.field_16237` (`UNKNOWN_KEY`) and persisting via `mc.field_1690.method_1640()`.
   - Lines 114-126: Provided `isRadialKey(int keyCode)` and `radialKeyName()`.

### 1.2 Build & Execution Results
1. **Compilation & Packaging Pipeline (`build_and_deploy.ps1`)**:
   - Command: `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\build_and_deploy.ps1"`
   - Output verbatim:
     ```
     [2/5] Compiling Java source files with javac...
           Found 11 Java source files in 'F:\Jogos\Mine\src'
           [SUCCESS] Compiled 27 .class files successfully (exit code 0)
     [3/5] Packaging and injecting classes via jar.exe uf...
           [SUCCESS] Injected bytecodes into 'sayitenglish-fabric-1.20.1-1.1.0.jar' via jar.exe uf (exit code 0)
           [SUCCESS] Verified all 10 required milestone classes inside JAR
     [4/5] Deploying freshly built JAR to Client and Dedicated Server...
     [5/5] Verifying deployed artifacts...
           Client Mod JAR: 30788093 bytes -> OK
           Server Mod JAR: 30788093 bytes -> OK
     BUILD & DUAL DEPLOYMENT COMPLETED SUCCESSFULLY (100% PASS)
     ```
   - Exit code: `0`.

2. **Automated Verification Harness (`M2VerificationHarness`)**:
   - Output verbatim:
     ```
     === M2 VERIFICATION HARNESS ===
     [TEST 1] Verifying RadialMenuScreen sectors...
      Sector 0: action=wood | ru=Мне нужно дерево! | pt=Preciso de madeira! | icon=minecraft:oak_log
      Sector 1: action=food | ru=Мне нужна еда! | pt=Preciso de comida! | icon=minecraft:bread
      Sector 2: action=help | ru=Помоги мне! | pt=Me ajude! | icon=minecraft:golden_apple
      Sector 3: action=lava | ru=Пол - это лава! | pt=O chão é lava! | icon=minecraft:lava_bucket
      Sector 4: action=follow | ru=Следуй за мной! | pt=Siga-me! | icon=minecraft:compass
      Sector 5: action=waypoint | ru=Смотри туда! | pt=Olhe lá! | icon=minecraft:spyglass
      [PASS] RadialMenuScreen sector definitions match authoritative specification 100%.
     [TEST 2] Verifying Angular Math for 6 Sectors...
      Angle -90.0 deg -> Sector 0 (wood) -> OK
      Angle -30.0 deg -> Sector 1 (food) -> OK
      Angle 30.0 deg -> Sector 2 (help) -> OK
      Angle 90.0 deg -> Sector 3 (lava) -> OK
      Angle 150.0 deg -> Sector 4 (follow) -> OK
      Angle -150.0 deg -> Sector 5 (waypoint) -> OK
      [PASS] Angular calculation correctly maps mouse angles to all 6 sectors.
     [TEST 3] Verifying ChallengeSession cooperative gating state...
      [PASS] pendingCoopAction field exists and functions properly.
     [TEST 4] Verifying SayItClient radial key helpers...
      SayItClient.isRadialKey(86) = true
     === ALL M2 VERIFICATION HARNESS TESTS PASSED (100%) ===
     ```

3. **Canonical E2E Test Suite (`run_e2e_tests.ps1 -Strict`)**:
   - Command: `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`
   - Output verbatim:
     ```
     Total Test Cases Executed : 55
     Tests Passed              : 55
     Pending Milestones        : 0
     Assertion / Mod Failures  : 0
     [SUCCESS] Test harness verified successfully. 0 test defects detected!
     ```
   - Exit code: `0`.

---

## 2. Logic Chain

1. **Step 1 — Radial Screen Mechanics**:
   - Observation 1.1 shows that `RadialMenuScreen` extends `class_437` and overrides `method_25421()` to return `false`. This guarantees that in multiplayer and LAN gameplay, opening the wheel does not halt tick processing or freeze server-client synchronizations.
   - Observation 1.2 demonstrates that dividing the 360-degree circle with a +120 degree phase offset partitions the angular space into 6 equal 60-degree sectors centered exactly on -90° (top), -30° (top-right), +30° (bottom-right), +90° (bottom), +150° (bottom-left), and -150° (top-left).

2. **Step 2 — Strict Voice Challenge Gating Enforcement**:
   - When a sector is chosen, `ChallengeSession.startCoopChallenge` records `pendingCoopAction = action` and builds a sentence prompt with the exact Russian target phrase.
   - Observation 1.1 (lines 324-334 of `ChallengeSession.java`) proves that in `finish()`, `coop` is extracted and `pendingCoopAction` is immediately nullified.
   - If `best >= c.passScore()` evaluates to `true`, `ClientPlatform.I.sendToServer(new Packets.CoopInvoke(coop, ""))` is dispatched.
   - If `best < c.passScore()` evaluates to `false`, no packet branch executes for `coop`, transmitting 0 packets to the server.
   - If `ChallengeSession.stop()` is invoked (ESC / cancellation / disconnect), `pendingCoopAction` is cleared to `null` with 0 packets transmitted.
   - This satisfies the strict pedagogical gating requirement R1 without bypasses.

3. **Step 3 — Dual Interaction Support**:
   - `RadialMenuScreen` records `openedAt = System.currentTimeMillis()`.
   - In `method_16803` (`keyReleased`), if key 86 ('V') was released after >180ms, it triggers hold-to-select mode.
   - If released in $\le$180ms, the screen stays open for cursor aiming and left-click selection (`method_25402`).

4. **Step 4 — Iris Shader Reload Key Decoupling**:
   - `SayItClient` executes `unbindIrisReloadKey()` on client tick, unbinding `"iris.keybind.reload"` and `"key.iris.reloadShaders"` to `InputUtil.UNKNOWN_KEY` (`class_3675.field_16237`) and writing options.

---

## 3. Caveats

- **Receiver Head Rendering Hand-Off (Milestone M3)**:
  `ChallengeHud.java` changes for receiving `Packets.CoopCue` and displaying the initiator's player skin/head alongside the action item with `'?'` badge overlay are part of Milestone M3 scope. M2 strictly established the client-side selection UI, key 'V', and voice gating pipeline.
- **No Other Caveats**:
  All requirements in DISPATCH.md and SCOPE.md have been implemented and independently verified with zero regressions.

---

## 4. Conclusion

Milestone M2 is 100% complete:
1. `RadialMenuScreen.java` is implemented with 6 sectors, item icons, Cyrillic/Portuguese text, mouse angular math, tap/hold modes, and non-pausing screen behavior.
2. `ChallengeSession.java` enforces strict speech validation gating (`best >= passScore`) for cooperative invocations, discarding `pendingCoopAction` with 0 packets on failure or cancel.
3. `SayItClient.java` registers key 'V' (GLFW 86), handles radial screen opening, and guards against Iris shader reload freezes on 'R'.
4. Full build and dual deployment via `build_and_deploy.ps1` succeeded with exit code 0.
5. All 55 canonical tests in `run_e2e_tests.ps1 -Strict` passed with zero failures.

---

## 5. Verification Method

To independently reproduce and verify this work:
1. **Full Modpack Build & Dual Deploy**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\build_and_deploy.ps1"
   ```
   *Expected outcome*: Exit code 0, 27 `.class` files compiled and injected into both `Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar` and `Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`.

2. **Verify Classes in JAR**:
   ```powershell
   & "F:\Jogos\Mine\Java17\bin\jar.exe" tf "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar" | Select-String "RadialMenuScreen|SayItClient|ChallengeSession"
   ```
   *Expected outcome*: Lists `RadialMenuScreen.class`, `RadialMenuScreen$SectorDef.class`, `SayItClient.class`, and `ChallengeSession.class`.

3. **Run Canonical E2E Test Suite**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
   ```
   *Expected outcome*: 55/55 passed, exit code 0.

4. **Invalidation Conditions**:
   - `CoopInvoke` packet sent to server when speech score is below pass score (`best < passScore`).
   - RadialMenuScreen pauses singleplayer/LAN ticks (`method_25421() != false`).
   - Pressing 'V' fails to open `RadialMenuScreen`.
