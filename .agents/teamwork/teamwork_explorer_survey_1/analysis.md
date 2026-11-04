# Technical Analysis & Implementation Architecture: Multiplayer Cooperative Mechanics & Phrase Invocation (Requirement R1)

## Executive Summary
This document provides a comprehensive technical exploration of the **SayIt** codebase (`sayitenglish-fabric-1.20.1-1.1.0.jar`) at `F:\Jogos\Mine` and establishes an end-to-end implementation architecture for **Requirement R1 (Multiplayer Cooperative Mechanics & Phrase Invocation)**.

The SayIt mod currently operates under a single-player paradigm where challenges (`word`, `sentence`, `practice`) are dispatched strictly to individual players based on inventory scans and player-specific stat triggers. To fulfill R1, we design an event-driven cooperative layer that leverages Fabric 1.20.1 networking, builds upon the existing Vosk/speech recognition engine, and introduces bidirectional multiplayer interaction:
1. **Practical phrase invocations** (request item, point to location/waypoint, follow me) verified by voice STT.
2. **Dynamic/kamikaze mini-challenges** ("The floor is lava", sprint/parkour, group rally) freely triggerable by any player.
3. **Random cooperative dialogues** (2-part conversational turns between nearby teammates).
4. **Active cooperative scoring & ranking system** (persistent score, 5 pedagogical rank tiers, live leaderboard).
5. **Comprehensible visual/audio cues** (bilingual HUD notifications, in-world particle beacons, glow effects, audio chimes).

---

## 1. Existing Codebase Architecture & Networking Exploration

### 1.1 Architecture & Boundaries
The SayIt codebase follows a clean client/server separation orchestrated by the `Platform` abstraction layer:
- **Mod ID**: `sayitenglish`
- **Root Package**: `com.spicesfire.sayitenglish`
- **Common / Server Entry Point**: `FabricEntrypoint.java` (`onInitialize`) -> invokes `SayIt.records()` and `SayIt.init()`.
- **Client Entry Point**: `FabricClientEntrypoint.java` (`onInitializeClient`) -> invokes `SayItClient.init()`.
- **Platform Implementations**:
  - `Platform.I` -> `com.spicesfire.sayitenglish.platform.fabric.FabricPlatform` (Server-side tick, connection lifecycle, commands, networking).
  - `ClientPlatform.I` -> `com.spicesfire.sayitenglish.platform.fabric.FabricClientPlatform` (Client-side tick, HUD render layers, keybindings, client networking).
- **Authoritative Server State**:
  - `ChallengeManager.java` manages per-player `State` and persistent `Progress` saved to `saves/<World>/sayitenglish/<UUID>.json`.
  - All progression updates, mastery stars, forgetting curve decay, item loss penalties, and Heracles quest completions occur authoritatively on the server.
- **Client Presentation & Audio Engine**:
  - `ChallengeSession.java`: Controls active challenge lifecycle, microphone recording, audio engine feeds, and sending `Result` packets.
  - `ChallengeHud.java`: Implements HUD rendering via Fabric's `HudRenderCallback.EVENT`.
  - `DictionaryScreen.java`: In-game GUI for pocket dictionary.
  - `SayItJadePlugin.java`: Integrates with Jade for real-time bilingual crosshair inspection.

### 1.2 Fabric 1.20.1 Networking Architecture
Networking in SayIt uses Fabric's `ServerPlayNetworking` and `ClientPlayNetworking`:
- **Packet Interface**: `com.spicesfire.sayitenglish.platform.ModPacket` with methods `void write(class_2540 b)` (`PacketByteBuf`) and `class_2960 channel()`.
- **Packet Type Definition**: `com.spicesfire.sayitenglish.platform.ModPacketType<T>` constructed via `Packets.type(String name, BiConsumer<class_2540, T> writer, Function<class_2540, T> reader)`.
- **Packet IDs (Channels)**: Namespaced as `sayitenglish:<name>`.
- **Existing Registered Packets**:
  | Packet Name | Direction | Payload Structure | Existing Purpose |
  |-------------|-----------|-------------------|------------------|
  | `sync` | S -> C | `(String language, List<String> words, List<Integer> scores, int sentences, int lost)` | Synchronizes player progress to client on join/update |
  | `choose_language` | C -> S | `(String code)` | Player selects language (e.g. "ru") |
  | `ask_language` | S -> C | `(int unused)` | Prompts client to display language selection screen |
  | `practice` | C -> S | `(int unused)` | Requests a practice challenge from server |
  | `challenge` | S -> C | `(int id, String kind, String subject, String arg, int seconds, int passScore)` | Server starts a challenge on client |
  | `result` | C -> S | `(int id, int score, String heard)` | Client returns speech score & transcript to server |

### 1.3 Event Triggers & Tick Lifecycle
In `ChallengeManager.java`:
- `init()`:
  - `Platform.I.onServerTickEnd`: Runs `ChallengeManager.tick` every 5 ticks.
  - `Platform.I.onJoin`: Initializes player `State`, grants Starter Dictionary if needed, triggers `sync`.
  - `Platform.I.onLeave`: Flushes player `Progress` to disk.
  - `Platform.I.onBlockBroken`: Probabilistically queues a mining event `(mine, item:<id>)`.
- `tick(MinecraftServer server)`:
  - Iterates connected players (`server.method_3760().method_14571()`).
  - Calls `scanInventory(player, s, now)` to check for newly picked items, applying spaced repetition and Ebbinghaus forgetting curve decay (`checkDecay`).
  - Calls `watch(player, s, now)` to detect crafts, mob kills, damage, dimension shifts, swimming, fire, riding, sprinting/flying.

---

## 2. Speech Recognition & Phrase Processing Pipeline

### 2.1 Acoustic Model & Vosk Integration
SayIt bundles an offline Vosk speech recognition pipeline:
- **Audio Capture**: `Microphone.java` captures 16,000 Hz, 16-bit signed PCM mono audio using Java `TargetDataLine`.
- **Speech Engine Interface**: `SpeechEngine.java` handles `prepare`, `begin(Request)`, `feed(byte[] pcm, int length)`, and `end()`.
- **Vosk Implementation**: `VoskEngine.java`:
  - Loads offline acoustic model from `models/vosk/vosk-model-small-ru-0.22`.
  - Dual recognizer setup:
    1. `VoskRaw free`: Unconstrained vocabulary recognizer for broad speech detection.
    2. `VoskRaw restricted`: Constrained grammar recognizer loaded with JSON grammar containing the target phrase, recognized alternatives, and `[unk]`.
  - Real-time partial results are streamed to `ChallengeSession.offer(Guess, target)`.

### 2.2 Pronunciation Scoring Engine
Pronunciation evaluation is performed in `Scorer.java`:
- Text is normalized via Unicode NFD, diacritic stripping, case-folding.
- Sentence similarity combines normalized Levenshtein edit distance and phonetic sound hashing (`sound(w)`).
- Confidence from Vosk (`withConfidence`) scales the match score (0-100%).
- For sentences (`targetWords.size() > 1`), a stricter pass threshold (`Math.max(current.passScore(), 85)`) is enforced to ensure complete phrase delivery before passing.

---

## 3. Detailed Technical Design for Requirement R1

### 3.1 Subsystem 1: Practical Phrase Invocations
Practical phrases allow players to coordinate survival tasks verbally in Russian while teammates receive clear Portuguese instructions and in-game cues.

#### A. Phrase Inventory & Pedagogical Mapping
| ID | Russian Cyrillic (Spoken by Initiator) | Phonetic Transcription | Portuguese Meaning (Shown to Teammates) | Gameplay Action / Objective |
|---|---|---|---|---|
| `coop.follow` | `Следуй за мной!` | `[Sleduy za mnoy]` | "Siga-me!" | Initiator glows; teammate must stay within 6 blocks for 8 seconds. |
| `coop.waypoint` | `Смотри туда!` | `[Smotri tuda]` | "Olhe para lá!" | Emits a glowing beacon/particle pillar at crosshair (up to 48m); teammate visits waypoint. |
| `coop.request_wood` | `Мне нужно дерево!` | `[Mnye nuzhno dyerevo]` | "Preciso de madeira!" | Teammate drops or hands wood/oak log to initiator within 20s. |
| `coop.request_food` | `Мне нужна еда!` | `[Mnye nuzhna yeda]` | "Preciso de comida!" | Teammate drops or hands bread/apple/food to initiator within 20s. |
| `coop.help` | `Помоги мне!` | `[Pomogi mnye]` | "Ajude-me!" | Teammate arrives within 5 blocks of initiator; heals both slightly. |

#### B. Invocation Flow
```
[Player A presses Coop Key 'K' or types /sayit coop invoke <action>]
                                  │
                                  ▼
[Client displays Phrase Speech Prompt on Player A's HUD: "Следуй за мной!"]
                                  │
                                  ▼
[Player A speaks Russian phrase into microphone -> Vosk evaluates >= 75%]
                                  │
                                  ▼
[Client sends Packets.CoopInvoke(action, arg) to Server]
                                  │
                                  ▼
[Server validates initiator, registers ActiveCoopChallenge in CoopManager]
                                  │
                                  ▼
[Server broadcasts Packets.CoopCue to all nearby players within 48 blocks]
                                  │
                                  ▼
[Player B's Client receives CoopCue -> plays chime, shows bilingual HUD card & waypoint]
                                  │
                                  ▼
[Player B completes objective -> Server detects completion -> Awards +15 Coop Score to both!]
```

---

### 3.2 Subsystem 2: Dynamic & Kamikaze Mini-Challenges
Dynamic, adrenaline-inducing mini-challenges can be triggered freely by any player at any time, encouraging spontaneous group coordination.

#### A. Challenge Roster
1. **"The Floor is Lava" (`Пол — это лава!`)**:
   - **Invocation**: Player speaks `Пол — это лава!` or triggers via `/sayit challenge lava`.
   - **Phase 1 (Warning - 3 seconds)**:
     - Global sound: Horn / alarm (`class_3417.field_14605` bell ring).
     - HUD banner: `§c⚠ ПОЛ — ЭТО ЛАВА ЧЕРЕЗ 3... 2... 1!`
   - **Phase 2 (Active Trial - 12 seconds)**:
     - Server tick checks every player's standing block:
       - Solid ground at feet (`level.getBlockState(pos.down())` on grass, dirt, stone at baseline): FAIL / Burn particles (`ParticleTypes.FLAME`).
       - Elevated block (standing on placed cobblestone, bed, leaves, fences, floating in water, hanging on ladder): SAFE!
   - **Phase 3 (Resolution)**:
     - All surviving players receive `+25 Coop Score` and speed buff.
     - Audio: Level-up chime (`class_3417.field_14725`).

2. **"Movement / Parkour Challenge" (`Прыгай!` / `Беги!`)**:
   - **Invocation**: Player speaks `Прыгай выше!` (Jump higher!) or `Беги быстрее!` (Run faster!).
   - **Trial (15 seconds)**:
     - All participating players must accumulate 8 sprint jumps or sprint 40 meters.
     - Tracked via player velocity and jump/movement events.
     - Surviving teammates receive `+20 Coop Score` and Jump Boost / Swiftness for 30s.

3. **"Rally / Group Hug" (`Держимся вместе!`)**:
   - **Invocation**: Player speaks `Держимся вместе!` (Stick together!).
   - **Trial (10 seconds)**:
     - All players within 64 blocks must converge within 4 blocks of the initiator.
     - Reward: `+20 Coop Score` and Regeneration buff.

---

### 3.3 Subsystem 3: Random Cooperative Dialogues
To stimulate natural conversational pacing in multiplayer, the server periodically initiates a 2-turn dialogue between nearby players.

#### A. Trigger Conditions
- In `ChallengeManager.tick`:
- Every 180 seconds (configurable cooldown), the server scans for pairs of online players within 20 blocks of each other.
- Neither player must have an active challenge.

#### B. 2-Part Turn-Based Sequence
```
[Server selects Player A (Speaker 1) and Player B (Speaker 2)]
                                  │
                                  ▼
[Turn 1: Server sends CoopDialogue(part=1, "sayitenglish.dialogue.greet.1a") to Player A]
         Target: "Привет, как дела?" [Privyet, kak dyela?]
         Translation: "Olá, como vai?"
                                  │
                                  ▼
[Player A speaks phrase -> Vosk verifies >= 75%]
                                  │
                                  ▼
[Chat broadcasts: "§6[Diálogo] Player A: §eПривет, как дела? §7[Privyet, kak dyela?]"
 Audio chime plays for Player B]
                                  │
                                  ▼
[Turn 2: Server sends CoopDialogue(part=2, "sayitenglish.dialogue.greet.1b") to Player B (15s window)]
         Target: "Все отлично, спасибо!" [Vsyo otlichno, spasibo!]
         Translation: "Tudo ótimo, obrigado!"
                                  │
                                  ▼
[Player B speaks phrase -> Vosk verifies >= 75%]
                                  │
                                  ▼
[Chat broadcasts: "§6[Diálogo] Player B: §eВсе отлично, спасибо! §7[Vsyo otlichno, spasibo!]"]
[Both players receive +30 Coop Points, 15 XP, and Cyrillic letter heat boost!]
```

#### C. Dialogue Repertory
| Theme | Part 1 (Speaker A) | Part 2 (Speaker B) | Meaning |
|---|---|---|---|
| Greeting | `Привет, как дела?` | `Все отлично, спасибо!` | "Olá, como vai?" -> "Tudo ótimo, obrigado!" |
| Exploration | `Куда мы идем?` | `Мы идем в шахту!` | "Onde estamos indo?" -> "Vamos para a mina!" |
| Battle Readiness | `Ты готов к бою?` | `Да, я готов!` | "Você está pronto para a luta?" -> "Sim, estou pronto!" |
| Resource Check | `У тебя есть факелы?` | `Да, у меня есть факелы!` | "Você tem tochas?" -> "Sim, eu tenho tochas!" |
| Hazard Alert | `Осторожно, крипер!` | `Я вижу его!` | "Cuidado, um creeper!" -> "Eu estou vendo ele!" |

---

### 3.4 Subsystem 4: Active Cooperative Scoring and Ranking System

#### A. Persistent Data Schema (`Progress.java`)
Add new fields to `Progress.java` (serialized via Gson into `saves/<World>/sayitenglish/<UUID>.json`):
```java
public int coopScore = 0;              // Cumulative cooperative score
public int coopCompleted = 0;          // Total cooperative events completed
public int coopPhrases = 0;            // Total phrase invocations completed
public int coopDialogues = 0;          // Total 2-turn dialogues completed
public long lastCoopTimestamp = 0L;    // Last coop action timestamp
```

#### B. Pedagogical Rank Hierarchy
Players advance through 5 distinct cooperative rank tiers as their score increases:
| Tier | Russian Rank Title | Portuguese Rank Name | Score Threshold | Color / Badge |
|:---:|---|---|:---:|:---:|
| 1 | **Новичок** | Novato Cooperativo | 0 – 49 pts | `§7[⚪ Новичок]` |
| 2 | **Товарищ** | Companheiro | 50 – 149 pts | `§a[🟢 Товарищ]` |
| 3 | **Следопыт** | Desbravador / Rastreador | 150 – 299 pts | `§b[🔵 Следопыт]` |
| 4 | **Мастер Команды** | Mestre de Equipe | 300 – 599 pts | `§d[🟣 Мастер Команды]` |
| 5 | **Легенда** | Lenda Cooperativa | 600+ pts | `§6[🟡 Легенда]` |

#### C. Rank Progression Formula
```java
public int coopRankTier() {
    if (this.coopScore >= 600) return 5;
    if (this.coopScore >= 300) return 4;
    if (this.coopScore >= 150) return 3;
    if (this.coopScore >= 50) return 2;
    return 1;
}

public String coopRankTitle() {
    switch (coopRankTier()) {
        case 5: return "\u00a76\u041b\u0435\u0433\u0435\u043d\u0434\u0430";          // Легенда
        case 4: return "\u00a7d\u041c\u0430\u0441\u0442\u0435\u0440 \u041a\u043e\u043c\u0430\u043d\u0434\u044b"; // Мастер Команды
        case 3: return "\u00a7b\u0421\u043b\u0435\u0434\u043e\u043f\u044b\u0442";    // Следопыт
        case 2: return "\u00a7a\u0422\u043e\u0432\u0430\u0440\u0438\u0449";          // Товарищ
        default: return "\u00a77\u041d\u043e\u0432\u0438\u0447\u043e\u043a";          // Новичок
    }
}
```

#### D. Leaderboard & Command Interface
- Command: `/sayit coop rank`:
  - Formats and displays real-time ranking of all players connected to the server.
- Command: `/sayit coop stats`:
  - Displays individual cooperative statistics, current rank badge, and distance to next rank.
- Pocket Dictionary GUI (`DictionaryScreen.java`):
  - Displays top banner with current cooperative score, rank, and completed events.

---

### 3.5 Subsystem 5: Comprehensible Visual & Audio Cues for the Receiving Player
To guarantee that the receiving player understands the action while learning Russian:

#### A. HUD / Action Bar Notification Card
When a cooperative event is broadcast to Player B:
- **Location**: Top-center HUD card (width 260px, rounded semi-transparent background `#D0101018`, golden border).
- **Header**: `§6★ COOP: <SenderName> §7• <RemainingTime>s`
- **Spoken Russian Line**: `§e<RussianPhrase> §7[<Phonetic>]` (e.g. `§eСледуй за мной! §7[Sleduy za mnoy]`)
- **Portuguese Meaning**: `§fTradução: §b"<Português>"` (e.g. `§fTradução: §b"Siga-me!"`)
- **Action Objective**: `§aObjetivo: §f<O que fazer>` (e.g. `§aObjetivo: §fPermaneça próximo por 8s`)
- **Progress Bar**: Smooth countdown timer bar transitioning from green to red.

#### B. In-World Spatial Indicators
1. **Waypoints ("Смотри туда!")**:
   - Server spawns a vertical particle beam at the designated crosshair position:
     `world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 30, 0.2, 1.5, 0.2, 0.05);`
   - Client renders an on-screen directional marker or distance tag: `§e[Метка: 32m]`.
2. **Follow Me ("Следуй за мной!")**:
   - Initiator player receives `StatusEffects.GLOWING` effect for 10 seconds, allowing Player B to easily locate them through terrain.
3. **The Floor is Lava ("Пол — это лава!")**:
   - Floating emergency banner at center of screen: `§c🔥 CHÃO É LAVA! SUBA EM UM BLOCO! 🔥`.
   - Red screen vignette or flame particles at foot level if touching unsafe ground.

#### C. Distinct Audio Cues
- **Incoming Coop Request**: `class_3417.field_14622` (XP orb chime, pitch 1.4f).
- **Emergency Challenge Alert**: `class_3417.field_14605` (bell ring, pitch 1.0f).
- **Challenge / Action Complete**: `class_3417.field_14725` (player level-up fanfare) + firework spark sound.
- **Trial Failed / Timeout**: `class_3417.field_14624` (villager failure, pitch 0.8f).

---

## 4. Code Specifications, Codecs, and Interface Contracts

### 4.1 Network Packets Specification (`Packets.java`)

```java
// 1. Client -> Server: Initiator requests or invokes a cooperative action
public record CoopInvoke(String action, String arg) implements ModPacket {
    public static final ModPacketType<CoopInvoke> TYPE = Packets.type(
        "coop_invoke",
        (b, p) -> p.write((class_2540)b),
        CoopInvoke::read
    );

    @Override
    public void write(class_2540 b) {
        b.method_10814(this.action); // writeString
        b.method_10814(this.arg);
    }

    static CoopInvoke read(class_2540 b) {
        return new CoopInvoke(b.method_19772(), b.method_19772()); // readString
    }

    @Override
    public class_2960 channel() {
        return TYPE.channel();
    }
}

// 2. Server -> Client: Broadcasts cue, translation, objective, coordinates, and duration
public record CoopCue(
    String sender,
    String action,
    String ruText,
    String phonetic,
    String ptText,
    String objective,
    double x,
    double y,
    double z,
    int seconds
) implements ModPacket {
    public static final ModPacketType<CoopCue> TYPE = Packets.type(
        "coop_cue",
        (b, p) -> p.write((class_2540)b),
        CoopCue::read
    );

    @Override
    public void write(class_2540 b) {
        b.method_10814(this.sender);
        b.method_10814(this.action);
        b.method_10814(this.ruText);
        b.method_10814(this.phonetic);
        b.method_10814(this.ptText);
        b.method_10814(this.objective);
        b.writeDouble(this.x);
        b.writeDouble(this.y);
        b.writeDouble(this.z);
        b.method_10804(this.seconds); // writeVarInt
    }

    static CoopCue read(class_2540 b) {
        return new CoopCue(
            b.method_19772(),
            b.method_19772(),
            b.method_19772(),
            b.method_19772(),
            b.method_19772(),
            b.method_19772(),
            b.readDouble(),
            b.readDouble(),
            b.readDouble(),
            b.method_10816()
        );
    }

    @Override
    public class_2960 channel() {
        return TYPE.channel();
    }
}

// 3. Server -> Client: Synchronizes cooperative score and rank
public record CoopSync(
    int score,
    int rankTier,
    String rankTitle,
    int completed
) implements ModPacket {
    public static final ModPacketType<CoopSync> TYPE = Packets.type(
        "coop_sync",
        (b, p) -> p.write((class_2540)b),
        CoopSync::read
    );

    @Override
    public void write(class_2540 b) {
        b.method_10804(this.score);
        b.method_10804(this.rankTier);
        b.method_10814(this.rankTitle);
        b.method_10804(this.completed);
    }

    static CoopSync read(class_2540 b) {
        return new CoopSync(
            b.method_10816(),
            b.method_10816(),
            b.method_19772(),
            b.method_10816()
        );
    }

    @Override
    public class_2960 channel() {
        return TYPE.channel();
    }
}
```

### 4.2 Server Cooperative Manager Architecture (`CoopManager` in `ChallengeManager.java`)
In `ChallengeManager.java`:
```java
public static final class CoopManager {
    public record ActiveTask(
        String id,
        String action,
        UUID initiatorUuid,
        Set<UUID> targetUuids,
        double targetX,
        double targetY,
        double targetZ,
        long startTick,
        long deadlineTick,
        int durationSeconds,
        Map<String, Object> metadata
    ) {}

    private static final Map<String, ActiveTask> ACTIVE_TASKS = new HashMap<>();
    private static long lastDialogueTick = 0L;

    public static void tick(MinecraftServer server, long now) {
        // 1. Tick active tasks (check distance, check item transfers, check floor is lava)
        Iterator<Map.Entry<String, ActiveTask>> it = ACTIVE_TASKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ActiveTask> entry = it.next();
            ActiveTask task = entry.getValue();
            if (now > task.deadlineTick()) {
                it.remove();
                handleTaskTimeout(server, task);
                continue;
            }
            if (evaluateTaskSuccess(server, task, now)) {
                it.remove();
                handleTaskSuccess(server, task);
            }
        }

        // 2. Periodic check for random cooperative dialogues
        if (now - lastDialogueTick >= 3600L) { // 180 seconds (3 min)
            lastDialogueTick = now;
            tryTriggerRandomDialogue(server, now);
        }
    }
}
```

### 4.3 Client Session & HUD Hooks (`ChallengeSession.java`, `ChallengeHud.java`)
- In `ChallengeSession.java`:
  ```java
  public static volatile Packets.CoopCue activeCue = null;
  public static volatile long cueExpiresAt = 0L;
  public static int coopScore = 0;
  public static int coopRankTier = 1;
  public static String coopRankTitle = "Новичок";
  
  public static void onCoopCue(Packets.CoopCue cue) {
      activeCue = cue;
      cueExpiresAt = System.currentTimeMillis() + (cue.seconds() * 1000L);
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
          mc.field_1724.method_5783(class_3417.field_14622.comp_349(), 0.8f, 1.4f);
      }
  }
  ```
- In `ChallengeHud.java`:
  ```java
  if (ChallengeSession.activeCue != null) {
      long remMs = ChallengeSession.cueExpiresAt - System.currentTimeMillis();
      if (remMs <= 0) {
          ChallengeSession.activeCue = null;
      } else {
          ChallengeHud.renderCoopCue(g, mc, font, ChallengeSession.activeCue, remMs);
      }
  }
  ```

---

## 5. Build, Packaging, and Verification Protocol

### 5.1 Compilation Command (Strictly Tested & Verified)
To compile all updated `.java` files with pure ASCII and native `\uXXXX` escaping:
```powershell
$jars = @(
    "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar",
    "F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar",
    "F:\Jogos\Mine\Instancia_Cliente\mods\Jade-1.20-Fabric-11.13.3.jar"
)
$libJars = Get-ChildItem -Recurse -Path F:\Jogos\Mine\Servidor_Dedicado\libraries -Filter "*.jar" | Where-Object { $_.Name -notmatch "sponge-mixin" } | Select-Object -ExpandProperty FullName
$clientLibJars = Get-ChildItem -Recurse -Path F:\Jogos\Mine\Launcher\data\libraries -Filter "*.jar" | Select-Object -ExpandProperty FullName
$cp = ($jars + $libJars + $clientLibJars) -join ";"

$srcFiles = Get-ChildItem -Recurse -Path F:\Jogos\Mine\src -Filter "*.java" | Select-Object -ExpandProperty FullName

& F:\Jogos\Mine\Java17\bin\javac.exe -cp "$cp" -d F:\Jogos\Mine\src $srcFiles
if ($LASTEXITCODE -ne 0) { throw "Compilation failed with code $LASTEXITCODE" }
```

### 5.2 Jar Repack & Synchronization Command
```powershell
# Update JAR with compiled bytecode
Set-Location F:\Jogos\Mine\src
& F:\Jogos\Mine\Java17\bin\jar.exe uf F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar com

# Synchronize identical binary to dedicated server
Copy-Item F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar -Force

# Return to root
Set-Location F:\Jogos\Mine
```

### 5.3 Automated E2E Test Suite Extension Plan
In `tests/run_e2e_tests.ps1`:
- Register new tests in Tier 1 and Tier 3:
  - `T1_F24: Multiplayer Practical Phrase Invocation Packet Handlers & Codecs`.
  - `T1_F25: Dynamic Mini-Challenge "Floor is Lava" State Evaluator`.
  - `T1_F26: Cooperative 2-Turn Dialogue Trigger and Turn Orchestrator`.
  - `T1_F27: Cooperative Scoring, Persistent Storage & 5-Tier Rank Hierarchy`.
  - `T1_F28: Teammate Bilingual Cue Rendering & Audio Alert Dispatch`.
- Test execution command:
  ```powershell
  powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
  ```

---

## 6. Implementation Risk & Mitigation Analysis

| Risk | Impact | Mitigation Strategy |
|---|---|---|
| Latency / Packet Loss during Voice Invocation | Teammates do not receive prompt in time | 1,500ms server grace buffer already established in SayIt; actions store explicit world timestamps rather than local system clocks. |
| Chat Spam from Multiple Rapid Invocations | Visual clutter, scrolling chat | Route repetitive countdowns and progress updates strictly to Action Bar (`player.method_7353(component, true)`), leaving Chat only for canonical milestone announcements. |
| Missing Teammates (Single-Player Mode) | Solo players blocked from coop phrases | Coop phrases gracefully fallback to self-practice or provide friendly chat notification if no other players are connected. |
| Character Encoding Corruptions (Mojibake) | Corrupted Cyrillic or Portuguese accents | Strict pure-ASCII Java source rule using native Unicode escapes (`\u041f\u043e\u043b...` / `\u00a76`), zero UTF-8 BOM in configs. |
