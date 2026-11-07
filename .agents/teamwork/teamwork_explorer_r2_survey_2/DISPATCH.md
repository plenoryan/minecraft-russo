# DISPATCH — Explorer 2 (Survey: Receiver Visual Cue HUD & Iris Key Unbind)

**Objective**: Survey receiver visual notification HUD rendering (player head + item icon with '?') and Iris shader reload key unbinding.

**Authoritative Requirements**: `F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md` (read lines 48-89 carefully).

**Scope of Investigation**:
1. Receiver Visual Notification (R2):
   - How `Packets.CoopCue` and `ChallengeHud.java` currently render cues/notifications on the client receiving player's screen.
   - How to render the requesting player's head/skin/icon (e.g. `PlayerFaceRenderer` / `drawTexture` from player skin or game profile) alongside an evocative icon with a question mark '?' (e.g., wood + '?', food + '?', help + '?').
   - How the packet data flow delivers the initiator's identity/UUID/name and the action type to the receiver.
2. Iris Shader Key Unbinding (R3):
   - Locate how Iris keybindings and Minecraft options are stored in `Instancia_Cliente` (e.g., `options.txt`, iris config files, or Fabric mod keybinding initialization).
   - Identify how `key.iris.reloadShaders` is assigned and how to unbind it (e.g., setting key code to `key.keyboard.unknown` or unbinding in options.txt / programmatically) to completely eliminate accidental shader reloads on 'R'.
   - Verify how client launch and option persistence behave.

**Output**: Write a comprehensive, verified handoff report to `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_2\handoff.md`.

## 2026-11-07T05:54:49Z
Investigate:
1. Receiver Visual Notification (R2):
   - Inspect Packets.CoopCue and ChallengeHud.java in src/com/spicesfire/sayitenglish/.
   - How to render the requesting player's head/skin/icon alongside an evocative action icon with a question mark '?' (e.g. wood + '?', food + '?', help + '?') on the partner's screen.
   - Packet data flow: how initiator identity (UUID/name) and action type are transferred to the client.
2. Iris Shader Key Unbinding (R3):
   - Locate options.txt and Iris shader keybindings in Instancia_Cliente.
   - How key.iris.reloadShaders is configured and how to unbind it (e.g. key.keyboard.unknown or programmatic unbinding) to completely eliminate accidental shader reloads on 'R'.
