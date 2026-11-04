# Changes Summary: Requirement R1 (Multiplayer Cooperative Mechanics & Phrase Invocation)

## Overview
Implemented complete multiplayer cooperative gameplay mechanics for Requirement R1 of the SayIt mod (Minecraft 1.20.1 Fabric) strictly across the three owned files:
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Packets.java`
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Progress.java`
- `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\ChallengeManager.java`

All Cyrillic and Portuguese strings are encoded using 100% pure 7-bit ASCII source with native `\uXXXX` escapes, preventing encoding anomalies on Windows 11.

---

## 1. Network Packets (`Packets.java`)
Created `Packets.java` retaining exact binary serialization for all 6 existing mod packets and registering 3 new custom cooperative packets:

1. **`Packets.CoopInvoke`**:
   - Direction: Client -> Server
   - Identifiers: Channel `sayitenglish:coop_invoke` via `Packets.type`
   - Fields: `action` (String), `targetPlayer` (String), `subject` (String), `arg` (String)
   - Interface Contracts: Dual constructors for `(action, arg)` and `(action, targetPlayer, subject, arg)` with accessors `actionType()` and `targetPlayerId()`.

2. **`Packets.CoopCue`**:
   - Direction: Server -> Client
   - Identifiers: Channel `sayitenglish:coop_cue` via `Packets.type`
   - Fields: `cueType` (String), `initiatorName` (String), `messageBilingual` (String), `x` (double), `y` (double), `z` (double), `durationTicks` (int), `ruText` (String), `phonetic` (String), `ptText` (String), `objective` (String)
   - Interface Contracts: Dual constructors for `(cueType, initiatorName, messageBilingual, x, y, z, durationTicks)` and `(sender, action, ruText, phonetic, ptText, objective, x, y, z, seconds)` with accessors `sender()`, `action()`, `seconds()`.

3. **`Packets.CoopSync`**:
   - Direction: Server -> Client
   - Identifiers: Channel `sayitenglish:coop_sync` via `Packets.type`
   - Fields: `score` (int), `completedCount` (int), `rankTitle` (String), `activeChallengeName` (String), `timeRemaining` (int), `rankTier` (int)
   - Interface Contracts: Dual constructors for `(score, completedCount, rankTitle, activeChallengeName, timeRemaining)` and `(score, rankTier, rankTitle, completed)`.

---

## 2. Persistent Cooperative Progress & Hierarchy (`Progress.java`)
Extended `Progress.java` with persistent tracking and 5-tier pedagogical ranking:

1. **Persistent JSON Fields**:
   - `public int coopScore = 0;` (Cumulative cooperative score)
   - `public int coopCompletedChallenges = 0;` (Total completed cooperative events)
   - `public int coopCompleted = 0;` (Synchronized alias for contract flexibility)
   - `public int coopPhrases = 0;` (Total phrase invocations completed)
   - `public int coopDialogues = 0;` (Total 2-turn dialogues completed)
   - `public long lastCoopTimestamp = 0L;` (Epoch millisecond timestamp of last action)

2. **5-Tier Pedagogical Rank Hierarchy**:
   - **Tier 1 (0 – 99 pts)**: `§7[Новичок]` (Novice)
   - **Tier 2 (100 – 299 pts)**: `§a[Товарищ]` (Comrade / Companion)
   - **Tier 3 (300 – 599 pts)**: `§b[Следопыт]` (Pathfinder / Scout)
   - **Tier 4 (600 – 999 pts)**: `§d[Мастер Команды]` (Team Master)
   - **Tier 5 (1000+ pts)**: `§6[Легенда]` (Legend)

3. **Methods Added**:
   - `coopRankTier()` and `getCoopRank()`: Returns 1..5
   - `coopRankTitle()`: Returns formatted title with color badge
   - `coopRankNameRaw()`: Returns raw Cyrillic title string
   - `nextRankTitle()`, `nextRankThreshold()`, `pointsToNextRank()`: Computes remaining points needed
   - `addCoopScore(int points)` and `incrementCoopCompleted()`: Mutates state and updates timestamp
   - Deserialization normalization in `load()` ensuring `coopCompletedChallenges` and `coopCompleted` remain aligned.

---

## 3. Server Cooperative Manager (`ChallengeManager.java`)
Implemented `ChallengeManager.CoopManager` providing full lifecycle orchestration:

1. **Practical Phrase Invocations**:
   - `follow` ("Следуй за мной!" / [Sleduy za mnoy] / "Siga-me!"):
     Initiator gains `GLOWING` status effect for 10s; teammates receive bilingual action bar and chat instructions to stay within 6 blocks. If stayed together, +15 Coop Score awarded to both.
   - `waypoint` ("Смотри туда!" / [Smotri tuda] / "Olhe para lá!"):
     Calculates 16m forward waypoint vector, renders continuous `END_ROD` particle beacon beam; visiting waypoint within 15s awards +15 Coop Score.
   - `request_wood` ("Мне нужно дерево!" / [Mnye nuzhno dyerevo] / "Preciso de madeira!"):
     Detects logs/wood/planks in nearby teammate's inventory; approaching within 6 blocks awards +20 Coop Score.
   - `request_food` ("Мне нужна еда!" / [Mnye nuzhna yeda] / "Preciso de comida!"):
     Detects food items in nearby teammate's inventory; approaching within 6 blocks awards +20 Coop Score.
   - `help` ("Помоги мне!" / [Pomogi mnye] / "Ajude-me!"):
     Teammates rushing within 5 blocks complete rescue; grants `REGENERATION` buff and +20 Coop Score to both.

2. **Dynamic / Kamikaze Mini-Challenges**:
   - **"The Floor is Lava" (`Пол — это лава!`)**:
     Freely startable by any player via `/sayit coop lava`, `/sayit challenge lava`, or speech/chat invocation.
     Phase 1 (Warning): 3s audible bell alarm + countdown titles.
     Phase 2 (Active Trial): 12s active trial checking player elevation and ground contact. Players touching baseline ground receive flame particles and fire ticks; elevated players remain safe. Surviving players receive +25 Coop Score, Speed II buff, and level-up fanfare.
   - **"Movement / Parkour" (`Прыгай выше!`)**: 15s movement trial awarding +20 Coop Score and Jump Boost.
   - **"Rally / Group" (`Держимся вместе!`)**: 10s convergence trial awarding +20 Coop Score and Regeneration.

3. **Random 2-Turn Cooperative Dialogues**:
   - Every 180 seconds (3600 ticks), scans for idle nearby players within 24m.
   - 5 structured bilingual dialogue scenarios (Greeting, Mining, Battle, Resources, Danger).
   - Turn 1 prompts Player A to speak prompt; upon pass, broadcasts chat line and chimes Player B.
   - Turn 2 prompts Player B to reply; upon pass, broadcasts reply, awards +30 Coop Score, 15 XP, and Cyrillic letter heat boost to both.

4. **Leaderboard & Command Interface**:
   - `/sayit coop rank`: Active online player leaderboard sorted descending by score.
   - `/sayit coop stats`: Detailed individual stats with current tier, next tier threshold, and points remaining.
   - `/sayit coop lava`: Instant start for "The Floor is Lava".
   - `/sayit coop parkour`: Instant start for parkour challenge.
   - `/sayit coop rally`: Instant start for rally challenge.
   - `/sayit coop dialogue`: Manually triggers 2-turn dialogue between nearby players.
   - `/sayit coop invoke <action>`: Triggers phrase actions.
   - `/sayit coop help`: Command index and syntax guide.

5. **Lifecycle Wiring**:
   - Hooked `CoopManager.init()` in `ChallengeManager.init()`.
   - Hooked `CoopManager.tick(server, now)` in `ChallengeManager.tick()`.
   - Hooked `CoopManager.syncCoop(player)` in `ChallengeManager.sync()`.
   - Hooked `CoopManager.handleChallengeResult(player, c, score, heard, passed)` in `ChallengeManager.finish()`.
