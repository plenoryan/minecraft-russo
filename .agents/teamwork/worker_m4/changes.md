# Summary of Changes — Worker M4: Build, Dual Deployment & Canonical Verification

**Milestone**: M4 (Requirement R5: Build Pipeline, Dual Deployment, and E2E Test Suite)  
**Agent**: `worker_m4`  
**Date**: 2026-11-04  

---

## 1. Created Artifact: `F:\Jogos\Mine\build_and_deploy.ps1`
- **Location**: `F:\Jogos\Mine\build_and_deploy.ps1`
- **Purpose**: Authoritative compilation and dual-deployment pipeline for SayIt Minecraft 1.20.1 Fabric mod.
- **Key Operations**:
  1. Validates presence of OpenJDK 17 toolchain (`javac.exe`, `jar.exe`, `java.exe`), source directory `src/`, base client mod JAR, `client-intermediary.jar`, `Jade-1.20-Fabric-11.13.3.jar`, server libraries (`Servidor_Dedicado\libraries` excluding `sponge-mixin`), and client libraries (`Launcher\data\libraries`).
  2. Compiles all 8 Java source files under `src/` (`Alphabet.java`, `ChallengeManager.java`, `Packets.java`, `Progress.java`, `client\ChallengeHud.java`, `client\ChallengeSession.java`, `client\DictionaryScreen.java`, `client\SayItJadePlugin.java`) with `javac.exe -encoding UTF-8` targeting a temporary staging classes directory.
  3. Pre-normalizes the base JAR from upstream Loom bundle (resolving the Gradle `META-INF/MANIFEST.MF` entry size mismatch) using `jar.exe cfm` with manifest preserved.
  4. Injects all 23 freshly compiled `.class` files into the mod JAR using OpenJDK 17 `jar.exe uf <jar> -C <classesDir> com`.
  5. Verifies internal table of contents using `jar.exe tf` for all 10 required milestone classes (`Alphabet.class`, `ChallengeManager.class`, `ChallengeManager$CoopManager.class`, `Packets.class`, `Packets$CoopInvoke.class`, `Packets$CoopCue.class`, `Packets$CoopSync.class`, `Progress.class`, `ChallengeHud.class`, `ChallengeSession.class`).
  6. Deploys the freshly built JAR (30,782,039 bytes) to both:
     - `F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
     - `F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar`
  7. Audits post-deployment timestamps, non-zero file sizes (>10 MB), and existence.

---

## 2. Modified Artifact: `F:\Jogos\Mine\tests\run_e2e_tests.ps1`
- **Location**: `F:\Jogos\Mine\tests\run_e2e_tests.ps1` (Line 740, Test `T1_F29`)
- **Bug Fixed**: In Windows PowerShell 5.1, raw UTF-8 Cyrillic literals `"пол"` and `"лава"` in a BOM-less `.ps1` file are parsed using Windows-1252 default encoding, causing `$normLava -notmatch "пол"` to evaluate as true and throw `"Floor is lava Cyrillic phrase failed normalization"`.
- **Change Applied**: Replaced raw string literals with pre-existing Base64 decoder:
  ```powershell
  if ($normLava -notmatch (b64u "0L/QvtC7") -or $normLava -notmatch (b64u "0LvQsNCy0LA=")) {
      throw "Floor is lava Cyrillic phrase failed normalization"
  }
  ```
- **Conformity**: Restores strict compliance with the file's stated rule: `# Supported: Windows 11 PowerShell 5.1 / 7+ (100% Pure 7-bit ASCII Source)`.

---

## 3. Updated Mod Artifacts: Client & Server Mod JARs
- **Client Mod JAR**: `F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar` (Size: 30,782,039 bytes, LastWriteTime: updated to build time).
- **Server Mod JAR**: `F:\Jogos\Mine\Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar` (Size: 30,782,039 bytes, LastWriteTime: updated to build time).
- **Contents**: Verified to contain all updated bytecodes from Milestones M1 and M3.

---

## 4. Verification Execution
1. `build_and_deploy.ps1` executed cleanly with exit code 0.
2. Canonical E2E test suite `run_e2e_tests.ps1 -Strict` executed: 55/55 tests PASSED (100%), 0 defects, exit code 0.
3. Dedicated server headless dry-run smoke test executed: loaded 89 mods including `sayitenglish 1.1.0`, 0 class resolution crashes, 0 mixin collisions, exited cleanly on `--help` with exit code 0.
