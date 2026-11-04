package com.spicesfire.sayitenglish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_2540;

public class AdversarialCoopNetworkingHarness {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> failureDetails = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println(" ADVERSARIAL STRESS TEST: MULTIPLAYER COOP NETWORKING & RANKING (R1)");
        System.out.println("==============================================================================");

        try {
            net.minecraft.class_155.method_36208();
            net.minecraft.class_2966.method_12851();
            System.out.println(" [BOOTSTRAP] Minecraft SharedConstants & Registries initialized successfully.");
        } catch (Throwable t) {
            System.out.println(" [BOOTSTRAP NOTE] " + t.getMessage());
        }

        // Group A: Packets.CoopInvoke
        runTest("ADV-NET-01: CoopInvoke Standard Roundtrip", AdversarialCoopNetworkingHarness::testCoopInvokeNormalRoundtrip);
        runTest("ADV-NET-02: CoopInvoke Dual Constructor Roundtrip", AdversarialCoopNetworkingHarness::testCoopInvokeDualConstructor);
        runTest("ADV-NET-03: CoopInvoke Null Safety in Write/Read", AdversarialCoopNetworkingHarness::testCoopInvokeNullSafety);
        runTest("ADV-NET-04: CoopInvoke Empty String Resilience", AdversarialCoopNetworkingHarness::testCoopInvokeEmptyStrings);
        runTest("ADV-NET-05: CoopInvoke Cyrillic and Unicode Roundtrip", AdversarialCoopNetworkingHarness::testCoopInvokeUnicodeAndCyrillic);
        runTest("ADV-NET-06: CoopInvoke Boundary String Length (32767 chars)", AdversarialCoopNetworkingHarness::testCoopInvokeBoundaryStringLength32767);
        runTest("ADV-NET-07: CoopInvoke Oversized String Overflow Protection (32768 chars)", AdversarialCoopNetworkingHarness::testCoopInvokeOverflowStringLength32768);
        runTest("ADV-NET-08: CoopInvoke Truncated Buffer Underflow Defense", AdversarialCoopNetworkingHarness::testCoopInvokeBufferUnderflow);

        // Group B: Packets.CoopCue
        runTest("ADV-NET-09: CoopCue 11-Parameter Full Roundtrip", AdversarialCoopNetworkingHarness::testCoopCueNormalRoundtrip);
        runTest("ADV-NET-10: CoopCue 7-Parameter Practical Constructor", AdversarialCoopNetworkingHarness::testCoopCue7ParamConstructor);
        runTest("ADV-NET-11: CoopCue 10-Parameter Legacy Constructor", AdversarialCoopNetworkingHarness::testCoopCue10ParamConstructor);
        runTest("ADV-NET-12: CoopCue Null Safety (All String Fields Null)", AdversarialCoopNetworkingHarness::testCoopCueNullSafety);
        runTest("ADV-NET-13: CoopCue Extreme Coordinates (World Border & Void)", AdversarialCoopNetworkingHarness::testCoopCueExtremeCoordinates);
        runTest("ADV-NET-14: CoopCue Special Floating Point Coordinates (NaN & Infinities)", AdversarialCoopNetworkingHarness::testCoopCueSpecialDoubleCoordinates);
        runTest("ADV-NET-15: CoopCue Duration Ticks Boundaries (Negative, Zero, Max Int)", AdversarialCoopNetworkingHarness::testCoopCueDurationTicksBoundaries);
        runTest("ADV-NET-16: CoopCue Multi-line Bilingual Text with IPA Phonetics", AdversarialCoopNetworkingHarness::testCoopCueLongBilingualStrings);
        runTest("ADV-NET-17: CoopCue Truncated Buffer Underflow Defense", AdversarialCoopNetworkingHarness::testCoopCueBufferUnderflow);

        // Group C: Packets.CoopSync
        runTest("ADV-NET-18: CoopSync 6-Parameter Full Roundtrip", AdversarialCoopNetworkingHarness::testCoopSyncNormalRoundtrip);
        runTest("ADV-NET-19: CoopSync 5-Parameter Auto-Tier Calculation Across Tiers", AdversarialCoopNetworkingHarness::testCoopSync5ParamConstructorCalculatedTier);
        runTest("ADV-NET-20: CoopSync 4-Parameter Legacy Constructor Roundtrip", AdversarialCoopNetworkingHarness::testCoopSync4ParamConstructor);
        runTest("ADV-NET-21: CoopSync Null Safety (Null Rank Title & Challenge)", AdversarialCoopNetworkingHarness::testCoopSyncNullSafety);
        runTest("ADV-NET-22: CoopSync Negative and Integer Extreme Values", AdversarialCoopNetworkingHarness::testCoopSyncNegativeAndExtremeValues);
        runTest("ADV-NET-23: CoopSync Truncated Buffer Underflow Defense", AdversarialCoopNetworkingHarness::testCoopSyncBufferUnderflow);

        // Group D: Progress Cooperative Ranking & Progression
        runTest("ADV-RNK-01: Progress.coopRankTier All Threshold Boundaries (0..5000)", AdversarialCoopNetworkingHarness::testProgressRankThresholdBoundaries);
        runTest("ADV-RNK-02: Progress Rank Titles and Visual Color Codes", AdversarialCoopNetworkingHarness::testProgressRankFormatting);
        runTest("ADV-RNK-03: Progress Score Accumulation and Monotonicity Guards", AdversarialCoopNetworkingHarness::testProgressAddScoreAndCompleted);
        runTest("ADV-RNK-04: Progress JSON Serialization and Backward Compatibility", AdversarialCoopNetworkingHarness::testProgressJsonPersistence);

        System.out.println("==============================================================================");
        System.out.println(String.format(" RESULTS: Total=%d, Passed=%d, Failed=%d", totalTests, passedTests, failedTests));
        if (failedTests > 0) {
            System.out.println(" FAILURES:");
            for (String f : failureDetails) {
                System.out.println("  - " + f);
            }
            System.exit(1);
        } else {
            System.out.println(" STATUS: ALL EMPIRICAL ADVERSARIAL STRESS TESTS PASSED (100% PASS)");
            System.exit(0);
        }
    }

    private static void runTest(String name, TestRunnable test) {
        totalTests++;
        try {
            test.run();
            passedTests++;
            System.out.println(String.format("  [%s] -> PASSED", name));
        } catch (Throwable t) {
            failedTests++;
            String err = name + ": " + t.getClass().getSimpleName() + " - " + t.getMessage();
            failureDetails.add(err);
            System.out.println(String.format("  [%s] -> FAILED (%s)", name, t.getMessage()));
            t.printStackTrace(System.out);
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Exception;
    }

    // =========================================================================
    // GROUP A: Packets.CoopInvoke
    // =========================================================================

    private static void testCoopInvokeNormalRoundtrip() throws Exception {
        Packets.CoopInvoke pkt = new Packets.CoopInvoke("request_wood", "Player42", "minecraft:oak_log", "count=5");
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopInvoke decoded = Packets.CoopInvoke.TYPE.read(buf);
        assertEquals("action", "request_wood", decoded.action());
        assertEquals("actionType()", "request_wood", decoded.actionType());
        assertEquals("targetPlayer", "Player42", decoded.targetPlayer());
        assertEquals("targetPlayerId()", "Player42", decoded.targetPlayerId());
        assertEquals("subject", "minecraft:oak_log", decoded.subject());
        assertEquals("arg", "count=5", decoded.arg());
        assertEquals("channel", "sayitenglish:coop_invoke", decoded.channel().toString());
    }

    private static void testCoopInvokeDualConstructor() throws Exception {
        Packets.CoopInvoke pkt = new Packets.CoopInvoke("lava", "60s");
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopInvoke decoded = Packets.CoopInvoke.TYPE.read(buf);
        assertEquals("action", "lava", decoded.action());
        assertEquals("targetPlayer", "", decoded.targetPlayer());
        assertEquals("subject", "", decoded.subject());
        assertEquals("arg", "60s", decoded.arg());
    }

    private static void testCoopInvokeNullSafety() throws Exception {
        Packets.CoopInvoke pkt = new Packets.CoopInvoke(null, null, null, null);
        class_2540 buf = new class_2540(Unpooled.buffer());
        // Must not throw NPE during write!
        pkt.write(buf);

        Packets.CoopInvoke decoded = Packets.CoopInvoke.TYPE.read(buf);
        // Nulls must be normalized to empty strings
        assertEquals("action", "", decoded.action());
        assertEquals("targetPlayer", "", decoded.targetPlayer());
        assertEquals("subject", "", decoded.subject());
        assertEquals("arg", "", decoded.arg());
    }

    private static void testCoopInvokeEmptyStrings() throws Exception {
        Packets.CoopInvoke pkt = new Packets.CoopInvoke("", "", "", "");
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopInvoke decoded = Packets.CoopInvoke.TYPE.read(buf);
        assertEquals("action", "", decoded.action());
        assertEquals("targetPlayer", "", decoded.targetPlayer());
        assertEquals("subject", "", decoded.subject());
        assertEquals("arg", "", decoded.arg());
    }

    private static void testCoopInvokeUnicodeAndCyrillic() throws Exception {
        String ruAction = "\u0441\u043b\u0435\u0434\u0443\u0439"; // следуй
        String ruTarget = "\u0410\u043b\u0435\u043a\u0441\u0435\u0439"; // Алексей
        String ruSubject = "\u0414\u0435\u0440\u0435\u0432\u043e \u2714"; // Дерево ✔
        String ruArg = "\u0411\u044b\u0441\u0442\u0440\u0435\u0435!"; // Быстрее!

        Packets.CoopInvoke pkt = new Packets.CoopInvoke(ruAction, ruTarget, ruSubject, ruArg);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopInvoke decoded = Packets.CoopInvoke.TYPE.read(buf);
        assertEquals("ruAction", ruAction, decoded.action());
        assertEquals("ruTarget", ruTarget, decoded.targetPlayer());
        assertEquals("ruSubject", ruSubject, decoded.subject());
        assertEquals("ruArg", ruArg, decoded.arg());
    }

    private static void testCoopInvokeBoundaryStringLength32767() throws Exception {
        char[] chars = new char[32767];
        Arrays.fill(chars, 'A');
        String maxStr = new String(chars);

        Packets.CoopInvoke pkt = new Packets.CoopInvoke(maxStr, "target", "subj", "arg");
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopInvoke decoded = Packets.CoopInvoke.TYPE.read(buf);
        assertEquals("max length string", 32767, decoded.action().length());
    }

    private static void testCoopInvokeOverflowStringLength32768() throws Exception {
        char[] chars = new char[32768];
        Arrays.fill(chars, 'B');
        String overflowStr = new String(chars);

        Packets.CoopInvoke pkt = new Packets.CoopInvoke(overflowStr, "target", "subj", "arg");
        class_2540 buf = new class_2540(Unpooled.buffer());
        try {
            pkt.write(buf);
            throw new AssertionError("Expected EncoderException or IllegalArgumentException for 32768 chars!");
        } catch (EncoderException | IllegalArgumentException e) {
            // PASS: Netty / Minecraft correctly prevents buffer overflow
        }
    }

    private static void testCoopInvokeBufferUnderflow() throws Exception {
        class_2540 buf = new class_2540(Unpooled.buffer());
        buf.method_10814("only_one_field");

        try {
            Packets.CoopInvoke.TYPE.read(buf);
            throw new AssertionError("Expected DecoderException or IndexOutOfBoundsException for truncated buffer");
        } catch (DecoderException | IndexOutOfBoundsException e) {
            // PASS
        }
    }

    // =========================================================================
    // GROUP B: Packets.CoopCue
    // =========================================================================

    private static void testCoopCueNormalRoundtrip() throws Exception {
        Packets.CoopCue pkt = new Packets.CoopCue(
            "waypoint", "Steve", "Vamos aqui | Let's go here",
            120.5, 64.0, -250.75, 400,
            "\u0418\u0434\u0438 \u0441\u044e\u0434\u0430", "Idi syuda", "Venha aqui", "\u0422\u043e\u0447\u043a\u0430"
        );
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopCue dec = Packets.CoopCue.TYPE.read(buf);
        assertEquals("cueType", "waypoint", dec.cueType());
        assertEquals("initiatorName", "Steve", dec.initiatorName());
        assertEquals("sender()", "Steve", dec.sender());
        assertEquals("action()", "waypoint", dec.action());
        assertEquals("messageBilingual", "Vamos aqui | Let's go here", dec.messageBilingual());
        assertEqualsDouble("x", 120.5, dec.x());
        assertEqualsDouble("y", 64.0, dec.y());
        assertEqualsDouble("z", -250.75, dec.z());
        assertEquals("durationTicks", 400, dec.durationTicks());
        assertEquals("seconds()", 20, dec.seconds()); // 400 / 20 = 20
        assertEquals("ruText", "\u0418\u0434\u0438 \u0441\u044e\u0434\u0430", dec.ruText());
        assertEquals("phonetic", "Idi syuda", dec.phonetic());
        assertEquals("ptText", "Venha aqui", dec.ptText());
        assertEquals("objective", "\u0422\u043e\u0447\u043a\u0430", dec.objective());
    }

    private static void testCoopCue7ParamConstructor() throws Exception {
        Packets.CoopCue pkt = new Packets.CoopCue("beacon", "Alex", "Mensagem", 10.0, 70.0, 20.0, 600);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopCue dec = Packets.CoopCue.TYPE.read(buf);
        assertEquals("cueType", "beacon", dec.cueType());
        assertEquals("seconds()", 30, dec.seconds());
        assertEquals("ruText", "", dec.ruText());
        assertEquals("phonetic", "", dec.phonetic());
        assertEquals("ptText", "", dec.ptText());
        assertEquals("objective", "", dec.objective());
    }

    private static void testCoopCue10ParamConstructor() throws Exception {
        Packets.CoopCue pkt = new Packets.CoopCue(
            "Bob", "follow", "\u0421\u043b\u0435\u0434\u0443\u0439", "Sleduy", "Siga-me", "Alvo",
            100.0, 65.0, -100.0, 15
        );
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopCue dec = Packets.CoopCue.TYPE.read(buf);
        assertEquals("sender()", "Bob", dec.sender());
        assertEquals("action()", "follow", dec.action());
        assertEquals("seconds()", 15, dec.seconds());
        assertEquals("durationTicks", 300, dec.durationTicks()); // 15 * 20 = 300
    }

    private static void testCoopCueNullSafety() throws Exception {
        Packets.CoopCue pkt = new Packets.CoopCue(null, null, null, 0.0, 0.0, 0.0, 0, null, null, null, null);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf); // Must not throw NPE!

        Packets.CoopCue dec = Packets.CoopCue.TYPE.read(buf);
        assertEquals("cueType", "", dec.cueType());
        assertEquals("initiatorName", "", dec.initiatorName());
        assertEquals("messageBilingual", "", dec.messageBilingual());
        assertEquals("ruText", "", dec.ruText());
        assertEquals("phonetic", "", dec.phonetic());
        assertEquals("ptText", "", dec.ptText());
        assertEquals("objective", "", dec.objective());
    }

    private static void testCoopCueExtremeCoordinates() throws Exception {
        double worldBorder = 29999999.0;
        double voidY = -1000.0;
        Packets.CoopCue pkt = new Packets.CoopCue("cue", "Init", "Msg", worldBorder, voidY, -worldBorder, 100);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopCue dec = Packets.CoopCue.TYPE.read(buf);
        assertEqualsDouble("worldBorderX", worldBorder, dec.x());
        assertEqualsDouble("voidY", voidY, dec.y());
        assertEqualsDouble("worldBorderZ", -worldBorder, dec.z());
    }

    private static void testCoopCueSpecialDoubleCoordinates() throws Exception {
        Packets.CoopCue pkt = new Packets.CoopCue("cue", "Init", "Msg", Double.NaN, Double.POSITIVE_INFINITY, Double.MIN_VALUE, 100);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopCue dec = Packets.CoopCue.TYPE.read(buf);
        assertTrue("isNan(x)", Double.isNaN(dec.x()));
        assertEqualsDouble("posInf(y)", Double.POSITIVE_INFINITY, dec.y());
        assertEqualsDouble("minValue(z)", Double.MIN_VALUE, dec.z());
    }

    private static void testCoopCueDurationTicksBoundaries() throws Exception {
        // Negative duration ticks -> seconds() must return 0
        Packets.CoopCue pktNeg = new Packets.CoopCue("cue", "Init", "Msg", 0, 0, 0, -100);
        assertEquals("negative seconds()", 0, pktNeg.seconds());

        // Zero duration ticks -> seconds() must return 0
        Packets.CoopCue pktZero = new Packets.CoopCue("cue", "Init", "Msg", 0, 0, 0, 0);
        assertEquals("zero seconds()", 0, pktZero.seconds());

        // Max integer duration ticks -> must not overflow seconds()
        Packets.CoopCue pktMax = new Packets.CoopCue("cue", "Init", "Msg", 0, 0, 0, Integer.MAX_VALUE);
        assertEquals("max seconds()", Integer.MAX_VALUE / 20, pktMax.seconds());

        // Min integer duration ticks -> seconds() must return 0
        Packets.CoopCue pktMin = new Packets.CoopCue("cue", "Init", "Msg", 0, 0, 0, Integer.MIN_VALUE);
        assertEquals("min seconds()", 0, pktMin.seconds());
    }

    private static void testCoopCueLongBilingualStrings() throws Exception {
        String longRu = "\u0411\u044b\u0441\u0442\u0440\u0435\u0435 \u0431\u0435\u0433\u0438\u0442\u0435 \u0432 \u0443\u0431\u0435\u0436\u0438\u0449\u0435, \u043f\u043e\u043a\u0430 \u043b\u0430\u0432\u0430 \u043d\u0435 \u0434\u043e\u0441\u0442\u0438\u0433\u043b\u0430 \u0432\u0430\u0441!";
        String longIpa = "[ˈbɨstrʲeje bʲɪˈɡʲitʲe v ʊbʲɪˈʐɨɕːe | 'korra para o abrigo]";
        String longPt = "Corram rapido para o abrigo antes que a lava atinja voces!";

        Packets.CoopCue pkt = new Packets.CoopCue("lava", "Host", longRu + " | " + longPt, 0, 64, 0, 1200, longRu, longIpa, longPt, "Sobreviva");
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopCue dec = Packets.CoopCue.TYPE.read(buf);
        assertEquals("longRu", longRu, dec.ruText());
        assertEquals("longIpa", longIpa, dec.phonetic());
        assertEquals("longPt", longPt, dec.ptText());
    }

    private static void testCoopCueBufferUnderflow() throws Exception {
        class_2540 buf = new class_2540(Unpooled.buffer());
        buf.method_10814("partial_cue");
        buf.method_10814("sender");

        try {
            Packets.CoopCue.TYPE.read(buf);
            throw new AssertionError("Expected underflow exception for truncated CoopCue buffer");
        } catch (DecoderException | IndexOutOfBoundsException e) {
            // PASS
        }
    }

    // =========================================================================
    // GROUP C: Packets.CoopSync
    // =========================================================================

    private static void testCoopSyncNormalRoundtrip() throws Exception {
        Packets.CoopSync pkt = new Packets.CoopSync(450, 15, "\u0421\u043b\u0435\u0434\u043e\u043f\u044b\u0442", "The Floor is Lava", 45, 3);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopSync dec = Packets.CoopSync.TYPE.read(buf);
        assertEquals("score", 450, dec.score());
        assertEquals("completedCount", 15, dec.completedCount());
        assertEquals("completed()", 15, dec.completed());
        assertEquals("rankTitle", "\u0421\u043b\u0435\u0434\u043e\u043f\u044b\u0442", dec.rankTitle());
        assertEquals("activeChallengeName", "The Floor is Lava", dec.activeChallengeName());
        assertEquals("timeRemaining", 45, dec.timeRemaining());
        assertEquals("rankTier", 3, dec.rankTier());
    }

    private static void testCoopSync5ParamConstructorCalculatedTier() throws Exception {
        int[] scores = { -50, 0, 99, 100, 299, 300, 599, 600, 999, 1000, 5000 };
        int[] expectedTiers = { 1, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5 };

        for (int i = 0; i < scores.length; i++) {
            Packets.CoopSync pkt = new Packets.CoopSync(scores[i], 5, "Rank", "Task", 10);
            assertEquals("score " + scores[i] + " tier", expectedTiers[i], pkt.rankTier());

            class_2540 buf = new class_2540(Unpooled.buffer());
            pkt.write(buf);
            Packets.CoopSync dec = Packets.CoopSync.TYPE.read(buf);
            assertEquals("roundtrip score " + scores[i], scores[i], dec.score());
            assertEquals("roundtrip tier " + scores[i], expectedTiers[i], dec.rankTier());
        }
    }

    private static void testCoopSync4ParamConstructor() throws Exception {
        Packets.CoopSync pkt = new Packets.CoopSync(150, 2, "Title", 3);
        assertEquals("rankTier", 2, pkt.rankTier());
        assertEquals("completed()", 3, pkt.completed());
        assertEquals("activeChallengeName", "", pkt.activeChallengeName());
        assertEquals("timeRemaining", 0, pkt.timeRemaining());
    }

    private static void testCoopSyncNullSafety() throws Exception {
        Packets.CoopSync pkt = new Packets.CoopSync(100, 5, null, null, 10, 2);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf); // Must not throw NPE!

        Packets.CoopSync dec = Packets.CoopSync.TYPE.read(buf);
        assertEquals("null rankTitle normalized", "", dec.rankTitle());
        assertEquals("null activeChallengeName normalized", "", dec.activeChallengeName());
    }

    private static void testCoopSyncNegativeAndExtremeValues() throws Exception {
        Packets.CoopSync pkt = new Packets.CoopSync(Integer.MIN_VALUE, Integer.MAX_VALUE, "Ext", "Ext", -1, 1);
        class_2540 buf = new class_2540(Unpooled.buffer());
        pkt.write(buf);

        Packets.CoopSync dec = Packets.CoopSync.TYPE.read(buf);
        assertEquals("int min score", Integer.MIN_VALUE, dec.score());
        assertEquals("int max completed", Integer.MAX_VALUE, dec.completedCount());
        assertEquals("negative time", -1, dec.timeRemaining());
    }

    private static void testCoopSyncBufferUnderflow() throws Exception {
        class_2540 buf = new class_2540(Unpooled.buffer());
        buf.method_10804(100); // only 1 int written

        try {
            Packets.CoopSync.TYPE.read(buf);
            throw new AssertionError("Expected underflow exception for truncated CoopSync buffer");
        } catch (DecoderException | IndexOutOfBoundsException e) {
            // PASS
        }
    }

    // =========================================================================
    // GROUP D: Progress Cooperative Ranking & Progression Boundaries
    // =========================================================================

    private static void testProgressRankThresholdBoundaries() throws Exception {
        int[] testScores =       { -100,      0,         99,        100,       299,       300,        599,        600,              999,              1000,      5000 };
        int[] expectedTiers =    { 1,         1,         1,         2,         2,         3,          3,          4,                4,                5,         5 };
        String[] expectedRaws =  { "Новичок", "Новичок", "Новичок", "Товарищ", "Товарищ", "Следопыт", "Следопыт", "Мастер Команды", "Мастер Команды", "Легенда", "Легенда" };
        int[] expectedNext =     { 100,       100,       100,       300,       300,       600,        600,        1000,             1000,             1000,      1000 };
        int[] expectedDiff =     { 200,       100,       1,         200,       1,         300,        1,          400,              1,                0,         0 };

        for (int i = 0; i < testScores.length; i++) {
            Progress p = new Progress();
            p.coopScore = testScores[i];

            assertEquals("score " + testScores[i] + " tier", expectedTiers[i], p.coopRankTier());
            assertEquals("score " + testScores[i] + " getCoopRank", expectedTiers[i], p.getCoopRank());
            assertEquals("score " + testScores[i] + " raw name", expectedRaws[i], p.coopRankNameRaw());
            assertEquals("score " + testScores[i] + " next threshold", expectedNext[i], p.nextRankThreshold());
            assertEquals("score " + testScores[i] + " pointsToNextRank", expectedDiff[i], p.pointsToNextRank());
        }
    }

    private static void testProgressRankFormatting() throws Exception {
        Progress p = new Progress();

        p.coopScore = 0;
        assertTrue("Tier 1 color \u00a77", p.coopRankTitle().startsWith("\u00a77"));
        assertEquals("Tier 1 title", "\u00a77\u041d\u043e\u0432\u0438\u0447\u043e\u043a", p.coopRankTitle());

        p.coopScore = 100;
        assertTrue("Tier 2 color \u00a7a", p.coopRankTitle().startsWith("\u00a7a"));
        assertEquals("Tier 2 title", "\u00a7a\u0422\u043e\u0432\u0430\u0440\u0438\u0449", p.coopRankTitle());

        p.coopScore = 300;
        assertTrue("Tier 3 color \u00a7b", p.coopRankTitle().startsWith("\u00a7b"));
        assertEquals("Tier 3 title", "\u00a7b\u0421\u043b\u0435\u0434\u043e\u043f\u044b\u0442", p.coopRankTitle());

        p.coopScore = 600;
        assertTrue("Tier 4 color \u00a7d", p.coopRankTitle().startsWith("\u00a7d"));
        assertEquals("Tier 4 title", "\u00a7d\u041c\u0430\u0441\u0442\u0435\u0440 \u041a\u043e\u043c\u0430\u043d\u0434\u044b", p.coopRankTitle());

        p.coopScore = 1000;
        assertTrue("Tier 5 color \u00a76", p.coopRankTitle().startsWith("\u00a76"));
        assertEquals("Tier 5 title", "\u00a76\u041b\u0435\u0433\u0435\u043d\u0434\u0430", p.coopRankTitle());
        assertTrue("Max rank indication in nextRankTitle()", p.nextRankTitle().contains("M\u00e1ximo") || p.nextRankTitle().contains("Maximo"));
    }

    private static void testProgressAddScoreAndCompleted() throws Exception {
        Progress p = new Progress();
        assertEquals("initial score", 0, p.coopScore);
        assertEquals("initial timestamp", 0L, p.lastCoopTimestamp);

        // Add 50 pts
        p.addCoopScore(50);
        assertEquals("added 50 pts", 50, p.coopScore);
        assertTrue("timestamp updated", p.lastCoopTimestamp > 0);

        long ts = p.lastCoopTimestamp;

        // Add 0 pts -> should not change score
        p.addCoopScore(0);
        assertEquals("add 0 pts", 50, p.coopScore);

        // Add negative pts -> should not change score
        p.addCoopScore(-30);
        assertEquals("add negative pts", 50, p.coopScore);

        // Increment completed
        assertEquals("initial completed challenges", 0, p.coopCompletedChallenges);
        p.incrementCoopCompleted();
        assertEquals("completed challenges", 1, p.coopCompletedChallenges);
        assertEquals("coopCompleted alias", 1, p.coopCompleted);
    }

    private static void testProgressJsonPersistence() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Progress p = new Progress();
        p.coopScore = 750;
        p.coopCompletedChallenges = 18;
        p.coopCompleted = 18;
        p.coopPhrases = 7;
        p.coopDialogues = 3;
        p.lastCoopTimestamp = 1730720000000L;

        String json = gson.toJson(p);
        assertTrue("json contains coopScore", json.contains("\"coopScore\": 750"));
        assertTrue("json contains coopCompletedChallenges", json.contains("\"coopCompletedChallenges\": 18"));
        assertTrue("json contains coopPhrases", json.contains("\"coopPhrases\": 7"));
        assertTrue("json contains coopDialogues", json.contains("\"coopDialogues\": 3"));

        Progress restored = gson.fromJson(json, Progress.class);
        assertEquals("restored coopScore", 750, restored.coopScore);
        assertEquals("restored coopCompletedChallenges", 18, restored.coopCompletedChallenges);
        assertEquals("restored coopCompleted", 18, restored.coopCompleted);
        assertEquals("restored coopPhrases", 7, restored.coopPhrases);
        assertEquals("restored coopDialogues", 3, restored.coopDialogues);
        assertEquals("restored lastCoopTimestamp", 1730720000000L, restored.lastCoopTimestamp);
        assertEquals("restored rank tier", 4, restored.coopRankTier());

        // Backward compatibility: JSON missing coop fields entirely
        String legacyJson = "{\"language\":\"ru\",\"sentences\":5,\"lost\":1}";
        Progress legacy = gson.fromJson(legacyJson, Progress.class);
        assertEquals("legacy coopScore default", 0, legacy.coopScore);
        assertEquals("legacy coopRankTier default", 1, legacy.coopRankTier());
        assertEquals("legacy coopCompleted default", 0, legacy.coopCompleted);
    }

    // Helper assertions
    private static void assertEquals(String desc, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(String.format("Assertion '%s' failed: expected <%s> but got <%s>", desc, expected, actual));
    }

    private static void assertEquals(String desc, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(String.format("Assertion '%s' failed: expected <%d> but got <%d>", desc, expected, actual));
        }
    }

    private static void assertEquals(String desc, long expected, long actual) {
        if (expected != actual) {
            throw new AssertionError(String.format("Assertion '%s' failed: expected <%d> but got <%d>", desc, expected, actual));
        }
    }

    private static void assertEqualsDouble(String desc, double expected, double actual) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(String.format("Assertion '%s' failed: expected <%f> but got <%f>", desc, expected, actual));
        }
    }

    private static void assertTrue(String desc, boolean condition) {
        if (!condition) {
            throw new AssertionError(String.format("Assertion '%s' failed: condition is false", desc));
        }
    }
}
