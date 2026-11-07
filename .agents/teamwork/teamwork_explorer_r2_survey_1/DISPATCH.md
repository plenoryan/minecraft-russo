# DISPATCH — Explorer 1 (Survey: Radial Wheel UI & Voice Challenge Gating)

**Objective**: Survey the client UI, input/keybindings, radial wheel architecture, and Russian voice challenge integration in SayIt (Minecraft 1.20.1 Fabric).

**Authoritative Requirements**: `F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md` (read lines 48-89 carefully).

**Scope of Investigation**:
1. How keybindings are registered and handled in Fabric 1.20.1 in the SayIt mod (check `src/com/spicesfire/sayitenglish/`).
2. Radial wheel UI architecture in Minecraft 1.20.1 / Java: How to implement a radial wheel screen (with configurable default key 'V', sectors with representative icons for cooperative actions: request wood, request food, request help, follow me, waypoint/point, lava challenge).
3. Speech challenge gating: How does `ChallengeSession.java`, `ChallengeHud.java`, `Packets.java`, and speech recognition flow work? How can selecting a sector on the radial wheel immediately trigger a Russian speech challenge for that specific phrase, and ensure that the cooperative packet (`CoopInvoke`) is strictly blocked if speech recognition fails or is cancelled, and ONLY sent to the server/partner upon successful speech validation?
4. Identify existing code files to modify or new files to add, and exact interface contracts.

**Output**: Write a comprehensive, verified handoff report to `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_1\handoff.md`.

## 2026-11-07T05:54:44Z
You are survey_exp_1, working as an Explorer on the SayIt mod (Minecraft 1.20.1 Fabric).
Working directory: F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_1
Authoritative Requirements: F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md
Your Dispatch Task: F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_1\DISPATCH.md

Investigate:
1. Client UI, keybindings, input handling in Fabric 1.20.1 SayIt mod (check src/com/spicesfire/sayitenglish/).
2. How to implement a radial wheel screen (default key 'V', sectors with representative icons for cooperative actions: wood, food, help, follow, waypoint, lava challenge).
3. Speech challenge gating: How ChallengeSession, ChallengeHud, Packets, and speech recognition integrate so that selecting a sector starts a Russian speech challenge, and the cooperative action packet (CoopInvoke) is strictly sent ONLY if speech is validated successfully (blocked on fail/cancel).
4. Code architecture: files to edit, classes to add, interface contracts.

Read the codebase thoroughly. Write your verified findings and recommendations to F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_r2_survey_1\handoff.md and report back via send_message.
