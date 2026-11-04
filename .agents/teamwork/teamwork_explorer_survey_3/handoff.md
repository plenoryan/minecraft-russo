# Handoff Report: Explorer Survey 3 (Requirements R4 & R5)

**Task**: Survey SayIt environment at `F:\Jogos\Mine` for Requirement R4 (QoL & Dynamism mods) and Requirement R5 (Build, Deployment & E2E Testing).  
**Handoff Type**: Hard (Task Complete)  
**Author**: teamwork_explorer_survey_3  
**Target Recipient**: Orchestrator (`dd52151e-9fc1-4341-8ae9-2b8120a8bfb1`)  

---

## 1. Observation

1. **Existing Installed Mods**:
   - `Instancia_Cliente\mods\`: Contains 27 JARs, including client rendering/immersion mods (`AmbientSounds`, `CreativeCore`, `CustomSkinLoader`, `ForgeConfigAPIPort`, `ImmediatelyFast`, `Jade`, `ModernUI`, `PresenceFootsteps`, `e4all`, `heraclesbutton`, `iris`, `kubejs`, `language-reload`, `modernfix`, `rhino`, `sodium`) and universal content/logic mods (`FarmersDelight`, `Heracles`, `architectury`, `fabric-api`, `ferritecore`, `geckolib`, `handcrafted`, `lithium`, `naturalist`, `resourcefullib`, `sayitenglish`).
   - `Servidor_Dedicado\mods\`: Contains strictly 10 universal JARs (`FarmersDelight`, `fabric-api`, `fabrictailor`, `ferritecore`, `geckolib`, `handcrafted`, `lithium`, `naturalist`, `resourcefullib`, `sayitenglish`).
   - `AGENTS.md` Lesson L13 explicitly states:
     > *"Mods de otimização de renderização gráfica e aceleração de fontes (como Sodium, Modern UI, ImmediatelyFast e CustomSkinLoader) contêm mixins exclusivos do ambiente cliente (`environment: client`). Incluí-los na pasta `mods` do servidor dedicado causa `ClassNotFoundException` e encerramento abrupto na inicialização."*

2. **Build System Reality**:
   - `Get-ChildItem -Path "F:\Jogos\Mine" -Filter "*gradle*" -Recurse`: returned 0 results. No `build.gradle`, `settings.gradle`, or Gradle wrapper exists.
   - Mod sources exist at `F:\Jogos\Mine\src\com\spicesfire\sayitenglish\` (7 Java files: `Alphabet.java`, `ChallengeManager.java`, `Progress.java`, `client/ChallengeHud.java`, `client/ChallengeSession.java`, `client/DictionaryScreen.java`, `client/SayItJadePlugin.java`).
   - Recompilation test using bundled `Java17\bin\javac.exe`:
     ```powershell
     & "F:\Jogos\Mine\Java17\bin\javac.exe" -encoding UTF-8 -cp "$cp" -d "$outDir" $srcFiles
     ```
     With classpath including:
     - `Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar`
     - `Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
     - `Instancia_Cliente\mods\Jade-1.20-Fabric-11.13.3.jar`
     - `Instancia_Cliente\mods\fabric-api-0.92.12+1.20.1.jar`
     - Libraries from `Launcher\data\libraries` and `Servidor_Dedicado\libraries`
     Produced output:
     > `"SUCCESS! 100% Clean Compilation! Total compiled classes: 11"` (Exit code 0).

3. **Candidate QoL & Dynamism Mods (Modrinth API v2 Verified)**:
   - `omnimovement` (Omnimovement 1.0, Fabric 1.20.1): `https://cdn.modrinth.com/data/ZmKZ0vTP/versions/zDpV6cM8/omnimovement-fabric-1.0%2Bmc1.20.1.jar` (29,222 B). Omnidirectional sprinting and auto step-up. Zero item/block additions.
   - `inventory-sorting` (Inventory Sorting 1.9.0-1.20 by kyrptonaught): `https://cdn.modrinth.com/data/5ibSyLAz/versions/tr2ZGtz4/InventorySorter-1.9.0-1.20.jar` (222,894 B). Middle-click sort, zero dependencies. Zero item/block additions.
   - `stack-to-nearby-chests` (Stack to Nearby Chests 0.5.10 by xiaocihua1337): `https://cdn.modrinth.com/data/HtGckJVc/versions/wMJlsfCI/stack-to-nearby-chests-mc1.20.1-0.5.10.jar` (559,703 B). Quick stack / restock. Zero item/block additions. Client-side only.
   - `entityculling` (Entity Culling 1.11.3): `https://cdn.modrinth.com/data/NNAgCjsB/versions/xSrd3qsM/entityculling-fabric-1.11.3-mc1.20.1.jar` (1,516,696 B). Occlusion culling for entity rendering boost.
   - `krypton` (Krypton 0.2.3): `https://cdn.modrinth.com/data/fQEb0iXm/versions/jiDwS0W1/krypton-0.2.3.jar` (185,895 B). Netty network stack optimization for multiplayer sessions.
   - `appleskin` (AppleSkin 2.5.2+mc1.20.1): `https://cdn.modrinth.com/data/EsAfCjCV/versions/N5XeV21r/appleskin-fabric-mc1.20.1-2.5.2.jar` (1,097,240 B). Hunger & saturation HUD tooltips.

4. **Existing E2E Test Suite**:
   - `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`: executed 45 tests, 45 passed, 0 failed, duration 3991 ms, exit code 0.

---

## 2. Logic Chain

1. **Premise**: Requirement R4 mandates adding QoL and dynamism mods (e.g. faster sprinting, inventory sorting, deposit to nearby chests, performance optimizations) that do not overload the language learning vocabulary, while R5 requires verified compilation, deployment, and comprehensive E2E testing.
2. **Analysis of R4**:
   - Mods that introduce custom items/blocks (e.g. backpacks, tech mods) will register new items into `Registries.ITEM`, polluting SayIt's acquisition listeners and causing uncurated English items to appear as voice challenges.
   - Therefore, candidate mods must be pure utility/QoL/performance mods that operate on vanilla mechanics or HUD overlays.
   - `Omnimovement` fulfills the "correr mais rápido" dynamism requirement via 360-degree sprinting and 1-block auto step-up.
   - `Inventory Sorting` (kyrptonaught) fulfills chest/inventory sorting with 0 external dependencies.
   - `Stack to Nearby Chests` fulfills deposit to nearby chests.
   - `Entity Culling` and `Krypton` provide essential rendering and network performance optimizations.
   - Per Lesson L13, client-only mods must remain segregated in `Instancia_Cliente\mods\` and must NOT be deployed to `Servidor_Dedicado\mods\`.
3. **Analysis of R5 Build Pipeline**:
   - The repository operates via direct OpenJDK 17 `javac` compilation rather than Gradle.
   - Using the verified classpath (remapped `client-intermediary.jar` + base `sayitenglish` JAR + runtime libraries), compiling `src/**/*.java` is fully automated, deterministic, and instantaneous (~1.5 seconds).
   - Packaging via `jar.exe uf` directly updates `sayitenglish-fabric-1.20.1-1.1.0.jar`.
   - Creating a dedicated script `build_and_deploy.ps1` establishes a clean, repeatable build and deployment pipeline to `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`.
4. **Analysis of R5 E2E Testing**:
   - The existing test suite in `tests\run_e2e_tests.ps1` has 45 tests covering milestones M1 to M5, but lacks dedicated assertions for the newly introduced requirements: R1 (coop phrases, kamikaze challenges, ranking), R2 (speech feedback suppression on single-word items and "disse: " header), R3 (slow gradual unlocking of non-identical Cyrillic letters), R4 (QoL mod files and server segregation), and R5 (clean compilation and dual JAR deployment).
   - Extending `run_e2e_tests.ps1` with 10 new test cases (`T1_F24` to `T1_F32` and `T4_07`) creates an authoritative 55-test suite that validates all acceptance criteria with code coverage across all project tiers.

---

## 3. Caveats

1. **Gradle Absolutes**: The original prompt mentioned Gradle (`build.gradle`, `settings.gradle`, `gradle.properties`, Loom). No Gradle project files exist in `F:\Jogos\Mine`. The team must proceed with the verified native `Java17\bin\javac.exe` + `jar.exe` toolchain rather than trying to construct an extraneous Gradle Loom setup that would disrupt the working offline development environment.
2. **Mod Download Connectivity**: The direct Modrinth CDN download URLs require active internet access during mod provisioning. Once downloaded, all JARs function 100% offline.
3. **Cloth Config Independence**: The selected candidate mods (`Omnimovement`, `Inventory Sorting`, `Stack to Nearby Chests`, `AppleSkin`, `Entity Culling`, `Krypton`) were specifically selected because none require `Cloth Config` or `Fabric Language Kotlin`, avoiding dependency chains that could destabilize Fabric Loader 0.16.14.

---

## 4. Conclusion

1. **For R4 (QoL Mods)**: Adopt the 6 identified Modrinth Fabric 1.20.1 mods:
   - Client (`Instancia_Cliente\mods\`): `omnimovement-fabric-1.0+mc1.20.1.jar`, `InventorySorter-1.9.0-1.20.jar`, `stack-to-nearby-chests-mc1.20.1-0.5.10.jar`, `entityculling-fabric-1.11.3-mc1.20.1.jar`, `krypton-0.2.3.jar`, `appleskin-fabric-mc1.20.1-2.5.2.jar`.
   - Server (`Servidor_Dedicado\mods\`): `InventorySorter-1.9.0-1.20.jar`, `krypton-0.2.3.jar`.
   - Provide an automated downloader script `download_qol_mods.ps1`.
2. **For R5 (Build & Deploy Pipeline)**: Implement `build_and_deploy.ps1` implementing the verified `javac` + `jar uf` pipeline, deploying atomically to `Instancia_Cliente\mods\` and `Servidor_Dedicado\mods\`.
3. **For R5 (E2E Test Extension)**: Extend `tests\run_e2e_tests.ps1` with 10 new test cases (`T1_F24` to `T1_F32` and `T4_07`), creating a canonical 55-test suite.

---

## 5. Verification Method

To independently verify the survey findings:

1. **Verify Existing E2E Test Suite**:
   ```powershell
   powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict
   ```
   *Expected*: 45 tests executed, 45 passed, exit code 0.

2. **Verify Java Compilation Pipeline**:
   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File "F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_3\test_compile.ps1"
   ```
   *Expected*: Output confirms `SUCCESS! 100% Clean Compilation! Total compiled classes: 11` with exit code 0.

3. **Verify Candidate Mod Endpoints & Metadata**:
   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File "F:\Jogos\Mine\.agents\teamwork\teamwork_explorer_survey_3\query_candidates.ps1"
   ```
   *Expected*: Returns HTTP 200 responses with exact file names, sizes, and direct Modrinth CDN download URLs.

4. **Invalidation Conditions**:
   - If `src/**/*.java` fails compilation when adding new cooperative multiplayer classes.
   - If dedicated server fails dry-run execution (`java -jar fabric-server-launch.jar --help`) after adding any new mod.
   - If any new mod registers unexpected item entries in `Registries.ITEM`.
