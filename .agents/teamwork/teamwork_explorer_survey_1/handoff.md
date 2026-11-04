# Handoff Report: Explorer Survey R1 (Multiplayer Cooperative Mechanics & Phrase Invocation)

## 1. Observation
1. **SayIt JAR & Class Layout**:
   - Jar Path: `F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar` (Length: 30,752,654 bytes) and identical copy at `F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`.
   - Classes present in `src`: `Alphabet.java` (14,841 bytes), `ChallengeManager.java` (39,637 bytes), `Progress.java` (5,191 bytes), `client/ChallengeHud.java` (13,256 bytes), `client/ChallengeSession.java` (9,446 bytes), `client/DictionaryScreen.java` (6,491 bytes), `client/SayItJadePlugin.java` (2,703 bytes).
   - Decompiled mod classes in JAR: `Packets.class`, `SayIt.class`, `Scorer.class`, `client/SayItClient.class`, `client/VoskEngine.class`, `client/Words.class`, `platform/fabric/FabricPlatform.class`, `platform/fabric/FabricClientPlatform.class`.
2. **Current Challenge Architecture & Single-Player Isolation**:
   - In `src/com/spicesfire/sayitenglish/ChallengeManager.java:128-151`:
     `ChallengeManager.tick` iterates online players and initiates challenges via `ChallengeManager.start(player, s, now, kind, id, "", seconds)`.
     Lines 333-338:
     ```java
     private static void start(class_3222 player, State s, long now, String kind, String subject, String arg, int seconds) {
         Packets.Challenge c;
         s.active = c = new Packets.Challenge(s.nextId++, kind, subject, arg, seconds, Config.get().passScore);
         s.deadline = now + ((long)seconds + 20L) * 20L;
         Platform.I.sendToPlayer(player, (ModPacket)c);
     }
     ```
     Challenges are dispatched strictly to the triggering player; no cooperative broadcast or multiplayer objective synchronization currently exists.
3. **Existing Fabric 1.20.1 Networking Channels**:
   - In `com.spicesfire.sayitenglish.Packets`:
     All packets implement `ModPacket` and are bound through `Packets.type(name, writer, reader)` using `class_2540` (`PacketByteBuf`) and `ServerPlayNetworking` / `ClientPlayNetworking`.
     Existing channels: `sayitenglish:sync`, `sayitenglish:choose_language`, `sayitenglish:ask_language`, `sayitenglish:practice`, `sayitenglish:challenge`, `sayitenglish:result`.
4. **Vosk Speech Recognition Pipeline**:
   - In `client/ChallengeSession.java:101-125` and `client/VoskEngine.java`:
     Audio is fed from `Microphone.java` (16kHz PCM) into `VoskRaw free` (unconstrained) and `VoskRaw restricted` (grammar with target phrase and alternatives). Real-time guesses are scored in `Scorer.java` and returned via `Packets.Result(id, score, heard)`.
5. **Exact Compilation Mechanism Verified**:
   - Command executed:
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
     ```
   - Result: Exit code `0`, clean compilation of all classes in `src`.

## 2. Logic Chain
1. **From Observation 2 & 3**: Because `Packets.java` and `FabricPlatform` provide an extensible record-based networking system on top of Fabric 1.20.1 (`ServerPlayNetworking`), we can introduce three custom packets (`CoopInvoke`, `CoopCue`, and `CoopSync`) without modifying low-level Fabric networking.
2. **From Observation 4**: Because `VoskEngine` and `ChallengeSession` already support full phrase recognition via constrained grammar and `Scorer.java` evaluates sentence accuracy, practical phrase invocations ("Следуй за мной", "Смотри туда", "Мне нужно дерево") can use the existing audio capture and pronunciation validation pipeline.
3. **From Observation 2 & 5**: By implementing `CoopManager` within `ChallengeManager.java` on the server, the server can orchestrate multi-player challenges (such as "The Floor is Lava" or 2-turn dialogues) with authoritative verification of player movement, elevation, item exchange, and proximity.
4. **From Observation 1 & 5**: Changes can be compiled with `Java17\bin\javac.exe` against `client-intermediary.jar` and injected directly into both `Instancia_Cliente` and `Servidor_Dedicado` jar files using `jar.exe uf`.

## 3. Caveats
- No caveats regarding compiler compatibility or networking: the build chain and intermediary classes were empirically verified with exit code 0.
- Assumed standard client proximity of <= 48 blocks for spatial audio/particle cues; players across different dimensions (e.g. Overworld vs Nether) will receive notification text but spatial beacon particles will only render if within the same dimension.
- Voice invocation requires the initiator to have a functional microphone (or use the existing 'Y' keyboard typing fallback configured in SayIt).

## 4. Conclusion
Requirement R1 can be seamlessly implemented by:
1. Adding 3 network packets (`CoopInvoke`, `CoopCue`, `CoopSync`) in `Packets.java`.
2. Extending `Progress.java` with persistent cooperative score, completed challenges, and a 5-tier pedagogical rank hierarchy (`Новичок` -> `Товарищ` -> `Следопыт` -> `Мастер Команды` -> `Легенда`).
3. Implementing `CoopManager` in `ChallengeManager.java` to handle:
   - Practical phrase invocations (item request, waypoint beacon, follow me).
   - Dynamic/kamikaze challenges ("The floor is lava", parkour/movement, group rally).
   - Random 2-turn cooperative dialogues between nearby players.
   - Leaderboard command (`/sayit coop rank`) and stats command (`/sayit coop stats`).
4. Extending `ChallengeHud.java` and `ChallengeSession.java` to display bilingual notification cards, waypoint markers, glowing effects, and auditory cues.
All design details, codecs, algorithms, and interface contracts are documented in `F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_1\analysis.md`.

## 5. Verification Method
1. **Compilation Verification**:
   Execute the PowerShell build snippet documented in Section 5.1 of `analysis.md` using `F:\Jogos\Mine\Java17\bin\javac.exe`. Expected result: exit code `0`.
2. **E2E Test Suite Run**:
   Run the canonical test suite:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
   ```
   Expected result: All tests pass with exit code `0`.
3. **Dedicated Server Dry-Run Smoke Test**:
   ```powershell
   & "F:\Jogos\Mine\Java17\bin\java.exe" -Dfabric.dli.main=net.fabricmc.loader.impl.launch.server.FabricServerLauncher -jar "F:\Jogos\Mine\Servidor_Dedicado\fabric-server-launch.jar" --help
   ```
   Expected result: Clean execution without `Incompatible mods found` or `Mod resolution failed`.
