# Handoff Report: Survey of Radial Wheel UI & Voice Challenge Gating

**Milestone**: Survey Radial Wheel UI & Voice Challenge Gating  
**Author**: survey_exp_1 (Explorer)  
**Target Repository**: `F:\Jogos\Mine`  
**Timestamp**: 2026-11-07T06:02:00Z  

---

## 1. Observation

### 1.1 Keybinding Registration & Input Handling in Fabric 1.20.1 SayIt Mod
- In `F:\Jogos\Mine\decompiled\com\spicesfire\sayitenglish\client\SayItClient.java`:
  - Lines 28-29: Keybindings declared as `private static class_304 typeKey; private static class_304 settingsKey;`.
  - Lines 36-40:
    ```java
    ClientPlatform c = ClientPlatform.I;
    typeKey = ClientCompat.key("key.sayitenglish.type", 89);
    c.registerKey(typeKey);
    settingsKey = ClientCompat.key("key.sayitenglish.settings", 85);
    c.registerKey(settingsKey);
    ```
  - Lines 76-83:
    ```java
    while (typeKey.method_1436()) {
        if (ChallengeSession.current == null || ClientCompat.currentScreen(mc) != null) continue;
        ClientCompat.openScreen(mc, new TypeAnswerScreen());
    }
    while (settingsKey.method_1436()) {
        if (ClientCompat.currentScreen(mc) != null) continue;
        ClientCompat.openScreen(mc, new SettingsScreen(null));
    }
    ```
- In `F:\Jogos\Mine\decompiled\com\spicesfire\sayitenglish\client\ClientCompat.java`:
  - Lines 53-59:
    ```java
    public static class_437 currentScreen(class_310 mc) {
        return mc.field_1755;
    }
    public static void openScreen(class_310 mc, class_437 screen) {
        mc.method_1507(screen);
    }
    public static class_304 key(String name, int keyCode) {
        return new class_304(name, class_3675.class_307.field_1668, keyCode, "key.category.sayitenglish.main");
    }
    ```
- In `F:\Jogos\Mine\decompiled\com\spicesfire\sayitenglish\platform\fabric\FabricClientPlatform.java`:
  - Line 89: `KeyBindingHelper.registerKeyBinding((class_304)key);`
  - Line 59: `ClientTickEvents.END_CLIENT_TICK.register(client -> listener.run());`
- Class & Method Obfuscation/Intermediary Mappings verified via `javap` on `client-intermediary.jar`:
  - `class_304`: `net.minecraft.client.option.KeyBinding`
  - `class_310`: `net.minecraft.client.MinecraftClient`, `method_1551()` = `getInstance()`, `method_1507(screen)` = `setScreen(Screen)`
  - `class_437`: `net.minecraft.client.gui.screen.Screen`
  - `class_332`: `net.minecraft.client.gui.DrawContext`
  - `method_1436()` on `class_304`: `KeyBinding.wasPressed()`
  - `method_1434()` on `class_304`: `KeyBinding.isPressed()`
  - `class_3675.class_307.field_1668`: `InputUtil.Type.KEYSYM`
  - GLFW key code for 'V': `86` (0x56)
  - GLFW key code for 'Y': `89` (0x59)
  - GLFW key code for 'U': `85` (0x55)

### 1.2 Screen Life Cycle & Mouse/Key Event Handlers
- Verified via `javap` on `net.minecraft.class_437` and `net.minecraft.class_4069`:
  - `method_25426()`: `init()`
  - `method_25394(class_332 g, int mouseX, int mouseY, float partialTick)`: `render(DrawContext context, int mouseX, int mouseY, float delta)`
  - `method_25421()`: `shouldPause()` (returning `false` allows the game to keep running in background during multiplayer)
  - `method_25419()`: `close()` (`ClientCompat.openScreen(this.field_22787, null)`)
  - `method_25404(int key, int scan, int mods)`: `keyPressed(int keyCode, int scanCode, int modifiers)`
  - `method_16803(int key, int scan, int mods)`: `keyReleased(int keyCode, int scanCode, int modifiers)`
  - `method_25402(double mouseX, double mouseY, int button)`: `mouseClicked(double mouseX, double mouseY, int button)`
  - `method_25406(double mouseX, double mouseY, int button)`: `mouseReleased(double mouseX, double mouseY, int button)`

### 1.3 Speech Challenge Gating & ChallengeSession Mechanics
- In `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java`:
  - Lines 71-74: `start(Packets.Challenge c)` sets `current = c` and initializes `prompt = c.kind().equals("sentence") ? Words.sentence(c.subject(), c.arg()) : Words.word(c.subject());`.
  - Lines 124-146: `tryListen()` initializes `SpeechEngine.Request` with `language`, `target`, `alternatives`, and starts `Microphone.start(...)`.
  - Lines 210-224: `offer(...)` sets `passed = true` when `s >= Math.max(passThreshold, 75)` for sentences.
  - Lines 251-260: `tick()` calls `finish()` when `passed || ChallengeSession.remaining() <= 0.0f`.
  - Lines 262-266: In `finish()`, `boolean ok = best >= c.passScore();` determines challenge success.
  - Line 294: `stop()` releases audio resources, cleans up state.
- In `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Packets.java`:
  - Lines 155-186: `CoopInvoke(String action, String targetPlayer, String subject, String arg)` is defined.
  - Lines 188-258: `CoopCue` is defined with 11 parameters (`cueType`, `initiatorName`, `messageBilingual`, `x`, `y`, `z`, `durationTicks`, `ruText`, `phonetic`, `ptText`, `objective`).
- In `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\ChallengeManager.java`:
  - Lines 1039-1042: Server receives `CoopInvoke` packet:
    ```java
    public static void onCoopInvoke(Packets.CoopInvoke pkt, class_3222 player) {
        if (pkt == null || player == null) return;
        startPhraseInvocation(player, pkt.action());
    }
    ```
  - Lines 1195-1324: `startPhraseInvocation` immediately broadcasts `CoopCue` to nearby teammates and instantiates the `ActiveTask`.
  - Currently, `CoopInvoke` is NOT sent anywhere from the client yet!

### 1.4 Iris Shader Reload Key 'R' Unbinding
- In `F:\Jogos\Mine\Instancia_Cliente\options.txt` (line 132) and `F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\options.txt` (line 132):
  - `key_iris.keybind.reload:key.keyboard.r`
  - In Minecraft options files, an unassigned key is represented as `key.keyboard.unknown`.

---

## 2. Logic Chain

### 2.1 Implementing the Radial Wheel UI (`RadialMenuScreen`)
1. **Registration**:
   - `SayItClient` needs a new KeyBinding: `radialKey = ClientCompat.key("key.sayitenglish.radial", 86);` (default 'V', GLFW_KEY_V = 86).
   - In `SayItClient.tick()`, when `radialKey.method_1436()` (`wasPressed()`) fires and no screen is open (`mc.field_1755 == null`), open `RadialMenuScreen`.
2. **Radial Menu Structure**:
   - `RadialMenuScreen` extends `class_437` (`Screen`).
   - `method_25421()` (`shouldPause()`) returns `false` to prevent game pause during multiplayer.
   - Contains 6 cooperative sectors:
     - Sector 0 (Top, -90°): `"wood"` (`Мне нужно дерево!`) -> Icon `minecraft:oak_log`
     - Sector 1 (Top-Right, -30°): `"food"` (`Мне нужна еда!`) -> Icon `minecraft:bread`
     - Sector 2 (Bottom-Right, 30°): `"help"` (`Помоги мне!`) -> Icon `minecraft:golden_apple`
     - Sector 3 (Bottom, 90°): `"lava"` (`Пол - это лава!`) -> Icon `minecraft:lava_bucket`
     - Sector 4 (Bottom-Left, 150°): `"follow"` (`Следуй за мной!`) -> Icon `minecraft:compass`
     - Sector 5 (Top-Left, -150°): `"waypoint"` (`Смотри туда!`) -> Icon `minecraft:spyglass`
3. **Angular Calculation**:
   - Center `cx = width / 2; cy = height / 2;`.
   - Mouse delta: `dx = mouseX - cx; dy = mouseY - cy; dist = Math.sqrt(dx*dx + dy*dy);`.
   - Deadzone: If `dist < 25.0 || dist > 140.0`, `hoveredSector = -1`.
   - Angle: `mouseAngle = Math.atan2(dy, dx);`.
   - Sector index: `rel = mouseAngle - (-Math.PI/2.0 - Math.PI/6.0); while(rel < 0) rel += 2*Math.PI; while(rel >= 2*Math.PI) rel -= 2*Math.PI; int sector = (int)(rel / (Math.PI / 3.0));`.
4. **Dual Interaction Modes (Tap vs Hold)**:
   - Tap 'V': User taps 'V', radial menu stays open. User moves mouse to sector, clicks left mouse button (`method_25402` / `mouseClicked`) -> sector selected. ESC (`key == 256` in `keyPressed`) closes menu.
   - Hold 'V': User holds 'V', moves mouse to sector, releases 'V' (`method_16803` / `keyReleased` where `key == 86`) -> sector selected and screen closed.

### 2.2 Strict Voice Challenge Gating Protocol
1. **Sector Selection**:
   - When a sector is selected in `RadialMenuScreen`, the client **NEVER** sends `CoopInvoke` immediately.
   - `RadialMenuScreen` closes: `ClientCompat.openScreen(this.field_22787, null);`.
   - Calls `ChallengeSession.startCoopChallenge(sector.action(), sector.ruText(), sector.ptText(), sector.icon(), 15, Config.get().passScore);`.
2. **ChallengeSession State**:
   - `ChallengeSession.pendingCoopAction = action;` is recorded.
   - A client-side `Packets.Challenge` is initialized with a distinct client challenge ID (`-100 - action.hashCode() % 1000`), `kind = "sentence"`.
   - `prompt` is created directly with the exact target Cyrillic phrase, Portuguese hint, and item icon.
   - Alternatives and phonetic variants are loaded for the Vosk speech recognizer.
   - Microphone listening starts (`tryListen()`).
3. **Enforcement in `finish()`**:
   - In `ChallengeSession.finish()`:
     - `boolean ok = best >= c.passScore();`
     - `String coopAction = pendingCoopAction; pendingCoopAction = null;`
     - If `coopAction != null`:
       - If `ok == true` (speech verified):
         `ClientPlatform.I.sendToServer(new Packets.CoopInvoke(coopAction, ""));`
         Dispatches the action to server. Partner receives `CoopCue`.
       - If `ok == false` (pronunciation below threshold or timer expired):
         **Strictly blocked!** No packet is sent. Partner receives nothing.
4. **Enforcement in `stop()`**:
   - If player hits ESC, leaves world, or cancels: `ChallengeSession.stop()` sets `pendingCoopAction = null;` without sending any packet.

### 2.3 Visual Notification on Receiver (`CoopCue` HUD)
1. In `SayItClient.init()`, register:
   `c.receivePacket(Packets.CoopCue.TYPE, ChallengeHud::receiveCoopCue);`
2. In `ChallengeHud`:
   - Store `activeCoopNotification`.
   - In `ChallengeHud.render(...)`, when `activeCoopNotification != null`:
     - Render sender's player head: `Items.PLAYER_HEAD` (`class_1802.field_8575`) or player skin avatar.
     - Render representative action item icon (oak log, bread, golden apple, compass, spyglass, lava bucket).
     - Render a prominent yellow question mark `'?'` (`"§e§l?"`) on the item icon.
     - Display bilingual sender text and objective banner.
     - Auto-dismiss after duration expires.

---

## 3. Caveats
1. **Iris Shaders Key Unbinding**:
   Unbinding line 132 in `options.txt` (`key_iris.keybind.reload:key.keyboard.unknown`) applies when Minecraft loads `options.txt`. If an existing instance re-saves options while Iris is active, it might rewrite the key if Iris forces a default. Therefore, defense-in-depth is required: both update `options.txt` and dynamically clear the keybinding in `SayItClient.init()` if present in the game options.
2. **Keyboard Fallback Key ('Y')**:
   When a cooperative voice challenge is active, `TypeAnswerScreen` (key 'Y') also works for accessibility (as mandated by F15/T4.6). If answered correctly via keyboard typing, `Scorer.score` yields 100%, which also validates speech requirement and allows `CoopInvoke` dispatch. This maintains consistency with the modpack's accessibility architecture.
3. **No Unexplored Areas**:
   All 4 areas requested in the dispatch have been investigated with direct code verification and bytecode inspection.

---

## 4. Conclusion & Architectural Specification

### 4.1 Classes to Add
| Class | Package | Description |
|---|---|---|
| `RadialMenuScreen.java` | `com.spicesfire.sayitenglish.client` | 6-sector interactive radial wheel screen (`Screen`) with item icons, Cyrillic text, Portuguese hints, mouse hover detection, and dual-mode (hold/click) selection. |
| `SayItClient.java` | `com.spicesfire.sayitenglish.client` | Moved to `src/` so modifications compile into mod JAR: registers keybinding 'V', registers `Packets.CoopCue` packet handler, updates client tick. |

### 4.2 Classes to Modify
| Class | Package | Modifications |
|---|---|---|
| `ChallengeSession.java` | `com.spicesfire.sayitenglish.client` | Add `pendingCoopAction` field, add `startCoopChallenge(...)` method, modify `finish()` and `stop()` to strictly gate `CoopInvoke` packet dispatch. |
| `ChallengeHud.java` | `com.spicesfire.sayitenglish.client` | Add `receiveCoopCue(...)`, store active cooperative notification, implement `renderCoopNotification(...)` displaying player head + action icon with yellow question mark `'?'`. |

### 4.3 Files to Edit (Configuration & Tests)
| File | Changes |
|---|---|
| `Instancia_Cliente\options.txt` | Set `key_iris.keybind.reload:key.keyboard.unknown` (line 132). |
| `Launcher\data\instances\Minecraft_Russo\.minecraft\options.txt` | Set `key_iris.keybind.reload:key.keyboard.unknown` (line 132). |
| `tests\run_e2e_tests.ps1` | Add Tier 1 tests for Radial Wheel UI, Voice Challenge Gating verification, Partner Cue visual HUD, and Iris 'R' unbinding. |

---

## 5. Verification Method

To independently verify this survey and its subsequent implementation:
1. **Build and Deployment**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\build_and_deploy.ps1"
   ```
   *Expected result*: Exit code 0, all classes compiled and injected into both `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`.
2. **E2E Test Suite Execution**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
   ```
   *Expected result*: Exit code 0, 100% tests passed.
3. **Keybinding and Config Verification**:
   Inspect `Instancia_Cliente\options.txt` line 132:
   ```powershell
   Get-Content "F:\Jogos\Mine\Instancia_Cliente\options.txt" | Select-String "key_iris.keybind.reload"
   ```
   *Expected output*: `key_iris.keybind.reload:key.keyboard.unknown`.
4. **Invalidation Conditions**:
   - If `CoopInvoke` is sent upon radial sector click before voice challenge completion -> Invalidation.
   - If pressing 'V' opens a screen that pauses singleplayer/LAN ticks (`shouldPause() == true`) -> Invalidation.
   - If partner notification does not show the player head and action icon with `'?'` -> Invalidation.
