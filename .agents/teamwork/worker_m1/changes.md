# Detailed Change Log: Milestone M1 (Speech HUD & Cyrillic Progression)

**Worker**: `worker_m1`  
**Date**: 2026-11-04  
**Target Root**: `F:\Jogos\Mine`  
**Files Modified**:
1. `src\com\spicesfire\sayitenglish\Alphabet.java`
2. `src\com\spicesfire\sayitenglish\client\ChallengeHud.java`
3. `src\com\spicesfire\sayitenglish\client\ChallengeSession.java`

---

## 1. Requirement R2: Speech Feedback HUD Adjustments

### 1.1 `src\com\spicesfire\sayitenglish\client\ChallengeSession.java`
- **Location**: Method `finish()` (lines 210-230).
- **Modification**:
  - Previously, `ChallengeHud.speechFeedback` was instantiated unconditionally upon completion of every challenge (whether single-word item challenges like `"word"`, dictionary practice `"practice"`, or sentence challenges `"sentence"`).
  - Implemented conditional gating:
    ```java
    boolean isSentence = (c != null && "sentence".equals(c.kind()))
        || (prompt != null && prompt.target() != null && Scorer.words(prompt.target()).size() > 1);

    if (isSentence) {
        String heardText = (bestHeard != null && !bestHeard.isEmpty()) ? bestHeard : (heard != null ? heard : "");
        ChallengeHud.speechFeedback = new ChallengeHud.SpeechFeedback(
            heardText,
            prompt != null ? prompt.nativeText() : "",
            prompt != null ? prompt.target() : "",
            best,
            ok,
            c != null ? c.kind() : "sentence",
            System.currentTimeMillis()
        );
    } else {
        ChallengeHud.speechFeedback = null;
    }
    ```
- **Rationale**: Eliminates visual clutter in the peripheral HUD when the player is performing repetitive item-gathering actions (e.g. collecting wood, stones, apples). Speech feedback is reserved for communicative and full sentences.

### 1.2 `src\com\spicesfire\sayitenglish\client\ChallengeHud.java`
- **Location**: Imports, `render()` method, and `renderSpeechFeedback()` method.
- **Modifications**:
  1. Added import `com.spicesfire.sayitenglish.Scorer`.
  2. In `render()`: Added defense-in-depth verification:
     ```java
     SpeechFeedback fb = speechFeedback;
     if (fb != null) {
         long age = System.currentTimeMillis() - fb.shownAt();
         boolean isSentence = "sentence".equals(fb.kind()) 
             || (fb.target() != null && Scorer.words(fb.target()).size() > 1);
         if (age > 4500L || !isSentence) {
             speechFeedback = null;
         } else {
             ChallengeHud.renderSpeechFeedback(g, mc, font, fb, age);
         }
     }
     ```
  3. In `renderSpeechFeedback()`:
     - Header changed from `"\u00a7a\u2714 O Que Eu Disse ..."` to strictly `"disse: "`.
     - Score badge (`fb.score() + "%"`) right-aligned on the header row.
     - Redundant `"Alvo: ..."` line eliminated.
     - Compact dimensions:
       - Width: `Math.min(screenW - 16, Math.max(150, textW + 16))` (minimum 150px).
       - Height: `28 + (trans.isEmpty() ? 0 : 10)` (range 28-38px).
       - Vertical line spacing condensed from 12px to 10px.

---

## 2. Requirement R3: Cyrillic Alphabet Progression Calibration

### 2.1 `src\com\spicesfire\sayitenglish\Alphabet.java`
- **Location**: New method `isIdentical(char ch)` and refactored `computeUnlockedLetters(Map<String, Integer> wordStars, Map<?, ?> heatMap)`.
- **Modifications**:
  1. Added helper method:
     ```java
     public static boolean isIdentical(char ch) {
         Integer lvl = LETTER_LEVEL.get(Character.toLowerCase(ch));
         return lvl != null && lvl <= 6;
     }
     ```
     Identifies Phase 1 transparent twin letters (`А, О, Т, К, М, Е`) that share glyphs and sounds with Latin characters.
  2. Refactored `computeUnlockedLetters`:
     - Global level calculation: Computes `totalStars` across all vocabulary items and evaluates `globalLevel = calculateLevel(totalStars)`.
     - Axis 1 (Identical Letters): For `lvl <= 6`, retains natural immediate substitution as soon as the word has stars (`stars >= 1`).
     - Axis 2 (Non-Identical Letters): For `lvl > 6` (false friends `В, Р, С, Н, У, Х`, Greek constructors `Д, П, Б, Л`, etc.), enforces dual gating:
       `if (globalLevel >= lvl && stars >= 3) { unlocked.add(c); ... }`
     - Heatmap Protection: For exposures, requires `lvl <= 6 || globalLevel >= Math.max(1, lvl - 3) || count >= thresh * 2`.
- **Rationale**:
  - When cutting oak logs (`minecraft:oak_log` -> `Дерево`), `getLettersOrderedByDifficulty` orders letters as `о` (lvl 2), `е` (lvl 6), `в` (lvl 7), `р` (lvl 8), `д` (lvl 13).
  - Under the new calibration:
    - At 1 star: unlocks only `[о, О]`.
    - At 2-5 stars on `oak_log` alone: unlocks `[о, О]` and `[е, Е]`. Non-identical letters (`в`, `р`, `д`) remain locked because `totalStars <= 5` yields `globalLevel <= 3 < 7`.
    - Non-identical letters only unlock when the player accumulates broad vocabulary mastery (`globalLevel >= letterLevel`) AND has practiced the word to mastery (`stars >= 3`).

---

## 3. Verification & Compliance

### 3.1 OpenJDK 17 Compilation
- **Command**:
  ```powershell
  $cp = @(
      "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar",
      "F:\Jogos\Mine\Launcher\data\libraries\com\google\code\gson\gson\2.10\gson-2.10.jar",
      "F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar",
      "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\brigadier\1.1.8\brigadier-1.1.8.jar",
      "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\datafixerupper\6.0.8\datafixerupper-6.0.8.jar",
      "F:\Jogos\Mine\Launcher\data\libraries\org\slf4j\slf4j-api\2.0.1\slf4j-api-2.0.1.jar"
  ) -join ";"

  & "F:\Jogos\Mine\Java17\bin\javac.exe" -cp $cp -encoding UTF-8 -d "$env:TEMP" `
      "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\Alphabet.java" `
      "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeHud.java" `
      "F:\Jogos\Mine\src\com\spicesfire\sayitenglish\client\ChallengeSession.java"
  ```
- **Result**: Exit code `0`, 0 errors, 0 warnings.

### 3.2 Unit Test Execution
- Executed unit test validating:
  - `Alphabet.isIdentical`: passed for all 6 identical letters in upper/lower case; rejected non-identical.
  - `Alphabet.computeUnlockedLetters` on `minecraft:oak_log` with 1 star: returns `[о, О]` only.
  - `Alphabet.computeUnlockedLetters` on `minecraft:oak_log` with 5 stars: returns `[е, Е, о, О]` only (no `в`, `р`, `д`).
  - `Alphabet.computeUnlockedLetters` with mature vocabulary (`totalStars = 14`, `globalLevel = 7`): unlocks `в` (lvl 7), preserves lock on `д` (lvl 13).
  - `Scorer.words`: correctly discriminates 1 word vs multi-word sentences.
- **Result**: All assertions passed.

### 3.3 Canonical E2E Test Suite
- **Command**: `powershell -ExecutionPolicy Bypass -File "F:\Jogos\Mine\tests\run_e2e_tests.ps1" -Strict`
- **Result**: 45/45 test cases passed, exit code `0`.
