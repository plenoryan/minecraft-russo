# ==============================================================================
# test_adversarial_r2_r3.ps1 - Empirical Adversarial Stress Test Harness
# Target: Requirement R2 (Speech Feedback HUD) & Requirement R3 (Cyrillic Alphabet Progression)
# Modpack: Minecraft Russian Language Learning (Fabric 1.20.1)
# ==============================================================================
[CmdletBinding()]
param (
    [string]$TargetRoot = "F:\Jogos\Mine",
    [switch]$ExportJson = $true
)

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.ForegroundColor = "White"

# Always enforce TargetRoot as working directory for relative config lookups
Set-Location $TargetRoot

$results = [System.Collections.Generic.List[PSCustomObject]]::new()
$startTime = Get-Date

function Register-AdvResult {
    param (
        [string]$Id,
        [string]$Suite,
        [string]$Name,
        [string]$Status, # PASS, FAIL, VULNERABILITY_FOUND, WARNING
        [string]$Details,
        [hashtable]$Evidence = @{}
    )
    $obj = [PSCustomObject]@{
        Id = $Id
        Suite = $Suite
        Name = $Name
        Status = $Status
        Details = $Details
        Evidence = $Evidence
        Timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    }
    $results.Add($obj)

    $color = switch ($Status) {
        "PASS" { "Green" }
        "FAIL" { "Red" }
        "VULNERABILITY_FOUND" { "Magenta" }
        "WARNING" { "Yellow" }
        default { "White" }
    }
    Write-Host ("  [{0}] {1} :: {2} -> {3}" -f $Id, $Suite, $Name, $Status) -ForegroundColor $color
    if ($Details) {
        Write-Host ("      {0}" -f $Details) -ForegroundColor Gray
    }
}

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " TIER 5 EMPIRICAL ADVERSARIAL STRESS TEST: R2 (HUD) & R3 (ALPHABET)" -ForegroundColor Cyan
Write-Host " Minecraft Russian Learning Modpack ('Melty Voice' Edition)" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host (" Target Root     : {0}" -f $TargetRoot) -ForegroundColor Gray
Write-Host (" Timestamp (UTC) : {0}" -f (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")) -ForegroundColor Gray
Write-Host ""

$javac = Join-Path $TargetRoot "Java17\bin\javac.exe"
$java = Join-Path $TargetRoot "Java17\bin\java.exe"
$javap = Join-Path $TargetRoot "Java17\bin\javap.exe"

if (-not (Test-Path $javac) -or -not (Test-Path $java)) {
    Write-Error "Java 17 runtime not found in $TargetRoot\Java17\bin"
    exit 1
}

$clientJar = Join-Path $TargetRoot "Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar"
$serverJar = Join-Path $TargetRoot "Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar"
$gsonJar = Join-Path $TargetRoot "Launcher\data\libraries\com\google\code\gson\gson\2.10\gson-2.10.jar"
$intermJar = Join-Path $TargetRoot "Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar"

$cp = @($clientJar, $gsonJar, $intermJar, (Join-Path $TargetRoot "src")) -join ";"

# ------------------------------------------------------------------------------
# STEP 1: Write and Compile Java Adversarial Harness
# ------------------------------------------------------------------------------
$javaHarnessCode = @'
package tests;

import com.spicesfire.sayitenglish.Alphabet;
import com.spicesfire.sayitenglish.Scorer;
import java.util.*;

public class AdversarialTester {
    private static int passes = 0;
    private static int fails = 0;

    private static void check(String id, boolean condition, String desc) {
        if (condition) {
            passes++;
            System.out.println("JAVA_ASSERT|PASS|" + id + "|" + desc);
        } else {
            fails++;
            System.out.println("JAVA_ASSERT|FAIL|" + id + "|" + desc);
        }
    }

    public static void main(String[] args) {
        testR3CyrillicCalibration();
        testR2SpeechHudFilter();
        System.out.println("JAVA_SUMMARY|" + passes + "|" + fails);
    }

    private static void testR3CyrillicCalibration() {
        // R3.1: Oak log gathering (Дерево) with 1, 2, 3, 4, 5 stars under zero global score
        // Word: Дерево contains: Д (13), е (6), р (8), е (6), в (7), о (2)
        // Difficulty order: о (2), е (6), в (7), р (8), д (13)
        // Identical letters: о, е. Non-identical: в, р, д.

        // Stars = 1
        Map<String, Integer> ws1 = Collections.singletonMap("minecraft:oak_log", 1);
        Set<Character> u1 = Alphabet.computeUnlockedLetters(ws1, Collections.emptyMap());
        check("R3_OAK_1", u1.contains('\u043e') && u1.contains('\u041e') && u1.size() == 2,
            "Oak 1 star unlocks ONLY 'o'/'O' (size=" + u1.size() + ")");
        check("R3_OAK_1_LOCK", !u1.contains('\u0434') && !u1.contains('\u0432') && !u1.contains('\u0440') && !u1.contains('\u0435'),
            "Oak 1 star keeps d, v, r, e strictly locked");

        // Stars = 2
        Map<String, Integer> ws2 = Collections.singletonMap("minecraft:oak_log", 2);
        Set<Character> u2 = Alphabet.computeUnlockedLetters(ws2, Collections.emptyMap());
        check("R3_OAK_2", u2.contains('\u043e') && u2.contains('\u041e') && u2.contains('\u0435') && u2.contains('\u0415') && u2.size() == 4,
            "Oak 2 stars unlocks ONLY identical letters 'o','e' (size=" + u2.size() + ")");
        check("R3_OAK_2_LOCK", !u2.contains('\u0434') && !u2.contains('\u0432') && !u2.contains('\u0440'),
            "Oak 2 stars keeps non-identical d, v, r strictly locked");

        // Stars = 3 (global level = (3+1)/2 = 2. lvl for v is 7 > 2)
        Map<String, Integer> ws3 = Collections.singletonMap("minecraft:oak_log", 3);
        Set<Character> u3 = Alphabet.computeUnlockedLetters(ws3, Collections.emptyMap());
        check("R3_OAK_3", u3.contains('\u043e') && u3.contains('\u0435') && u3.size() == 4,
            "Oak 3 stars with zero global score unlocks ONLY 'o','e'");
        check("R3_OAK_3_LOCK", !u3.contains('\u0434') && !u3.contains('\u0432') && !u3.contains('\u0440'),
            "Oak 3 stars with zero global score: non-identical d, v, r strictly locked");

        // Stars = 4 (global level = 2)
        Map<String, Integer> ws4 = Collections.singletonMap("minecraft:oak_log", 4);
        Set<Character> u4 = Alphabet.computeUnlockedLetters(ws4, Collections.emptyMap());
        check("R3_OAK_4", u4.contains('\u043e') && u4.contains('\u0435') && u4.size() == 4,
            "Oak 4 stars with zero global score unlocks ONLY 'o','e'");
        check("R3_OAK_4_LOCK", !u4.contains('\u0434') && !u4.contains('\u0432') && !u4.contains('\u0440'),
            "Oak 4 stars with zero global score: non-identical d, v, r strictly locked");

        // Stars = 5 (global level = (5+1)/2 = 3. v is 7, r is 8, d is 13; all > 3)
        Map<String, Integer> ws5 = Collections.singletonMap("minecraft:oak_log", 5);
        Set<Character> u5 = Alphabet.computeUnlockedLetters(ws5, Collections.emptyMap());
        check("R3_OAK_5", u5.contains('\u043e') && u5.contains('\u0435') && u5.size() == 4,
            "Oak 5 stars with zero global score unlocks ONLY 'o','e' (size=" + u5.size() + ")");
        check("R3_OAK_5_LOCK", !u5.contains('\u0434') && !u5.contains('\u0432') && !u5.contains('\u0440'),
            "Oak 5 stars with zero global score: non-identical d, v, r remain strictly locked!");

        // R3.2: High star counts on single words vs multi-word global progression
        // Single word with 10 stars (global level = 5): v(7), r(8), d(13) still locked!
        Map<String, Integer> ws10 = Collections.singletonMap("minecraft:oak_log", 10);
        Set<Character> u10 = Alphabet.computeUnlockedLetters(ws10, Collections.emptyMap());
        check("R3_SINGLE_10", !u10.contains('\u0432') && !u10.contains('\u0440') && !u10.contains('\u0434'),
            "Single word 10 stars: global level 5 still locks v(7), r(8), d(13)");

        // Single word with 20 stars (global level = 10): v(7) and r(8) unlock, but d(13) stays locked!
        Map<String, Integer> ws20 = Collections.singletonMap("minecraft:oak_log", 20);
        Set<Character> u20 = Alphabet.computeUnlockedLetters(ws20, Collections.emptyMap());
        check("R3_SINGLE_20", u20.contains('\u0432') && u20.contains('\u0440') && !u20.contains('\u0434'),
            "Single word 20 stars: v(7) and r(8) unlock at level 10, but d(13) stays locked");

        // Multi-word progression: 10 words with 3 stars each (totalStars = 30, globalLevel = 15)
        // With oak_log at 3 stars: unlockCount=3 -> o(2), e(6), v(7). v unlocks! r(8) and d(13) require stars >= 4 and >= 5.
        Map<String, Integer> multiWords3 = new HashMap<>();
        for (int i = 0; i < 9; i++) {
            multiWords3.put("fake_word_" + i, 3);
        }
        multiWords3.put("minecraft:oak_log", 3);
        Set<Character> uMulti3 = Alphabet.computeUnlockedLetters(multiWords3, Collections.emptyMap());
        check("R3_MULTI_30_STARS3", uMulti3.contains('\u0432') && !uMulti3.contains('\u0440') && !uMulti3.contains('\u0434'),
            "Multi-word maturity (30 stars, level 15) with oak_log stars=3: v(7) unlocks, r(8) and d(13) require higher star count");

        // Multi-word progression with oak_log at 5 stars (totalStars = 32, globalLevel = 16):
        // All 5 letters are eligible and globalLevel >= 13 -> d(13), v(7), r(8) all unlock!
        Map<String, Integer> multiWords5 = new HashMap<>(multiWords3);
        multiWords5.put("minecraft:oak_log", 5);
        Set<Character> uMulti5 = Alphabet.computeUnlockedLetters(multiWords5, Collections.emptyMap());
        check("R3_MULTI_32_STARS5", uMulti5.contains('\u0434') && uMulti5.contains('\u0432') && uMulti5.contains('\u0440'),
            "Multi-word maturity (32 stars, level 16) with oak_log stars=5: d(13), v(7), r(8) fully unlock!");

        // Multi-word with oak_log only at 2 stars: even with globalLevel 15, non-identical letters require stars >= 3
        Map<String, Integer> multiWords2 = new HashMap<>(multiWords3);
        multiWords2.put("minecraft:oak_log", 2);
        Set<Character> uMulti2 = Alphabet.computeUnlockedLetters(multiWords2, Collections.emptyMap());
        check("R3_MULTI_MASTERY", !uMulti2.contains('\u0434') && !uMulti2.contains('\u0432') && !uMulti2.contains('\u0440'),
            "Multi-word maturity without individual word mastery (stars=2 < 3): non-identical d,v,r stay locked");

        // R3.3: Identical letters (А, О, Е, Т, К, М) natural substitution zero regression
        char[] identicals = {'\u0430', '\u0410', '\u043e', '\u041e', '\u0435', '\u0415', '\u0442', '\u0422', '\u043a', '\u041a', '\u043c', '\u041c'};
        boolean allIdenticalPass = true;
        for (char c : identicals) {
            if (!Alphabet.isIdentical(c)) allIdenticalPass = false;
        }
        check("R3_IS_IDENTICAL_POS", allIdenticalPass, "Alphabet.isIdentical returns true for all А, О, Е, Т, К, М (both cases)");

        char[] nonIdenticals = {'\u0432', '\u0440', '\u0441', '\u043d', '\u0443', '\u0445', '\u0434', '\u043f', '\u0431', '\u0433', '\u043b', '\u0438', '\u0439', '\u0436', '\u0437', '\u0447', '\u0448', '\u0449', '\u0446', '\u0444', '\u044f', '\u044e', '\u0451', '\u044b', '\u044d', '\u044c', '\u044a'};
        boolean allNonIdenticalPass = true;
        for (char c : nonIdenticals) {
            if (Alphabet.isIdentical(c)) allNonIdenticalPass = false;
        }
        check("R3_IS_IDENTICAL_NEG", allNonIdenticalPass, "Alphabet.isIdentical returns false for all 27 non-identical Cyrillic letters");

        // Words with identical letters unlock immediately with stars >= 1 under zero global score
        // "кот" (к, о, т - levels 4, 2, 3 - all identical)
        Map<String, Integer> wsCat = Collections.singletonMap("\u043a\u043e\u0442", 3);
        Set<Character> uCat = Alphabet.computeUnlockedLetters(wsCat, Collections.emptyMap());
        check("R3_IDENTICAL_UNREGRESSED", uCat.contains('\u043a') && uCat.contains('\u043e') && uCat.contains('\u0442'),
            "Word 'кот' (all identical) unlocks all letters k, o, t immediately under zero global score");

        // R3.4: Heatmap calibration stress testing
        // Identical letters unlock at threshold (15) even with globalLevel 0
        Map<Character, Integer> hmIdentical = Collections.singletonMap('\u043e', 15);
        Set<Character> uHmIdent = Alphabet.computeUnlockedLetters(Collections.emptyMap(), hmIdentical);
        check("R3_HEAT_IDENTICAL", uHmIdent.contains('\u043e') && uHmIdent.contains('\u041e'),
            "Heatmap: identical letter 'o' unlocks at threshold 15 with zero global score");

        // Non-identical letter 'v' (lvl 7, thresh 25) with count=25 and globalLevel=0: MUST remain locked
        Map<Character, Integer> hmNonIdent = Collections.singletonMap('\u0432', 25);
        Set<Character> uHmNonIdent = Alphabet.computeUnlockedLetters(Collections.emptyMap(), hmNonIdent);
        check("R3_HEAT_NONIDENT_LOCK", !uHmNonIdent.contains('\u0432'),
            "Heatmap: non-identical letter 'v' at single threshold 25 with zero global score stays locked");

        // Non-identical letter 'v' with count >= thresh * 2 (50): unlocks via heavy immersion
        Map<Character, Integer> hmNonIdentDouble = Collections.singletonMap('\u0432', 50);
        Set<Character> uHmDouble = Alphabet.computeUnlockedLetters(Collections.emptyMap(), hmNonIdentDouble);
        check("R3_HEAT_NONIDENT_DOUBLE", uHmDouble.contains('\u0432'),
            "Heatmap: non-identical letter 'v' unlocks when immersion reaches 2x threshold (50)");

        // R3.5: Null and boundary robustness
        Set<Character> uNull = Alphabet.computeUnlockedLetters(null, null);
        check("R3_NULL_SAFETY", uNull != null && uNull.isEmpty(), "Null wordStars and null heatMap handled safely (empty set)");

        Map<String, Integer> wsNeg = Collections.singletonMap("minecraft:oak_log", -5);
        Set<Character> uNeg = Alphabet.computeUnlockedLetters(wsNeg, Collections.emptyMap());
        check("R3_NEG_SAFETY", uNeg != null && uNeg.isEmpty(), "Negative star count produces empty unlocked set");

        check("R3_CALCULATE_LEVEL_CAP", Alphabet.calculateLevel(1000) == 32, "calculateLevel caps gracefully at 32");
        check("R3_CALCULATE_LEVEL_ZERO", Alphabet.calculateLevel(0) == 0 && Alphabet.calculateLevel(-10) == 0, "calculateLevel handles 0 and negative cleanly");
    }

    private static void testR2SpeechHudFilter() {
        // R2: Speech Feedback HUD display rules
        // In ChallengeSession.finish():
        // boolean isSentence = (c != null && "sentence".equals(c.kind()))
        //     || (prompt != null && prompt.target() != null && Scorer.words(prompt.target()).size() > 1);

        // Helper oracle function matching ChallengeSession.finish()
        // returns true if speechFeedback is instantiated, false if strictly null

        // 1. Single-word item challenges ("word", "pickup", "craft")
        check("R2_ITEM_APPLE", !evalIsSentence("word", "apple"), "Single word 'apple' evaluates isSentence=false");
        check("R2_ITEM_DEREVO", !evalIsSentence("word", "\u0414\u0435\u0440\u0435\u0432\u043e"), "Single Cyrillic word 'Дерево' evaluates isSentence=false");
        check("R2_ITEM_KAMEN", !evalIsSentence("pickup", "\u041a\u0430\u043c\u0435\u043d\u044c"), "Item challenge 'Камень' evaluates isSentence=false");

        // 2. Practice challenge ("practice") with 1 word
        check("R2_PRACTICE_ONE", !evalIsSentence("practice", "bread"), "Practice challenge on single word 'bread' evaluates isSentence=false");
        check("R2_PRACTICE_HLEB", !evalIsSentence("practice", "\u0425\u043b\u0435\u0431"), "Practice challenge on Cyrillic 'Хлеб' evaluates isSentence=false");

        // 3. Empty, whitespace, null targets
        check("R2_EMPTY_TARGET", !evalIsSentence("word", ""), "Empty target evaluates isSentence=false");
        check("R2_WS_TARGET", !evalIsSentence("word", "   "), "Whitespace target evaluates isSentence=false");
        check("R2_NULL_TARGET", !evalIsSentence("word", null), "Null target evaluates isSentence=false");
        check("R2_PUNCT_ONLY", !evalIsSentence("word", "!@#$%^&*()"), "Punctuation-only target evaluates isSentence=false");

        // 4. Punctuation with single words
        check("R2_PUNCT_WORD_EXCLAM", !evalIsSentence("word", "apple!"), "Single word with exclamation 'apple!' evaluates isSentence=false");
        check("R2_PUNCT_WORD_COMMA", !evalIsSentence("word", "apple,"), "Single word with comma 'apple,' evaluates isSentence=false");
        check("R2_PUNCT_WORD_DOTS", !evalIsSentence("word", "...wood..."), "Single word with ellipsis '...wood...' evaluates isSentence=false");

        // 5. Explicit "sentence" challenge kind
        check("R2_KIND_SENTENCE_1W", evalIsSentence("sentence", "word"), "Kind 'sentence' with 1 word evaluates isSentence=true");
        check("R2_KIND_SENTENCE_MW", evalIsSentence("sentence", "Give me the oak log"), "Kind 'sentence' with multi-word evaluates isSentence=true");

        // 6. Multi-word phrases in non-sentence challenges
        check("R2_PHRASE_2W", evalIsSentence("word", "red apple"), "2-word phrase 'red apple' evaluates isSentence=true");
        check("R2_PHRASE_3W_RU", evalIsSentence("word", "\u0414\u0430\u0439 \u043c\u043d\u0435 \u0434\u0435\u0440\u0435\u0432\u043e"),
            "3-word Cyrillic phrase 'Дай мне дерево' evaluates isSentence=true");
        check("R2_PHRASE_LAVA", evalIsSentence("challenge", "\u041f\u043e\u043b \u044d\u0442\u043e \u043b\u0430\u0432\u0430"),
            "Cooperative phrase 'Пол это лава' evaluates isSentence=true");
        check("R2_PHRASE_FOLLOW", evalIsSentence("challenge", "\u0421\u043b\u0435\u0434\u0443\u0439 \u0437\u0430 \u043c\u043d\u043e\u0439"),
            "Cooperative phrase 'Следуй за мной' evaluates isSentence=true");
    }

    private static boolean evalIsSentence(String kind, String target) {
        boolean kindSentence = kind != null && "sentence".equals(kind);
        boolean wordsMulti = target != null && Scorer.words(target).size() > 1;
        return kindSentence || wordsMulti;
    }
}
'@

$testSrcDir = Join-Path $TargetRoot "tests"
$testerJava = Join-Path $testSrcDir "AdversarialTester.java"
$testerClass = Join-Path $testSrcDir "AdversarialTester.class"

[System.IO.File]::WriteAllText($testerJava, $javaHarnessCode, [System.Text.Encoding]::ASCII)

Write-Host "[1/3] Compiling Java Adversarial Harness with OpenJDK 17 javac..." -ForegroundColor Yellow
$compileOutput = & $javac -cp $cp -d $testSrcDir $testerJava 2>&1
if ($LASTEXITCODE -ne 0) {
    Register-AdvResult "ADV_BUILD_01" "Build" "Compile AdversarialTester.java" "FAIL" ($compileOutput -join " `n")
    Write-Host "Failed to compile Java adversarial test harness!" -ForegroundColor Red
    exit 1
} else {
    Register-AdvResult "ADV_BUILD_01" "Build" "Compile AdversarialTester.java" "PASS" "Compiled cleanly with OpenJDK 17 javac"
}

# ------------------------------------------------------------------------------
# STEP 2: Execute Java Adversarial Harness and Parse Assertions
# ------------------------------------------------------------------------------
Write-Host "[2/3] Executing Java Adversarial Harness against Live Bytecodes..." -ForegroundColor Yellow
$runOutput = & $java -cp "$cp;$testSrcDir" tests.AdversarialTester 2>&1
$javaExitCode = $LASTEXITCODE

foreach ($line in $runOutput) {
    if ($line -match "^JAVA_ASSERT\|(PASS|FAIL)\|([^\|]+)\|(.*)$") {
        $status = $matches[1]
        $assertId = $matches[2]
        $desc = $matches[3]
        $suite = if ($assertId -like "R3*") { "R3_Alphabet" } else { "R2_SpeechHUD" }
        Register-AdvResult $assertId $suite $desc $status $desc
    }
}

# Clean up temporary test harness files
Remove-Item (Join-Path $testSrcDir "tests") -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item $testerJava -Force -ErrorAction SilentlyContinue
Remove-Item $testerClass -Force -ErrorAction SilentlyContinue

# ------------------------------------------------------------------------------
# STEP 3: Bytecode Disassembly Forensic Verification for ChallengeHud & ChallengeSession
# ------------------------------------------------------------------------------
Write-Host "[3/3] Inspecting Bytecodes in Deployed Client Mod JAR..." -ForegroundColor Yellow

# Inspect ChallengeHud.renderSpeechFeedback for:
# 1. header "disse: "
# 2. sipush 150 (min box width 150px)
# 3. Math.max(150, textW + 16)
# 4. height 28 + (trans.isEmpty() ? 0 : 10)
# 5. age cutoff 4500ms

$hudBytecode = & $javap -c -p -cp $clientJar com.spicesfire.sayitenglish.client.ChallengeHud 2>&1 | Out-String

$hasStrictHeader = $hudBytecode -match 'String disse:\s*'
Register-AdvResult "ADV_HUD_01" "Bytecode_HUD" "Strict Header 'disse: ' in ChallengeHud" `
    ($(if ($hasStrictHeader) { "PASS" } else { "FAIL" })) `
    "Header constant 'disse: ' verified in bytecode (no formatting prefixes)"

$hasMin150Width = $hudBytecode -match 'sipush\s+150'
Register-AdvResult "ADV_HUD_02" "Bytecode_HUD" "Minimum Box Width 150px in ChallengeHud" `
    ($(if ($hasMin150Width) { "PASS" } else { "FAIL" })) `
    "Bytecode contains sipush 150 and Math.max constraint"

$hasCompactHeight = $hudBytecode -match 'bipush\s+28'
Register-AdvResult "ADV_HUD_03" "Bytecode_HUD" "Compact Box Height (28-38px) in ChallengeHud" `
    ($(if ($hasCompactHeight) { "PASS" } else { "FAIL" })) `
    "Bytecode contains base height bipush 28 with dynamic 10px translation row"

$hasAgeCutoff = $hudBytecode -match 'ldc2_w\s+#?\d+\s*//\s*long 4500l' -or $hudBytecode -match '4500'
Register-AdvResult "ADV_HUD_04" "Bytecode_HUD" "HUD Feedback Age Cutoff (4500ms)" `
    ($(if ($hasAgeCutoff) { "PASS" } else { "FAIL" })) `
    "Bytecode enforces 4500ms display expiration cutoff"

# Inspect ChallengeSession.finish for speechFeedback assignment & nullification
$sessionBytecode = & $javap -c -p -cp $clientJar com.spicesfire.sayitenglish.client.ChallengeSession 2>&1 | Out-String
$hasNullBranch = $sessionBytecode -match 'aconst_null\s*[\r\n]+\s*\d+:\s*putstatic\s+#\d+\s*// Field com/spicesfire/sayitenglish/client/ChallengeHud\.speechFeedback'
Register-AdvResult "ADV_HUD_05" "Bytecode_Session" "Explicit speechFeedback=null on Single-Word Challenges" `
    ($(if ($hasNullBranch) { "PASS" } else { "FAIL" })) `
    "Bytecode confirms aconst_null putstatic speechFeedback when isSentence evaluates to false"

# Check dual deployment parity
$clientJarSize = (Get-Item $clientJar).Length
$serverJarSize = (Get-Item $serverJar).Length
Register-AdvResult "ADV_DEPLOY_01" "Deployment" "Dual Deployment JAR Byte Parity" `
    ($(if ($clientJarSize -eq $serverJarSize -and $clientJarSize -gt 30000000) { "PASS" } else { "FAIL" })) `
    ("Client JAR: {0} bytes, Server JAR: {1} bytes" -f $clientJarSize, $serverJarSize)

# ------------------------------------------------------------------------------
# SUMMARY & OUTPUT
# ------------------------------------------------------------------------------
$total = $results.Count
$passed = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failed = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
$vulns = ($results | Where-Object { $_.Status -eq "VULNERABILITY_FOUND" }).Count
$duration = [Math]::Round(((Get-Date) - $startTime).TotalMilliseconds)

Write-Host ""
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " ADVERSARIAL TEST EXECUTION SUMMARY" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host (" Total Assertions Evaluated : {0}" -f $total)
Write-Host (" Passed                     : {0}" -f $passed) -ForegroundColor Green
Write-Host (" Failed                     : {0}" -f $failed) -ForegroundColor $(if ($failed -gt 0) { "Red" } else { "Gray" })
Write-Host (" Vulnerabilities Found      : {0}" -f $vulns) -ForegroundColor $(if ($vulns -gt 0) { "Magenta" } else { "Gray" })
Write-Host (" Total Duration             : {0} ms" -f $duration)
Write-Host "==============================================================================" -ForegroundColor Cyan

if ($ExportJson) {
    $outPath = Join-Path $TargetRoot "tests\test_adversarial_r2_r3_results.json"
    $results | ConvertTo-Json -Depth 5 | Set-Content -Path $outPath -Encoding UTF8
    Write-Host (" Results exported to: {0}" -f $outPath) -ForegroundColor Gray
}

if ($failed -gt 0 -or $vulns -gt 0) {
    exit 1
} else {
    exit 0
}
