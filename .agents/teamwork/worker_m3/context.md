# Worker M3 Context: Multiplayer Cooperative Mechanics & Phrase Invocations

## Assigned Scope
Implement Requirement R1 (Multiplayer Cooperative Mechanics & Phrase Invocation):
1. Add custom network packets (`CoopInvoke`, `CoopCue`, `CoopSync`) in `Packets.java`.
2. Extend `Progress.java` with persistent cooperative score, completed challenges, and a 5-tier pedagogical rank hierarchy (`Новичок`, `Товарищ`, `Следопыт`, `Мастер Команды`, `Легенда`).
3. Implement `CoopManager` in `ChallengeManager.java` handling:
   - Practical phrase invocations (item request, waypoint beacon, follow me).
   - Dynamic / kamikaze challenges ("The floor is lava", parkour/movement challenges) freely startable by any player.
   - Random 2-turn cooperative dialogues between nearby players.
   - Leaderboard & ranking commands (`/sayit coop rank`, `/sayit coop stats`).
   - Bilingual visual/audio cues for teammate learning and comprehension.

## Input Files
- Original Request: F:\Jogos\Mine\.agents\teamwork\ORIGINAL_REQUEST.md
- Master Project: F:\Jogos\Mine\.agents\teamwork\orchestrator\PROJECT.md
- Survey Analysis 1: F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_1\analysis.md
- Survey Handoff 1: F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_1\handoff.md

## Owned Files (Strict Exclusive Ownership)
- F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Packets.java
- F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Progress.java
- F:\Jogos\Mine\src\com\spicesfire\sayitenglish\ChallengeManager.java
