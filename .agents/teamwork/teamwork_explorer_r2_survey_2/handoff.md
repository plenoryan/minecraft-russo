# Handoff Report — Explorer 2: Receiver Visual Notification (R2) & Iris Shader Key Unbinding (R3)

**Author**: survey_exp_2  
**Date**: 2026-11-07T06:05:00Z  
**Target Project**: SayIt Mod (Minecraft 1.20.1 Fabric)  
**Working Directory**: `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_2`  

---

## 1. Observation

### 1.1 Receiver Visual Cue & Networking Architecture
1. **`Packets.CoopCue` (`src/com/spicesfire/sayitenglish/Packets.java:188-258`)**:
   - `Packets.CoopCue` is an existing record packet with 11 parameters:
     ```java
     public record CoopCue(
         String cueType,
         String initiatorName,
         String messageBilingual,
         double x,
         double y,
         double z,
         int durationTicks,
         String ruText,
         String phonetic,
         String ptText,
         String objective
     ) implements ModPacket
     ```
   - Wire serialization (`write`/`read`) at lines 224-252 preserves all fields.
   - Constructors available:
     - 11-param canonical constructor (`Packets.java:188`)
     - 7-param constructor (`Packets.java:203`): `(cueType, initiatorName, messageBilingual, x, y, z, durationTicks)`
     - 10-param constructor (`Packets.java:207`): `(sender, action, ruText, phonetic, ptText, objective, x, y, z, seconds)`
   - Wire layout is validated by `tests/AdversarialCoopNetworkingHarness.java:44-53, 218-356` across 9 distinct stress tests (including extreme coordinates, buffer underflow, and UTF-8 multi-line texts).

2. **Server-Side Dispatch (`src/com/spicesfire/sayitenglish/ChallengeManager.java:1088-1131`)**:
   - `ChallengeManager.sendBilingualCue(...)` constructs `Packets.CoopCue` using `initiator.method_5477().getString()` (the player's visible name, e.g. "AmigoRusso" or "Player123") as `initiatorName`, and the action string (e.g. "wood", "food", "help", "follow", "waypoint") as `action`.
   - Lines 1121-1125:
     ```java
     for (class_3222 target : targets) {
         if (target == null) continue;
         if (Platform.I.canSendTo(target, Packets.CoopCue.TYPE)) {
             Platform.I.sendToPlayer(target, (ModPacket)pkt);
         }
     ```
   - Target teammate also receives chat message (`chatMsg`), objective description (`objMsg`), actionbar subtitle (`actionMsg`), and an anvil chime sound (`class_3417.field_14622`).

3. **Client-Side Packet Reception Gap (`decompiled/com/spicesfire/sayitenglish/client/SayItClient.java:43-47`)**:
   - Currently, `SayItClient.init()` only registers clientbound packet listeners for:
     ```java
     c.receivePacket(Packets.Challenge.TYPE, ChallengeSession::start);
     c.receivePacket(Packets.Sync.TYPE, ChallengeSession::sync);
     c.receivePacket(Packets.AskLanguage.TYPE, p -> {
         ChallengeSession.askLanguage = true;
     });
     ```
   - `Packets.CoopCue.TYPE` is **not registered** on the client. As a result, the incoming `CoopCue` packet sent by the server is currently ignored on the client layer, relying only on vanilla chat and actionbar fallbacks.

4. **Minecraft 1.20.1 Client Head & Icon Rendering Toolchain**:
   - **Player Face Rendering**: `net.minecraft.class_7532` (`PlayerFaceRenderer` in Yarn). Method `method_44443(class_332 context, class_2960 skinTexture, int x, int y, int size)` renders the player's base head skin plus outer hat/hair layer.
   - **Skin Resolution**: From `MinecraftClient.getInstance().getNetworkHandler()` (`class_310.method_1551().method_1562()`), `class_634.method_2874(String name)` returns `PlayerListEntry` (`class_640`). Calling `class_640.method_2968()` returns the player's skin `Identifier` (`class_2960`).
   - **Default Skin Fallback**: If player entry is null (e.g. offline mode or entity not yet in client tracker), `net.minecraft.class_1068.method_4648(UUID)` (`DefaultSkinHelper.getSkin(UUID)`) yields the standard skin (Steve/Alex) derived from `UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8))`.
   - **Action Item Registry**: `net.minecraft.class_7923.field_41178` (`Registries.ITEM`) retrieves items via `new class_2960("minecraft:oak_log")`, `new class_2960("minecraft:bread")`, `new class_2960("minecraft:golden_apple")`, `new class_2960("minecraft:compass")`, `new class_2960("minecraft:recovery_compass")`, and `new class_2960("minecraft:lava_bucket")`.
   - **Item Icon & '?' Overlay**: `DrawContext.drawItem(ItemStack, int, int)` is available via `ClientCompat.item(g, stack, x, y)` (`class_332.method_51427`). Additionally, `class_332.method_51432(class_327 font, class_1799 stack, int x, int y, String countOverride)` (`DrawContext.drawItemInSlot`) natively overlays custom string text (e.g. `countOverride = "?"`) in the item slot with shadow.

---

### 1.2 Iris Shader Reload Key Unbinding
1. **Keybind Storage & Configuration File**:
   - Client options file: `F:\Jogos\Mine\Instancia_Cliente\options.txt`.
   - In `options.txt`, lines 132-135 verbatim:
     ```
     132: key_iris.keybind.reload:key.keyboard.r
     133: key_iris.keybind.toggleShaders:key.keyboard.k
     134: key_iris.keybind.shaderPackSelection:key.keyboard.o
     135: key_iris.keybind.wireframe:key.keyboard.unknown
     ```
   - NTFS Filesystem structure: `F:\Jogos\Mine\Instancia_Cliente\options.txt` and `F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\options.txt` are **NTFS HardLinks** (`LinkType: HardLink`) sharing the exact same file content on disk.

2. **Iris Internal KeyBinding Architecture (`iris-1.7.6+mc1.20.1.jar`)**:
   - `net.irisshaders.iris.Iris` declares:
     ```java
     private static KeyBinding reloadKeybind; // translation key "iris.keybind.reload", default 82 (GLFW_KEY_R)
     ```
   - In `net.irisshaders.iris.mixin.MixinMinecraft_Keybinds`, `Iris.handleKeybinds(MinecraftClient)` is invoked on every client tick:
     ```java
     if (reloadKeybind.wasPressed()) { // class_304.method_1436()
         reload();
     }
     ```
   - In Minecraft 1.20.1, unbound keybindings are represented as `key.keyboard.unknown` (key code `-1`, `InputUtil.UNKNOWN_KEY` / `class_3675.field_16237`).
   - When set to `key.keyboard.unknown`, `reloadKeybind.wasPressed()` is never triggered, eliminating all shader reload freezes on 'R'.

3. **Critical Encoding Pitfall (`AGENTS.md:211, 215`)**:
   - Minecraft 1.20.1 crashes or resets `options.txt` to vanilla defaults if `options.txt` contains a UTF-8 Byte Order Mark (`\uFEFF`).
   - `options.txt` must strictly be written with pure ASCII or UTF-8 without BOM (`New-Object System.Text.UTF8Encoding($false)`).

---

## 2. Logic Chain

1. **Step 1 — Zero Packet Breaking Change**:
   Because `Packets.CoopCue` already contains `initiatorName`, `cueType`, `ruText`, `phonetic`, `ptText`, `objective`, and `durationTicks`, no network protocol breakage or wire layout modification is needed. All 9 adversarial network roundtrip tests in `AdversarialCoopNetworkingHarness.java` will remain 100% green.

2. **Step 2 — Client Delivery Pipeline**:
   Registering `ClientPlatform.I.receivePacket(Packets.CoopCue.TYPE, ChallengeHud::onCoopCue)` inside `SayItClient.init()` delivers the server cue directly to the client render layer without touching the STT worker or sound engine.

3. **Step 3 — Partner Screen Head & Evocative Icon Rendering**:
   - Upon receiving `Packets.CoopCue`, `ChallengeHud` stores an active notification record `CoopCueNotification` containing the initiator name, evocative action, bilingual texts, timestamp, and duration.
   - During `ChallengeHud.render()`, if `activeCoopCue != null`, a prominent floating notification card is rendered at top-center (`(screenW - 240) / 2, y = 12`).
   - The card features:
     1. Player head skin (`24x24` px) via `PlayerFaceRenderer.method_44443` resolved from `ClientPlayNetworkHandler` or offline UUID fallback.
     2. Evocative action item (`16x16` px) mapped from action type:
        - `wood` / `request_wood` $\rightarrow$ `minecraft:oak_log`
        - `food` / `request_food` $\rightarrow$ `minecraft:bread`
        - `help` $\rightarrow$ `minecraft:golden_apple`
        - `follow` $\rightarrow$ `minecraft:compass`
        - `waypoint` $\rightarrow$ `minecraft:recovery_compass`
        - `lava` $\rightarrow$ `minecraft:lava_bucket`
     3. Evocative question mark `?` badge overlaid directly via `class_332.method_51432` (`DrawContext.drawItemInSlot`) and formatted header `[initiator] pediu [?]:`.
     4. Bilingual text (`ruText` in Russian and `ptText` in Portuguese) and an active duration countdown bar.

4. **Step 4 — Dual-Layer Unbinding for Iris 'R' Key**:
   - *Static Layer*: Modifying line 132 of `Instancia_Cliente\options.txt` from `key_iris.keybind.reload:key.keyboard.r` to `key_iris.keybind.reload:key.keyboard.unknown` unbinds the key immediately upon game launch.
   - *Runtime Programmatic Layer*: Adding an automated guard in `SayItClient.tick()` ensures that even if `options.txt` is regenerated or reset by the player in the Controls menu, `SayItClient` detects `iris.keybind.reload` and sets its bound key to `InputUtil.UNKNOWN_KEY`, updating key tables and saving the file.

---

## 3. Caveats

1. **Player Skins in Pure Offline LAN / P2P Sessions**:
   If the partner player is playing with an offline nickname and CustomSkinLoader has not yet cached a skin, `PlayerFaceRenderer` safely falls back to standard Steve/Alex skins via `DefaultSkinHelper.getSkin(UUID)`. There is zero chance of a null pointer exception or crash.
2. **Options.txt File Encoding**:
   When updating `options.txt` via script or automation, any UTF-8 BOM will cause Minecraft to reset `options.txt` to default vanilla settings. Writers must strictly use ASCII or UTF-8 Without BOM.
3. **Dual Translation Key Aliasing**:
   The Iris mod in this pack registers its translation key as `iris.keybind.reload` (options key `key_iris.keybind.reload`). For maximum defense against version discrepancies, programmatic unbinding checks both `iris.keybind.reload` and `key.iris.reloadShaders`.

---

## 4. Conclusion & Proposed Implementation Plan

### 4.1 Changes to `src/com/spicesfire/sayitenglish/client/ChallengeHud.java`
Add the notification state, packet handler, and renderer:

```java
// ==============================================================================
// R2: Receiver Visual Notification (CoopCue) with Initiator Head & Item + '?'
// ==============================================================================
public record CoopCueNotification(
    String initiatorName,
    String action,
    String ruText,
    String phonetic,
    String ptText,
    String objective,
    long receivedAt,
    long durationMs
) {}

public static volatile CoopCueNotification activeCoopCue = null;

public static void onCoopCue(Packets.CoopCue cue) {
    if (cue == null) return;
    long durationMs = cue.durationTicks() > 0 ? (long)cue.durationTicks() * 50L : 10000L;
    activeCoopCue = new CoopCueNotification(
        cue.initiatorName() != null ? cue.initiatorName() : "Aliado",
        cue.cueType() != null ? cue.cueType() : "help",
        cue.ruText() != null ? cue.ruText() : "",
        cue.phonetic() != null ? cue.phonetic() : "",
        cue.ptText() != null ? cue.ptText() : "",
        cue.objective() != null ? cue.objective() : "",
        System.currentTimeMillis(),
        durationMs
    );
}

// In ChallengeHud.render(class_332 g, float partialTick):
CoopCueNotification cue = activeCoopCue;
if (cue != null) {
    long age = System.currentTimeMillis() - cue.receivedAt();
    if (age > cue.durationMs()) {
        activeCoopCue = null;
    } else {
        renderCoopCueNotification(g, mc, font, cue, age);
    }
}

private static void renderCoopCueNotification(
    class_332 g,
    class_310 mc,
    class_327 font,
    CoopCueNotification cue,
    long age
) {
    int screenW = mc.method_22683().method_4486();
    int w = 240;
    int h = 38;
    int x = (screenW - w) / 2;
    int y = 12;

    // Dark panel background with gold accent borders
    ClientCompat.fill(g, x, y, x + w, y + h, -803726810);
    ClientCompat.fill(g, x, y, x + w, y + 2, -14006);
    ClientCompat.fill(g, x, y, x + 3, y + h, -14006);
    ClientCompat.fill(g, x + w - 3, y, x + w, y + h, -14006);

    // 1. Initiator player face
    class_2960 skin = null;
    if (mc.method_1562() != null && cue.initiatorName() != null) {
        class_640 entry = mc.method_1562().method_2874(cue.initiatorName());
        if (entry != null) {
            skin = entry.method_2968();
        }
    }
    if (skin == null) {
        java.util.UUID offlineUuid = java.util.UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + cue.initiatorName()).getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        skin = class_1068.method_4648(offlineUuid);
    }
    int headX = x + 6;
    int headY = y + 7;
    int headSize = 24;
    net.minecraft.class_7532.method_44443(g, skin, headX, headY, headSize);

    // 2. Evocative action item + '?' overlay
    class_1799 actionStack;
    String act = cue.action().toLowerCase();
    if (act.contains("wood")) {
        actionStack = new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:oak_log")));
    } else if (act.contains("food")) {
        actionStack = new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:bread")));
    } else if (act.contains("help")) {
        actionStack = new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:golden_apple")));
    } else if (act.contains("follow")) {
        actionStack = new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:compass")));
    } else if (act.contains("waypoint")) {
        actionStack = new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:recovery_compass")));
    } else if (act.contains("lava")) {
        actionStack = new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:lava_bucket")));
    } else {
        actionStack = new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:paper")));
    }

    int iconX = headX + headSize + 6; // x + 36
    int iconY = y + 11;
    ClientCompat.item(g, actionStack, iconX, iconY);
    g.method_51432(font, actionStack, iconX, iconY, "?");

    // 3. Bilingual request text
    int tx = iconX + 22;
    String title = "\u00a7e" + cue.initiatorName() + " \u00a7fpediu \u00a76[?]:";
    String sub = "\u00a76" + cue.ruText() + (!cue.ptText().isEmpty() ? " \u00a77(" + cue.ptText() + ")" : "");
    ClientCompat.text(g, font, title, tx, y + 6, -1);
    ClientCompat.text(g, font, sub, tx, y + 18, -1);

    // 4. Time remaining bar
    float frac = Math.max(0.0f, 1.0f - ((float)age / (float)cue.durationMs()));
    ClientCompat.fill(g, x + 3, y + h - 2, x + 3 + (int)((float)(w - 6) * frac), y + h, -14006);
}
```

### 4.2 Changes to `src/com/spicesfire/sayitenglish/client/SayItClient.java`
1. Register `Packets.CoopCue.TYPE`:
   ```java
   c.receivePacket(Packets.CoopCue.TYPE, ChallengeHud::onCoopCue);
   ```
2. Add programmatic unbinding for Iris reload key on first tick:
   ```java
   private static boolean irisReloadUnbound = false;

   // In tick():
   if (!irisReloadUnbound) {
       unbindIrisReloadKey();
       irisReloadUnbound = true;
   }

   private static void unbindIrisReloadKey() {
       class_310 mc = class_310.method_1551();
       if (mc.field_1690 == null || mc.field_1690.field_1839 == null) return;
       boolean changed = false;
       for (class_304 key : mc.field_1690.field_1839) {
           if (key == null) continue;
           String name = key.method_1431();
           if ("iris.keybind.reload".equals(name) || "key.iris.reloadShaders".equals(name)) {
               if (!key.method_1429().equals(class_3675.field_16237)) {
                   key.method_1422(class_3675.field_16237);
                   changed = true;
               }
           }
       }
       if (changed) {
           class_304.method_1424();
           mc.field_1690.method_1640();
       }
   }
   ```

### 4.3 Static Update to `Instancia_Cliente\options.txt`
Replace:
```
key_iris.keybind.reload:key.keyboard.r
```
With:
```
key_iris.keybind.reload:key.keyboard.unknown
```
Ensuring file writing is done with `[System.Text.Encoding]::ASCII` (Zero BOM).

---

## 5. Verification Method

### 5.1 Static Configuration Verification
Run in PowerShell:
```powershell
# 1. Verify key unbinding in options.txt
$opt = Get-Content -Raw "F:\Jogos\Mine\Instancia_Cliente\options.txt"
if ($opt -match "key_iris\.keybind\.reload:key\.keyboard\.unknown") {
    Write-Host "[OK] Iris reload key is unbound in options.txt" -ForegroundColor Green
} else {
    throw "Iris reload key is not set to unknown!"
}

# 2. Verify Zero-BOM compliance
$bytes = [System.IO.File]::ReadAllBytes("F:\Jogos\Mine\Instancia_Cliente\options.txt")
if ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    throw "BOM detected in options.txt! Must be removed."
} else {
    Write-Host "[OK] options.txt is clean (No BOM detected)" -ForegroundColor Green
}
```

### 5.2 Build & Deployment Pipeline Verification
Run:
```powershell
& "F:\Jogos\Mine\build_and_deploy.ps1"
```
*Expected Result*: Exit code `0`. Mod JAR recompiled and injected with updated `ChallengeHud.class` and `SayItClient.class`.

### 5.3 Automated E2E Test Suite Execution
Run:
```powershell
powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
```
*Expected Result*: 100% pass (Exit code `0`).

### 5.4 Invalidation Conditions
- Any occurrence of `key_iris.keybind.reload:key.keyboard.r` in `options.txt`.
- Missing `PlayerFaceRenderer.method_44443` or `class_332.method_51432` in `ChallengeHud.java`.
- Non-zero exit code or compile failure during `build_and_deploy.ps1`.
