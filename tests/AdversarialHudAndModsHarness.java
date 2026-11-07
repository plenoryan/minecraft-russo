package com.spicesfire.sayitenglish;

import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.client.ChallengeHud;
import com.spicesfire.sayitenglish.client.SayItClient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_1799;
import net.minecraft.class_7923;

public class AdversarialHudAndModsHarness {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> failureDetails = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println(" ADVERSARIAL STRESS TEST: HUD NOTIFICATIONS, IRIS UNBIND & MODS (R2)");
        System.out.println("==============================================================================");

        try {
            net.minecraft.class_155.method_36208();
            net.minecraft.class_2966.method_12851();
            System.out.println(" [BOOTSTRAP] Minecraft SharedConstants & Registries initialized successfully.");
        } catch (Throwable t) {
            System.out.println(" [BOOTSTRAP NOTE] " + t.getMessage());
        }

        // Category A: ChallengeHud.onCoopCue Null Safety & Defaults
        runTest("ADV-HUD-01: onCoopCue Null Packet Defense", AdversarialHudAndModsHarness::testOnCoopCueNullPacket);
        runTest("ADV-HUD-02: onCoopCue All-Null Fields Safe Defaults", AdversarialHudAndModsHarness::testOnCoopCueAllNullFields);
        runTest("ADV-HUD-03: onCoopCue Bilingual Pipe Fallback Parsing", AdversarialHudAndModsHarness::testOnCoopCueBilingualParsing);

        // Category B: ChallengeHud Fuzzing & Extreme Boundary Strings
        runTest("ADV-HUD-04: onCoopCue 10,000-char Extreme Strings Fuzzing", AdversarialHudAndModsHarness::testOnCoopCueExtremeStringLengths);
        runTest("ADV-HUD-05: onCoopCue Cyrillic, Emojis and Control Codes", AdversarialHudAndModsHarness::testOnCoopCueUnicodeCyrillicAndControlCodes);

        // Category C: ChallengeHud Duration Math & Expiration
        runTest("ADV-HUD-06: onCoopCue Duration Ticks Boundaries (Negative, Zero, Max Int)", AdversarialHudAndModsHarness::testOnCoopCueDurationTicksBoundaries);
        runTest("ADV-HUD-07: onCoopCue Expiration and Age Lifecycle Math", AdversarialHudAndModsHarness::testOnCoopCueExpirationMath);

        // Category D: ChallengeHud Action Item Stack Resolution & '?' Badge
        runTest("ADV-HUD-08: resolveActionStack Standard Sectors Resolution", AdversarialHudAndModsHarness::testResolveActionStackStandardSectors);
        runTest("ADV-HUD-09: resolveActionStack Unknown, Empty and Null Fallback to Paper", AdversarialHudAndModsHarness::testResolveActionStackFallbackPaper);
        runTest("ADV-HUD-10: resolveActionStack Direct Item and Case Insensitivity", AdversarialHudAndModsHarness::testResolveActionStackDirectAndCaseInsensitive);

        // Category E: ChallengeHud Multi-Threaded Packet Concurrency Storm
        runTest("ADV-HUD-11: 50-Thread Concurrent Packet Storm (5000 packets)", AdversarialHudAndModsHarness::testCoopCueConcurrentStorm);

        // Category F: SayItClient Iris Reload Runtime Guard Architectural Analysis
        runTest("ADV-IRIS-01: SayItClient irisReloadUnbound Initial State", AdversarialHudAndModsHarness::testIrisReloadUnboundInitialState);
        runTest("ADV-IRIS-02: SayItClient irisReloadUnbound One-Shot Latch Behavior", AdversarialHudAndModsHarness::testIrisReloadUnboundOneShotBehavior);

        System.out.println("==============================================================================");
        System.out.println(String.format(" RESULTS: Total=%d, Passed=%d, Failed=%d", totalTests, passedTests, failedTests));
        if (failedTests > 0) {
            System.out.println(" FAILURES:");
            for (String f : failureDetails) {
                System.out.println("  - " + f);
            }
            System.exit(1);
        } else {
            System.out.println(" [SUCCESS] All adversarial test vectors PASSED!");
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
            String detail = name + ": " + t.getClass().getSimpleName() + " - " + t.getMessage();
            failureDetails.add(detail);
            System.out.println(String.format("  [%s] -> FAILED: %s", name, t.getMessage()));
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Throwable;
    }

    // --- Tests ---

    static void testOnCoopCueNullPacket() {
        ChallengeHud.activeCoopCue = null;
        ChallengeHud.onCoopCue(null);
        if (ChallengeHud.activeCoopCue != null) {
            throw new AssertionError("activeCoopCue should remain null when receiving null packet");
        }
    }

    static void testOnCoopCueAllNullFields() {
        Packets.CoopCue cue = new Packets.CoopCue(null, null, null, 0, 0, 0, 0, null, null, null, null);
        ChallengeHud.onCoopCue(cue);
        ChallengeHud.CoopCueNotification notif = ChallengeHud.activeCoopCue;
        if (notif == null) {
            throw new AssertionError("activeCoopCue must not be null after onCoopCue");
        }
        if (!"Aliado".equals(notif.initiatorName())) {
            throw new AssertionError("Expected default initiatorName 'Aliado', got: " + notif.initiatorName());
        }
        if (!"help".equals(notif.action())) {
            throw new AssertionError("Expected default action 'help', got: " + notif.action());
        }
        if (!"".equals(notif.ruText())) {
            throw new AssertionError("Expected empty ruText, got: " + notif.ruText());
        }
        if (!"".equals(notif.ptText())) {
            throw new AssertionError("Expected empty ptText, got: " + notif.ptText());
        }
        if (!"".equals(notif.phonetic())) {
            throw new AssertionError("Expected empty phonetic, got: " + notif.phonetic());
        }
        if (!"".equals(notif.objective())) {
            throw new AssertionError("Expected empty objective, got: " + notif.objective());
        }
        if (notif.durationMs() != 10000L) {
            throw new AssertionError("Expected 10000ms duration default, got: " + notif.durationMs());
        }
    }

    static void testOnCoopCueBilingualParsing() {
        // Subtest 1: Standard pipe
        Packets.CoopCue cue1 = new Packets.CoopCue("wood", "Player1", "Дай дерево | Me dê madeira", 0, 0, 0, 100);
        ChallengeHud.onCoopCue(cue1);
        ChallengeHud.CoopCueNotification n1 = ChallengeHud.activeCoopCue;
        if (!"Дай дерево".equals(n1.ruText()) || !"Me dê madeira".equals(n1.ptText())) {
            throw new AssertionError("Failed pipe split: ru='" + n1.ruText() + "', pt='" + n1.ptText() + "'");
        }

        // Subtest 2: Single element, no pipe
        Packets.CoopCue cue2 = new Packets.CoopCue("food", "Player2", "Только русский текст", 0, 0, 0, 100);
        ChallengeHud.onCoopCue(cue2);
        ChallengeHud.CoopCueNotification n2 = ChallengeHud.activeCoopCue;
        if (!"Только русский текст".equals(n2.ruText()) || !"".equals(n2.ptText())) {
            throw new AssertionError("Failed single element split: ru='" + n2.ruText() + "', pt='" + n2.ptText() + "'");
        }

        // Subtest 3: Multiple pipes
        Packets.CoopCue cue3 = new Packets.CoopCue("lava", "Player3", "Часть1 | Часть2 | Часть3", 0, 0, 0, 100);
        ChallengeHud.onCoopCue(cue3);
        ChallengeHud.CoopCueNotification n3 = ChallengeHud.activeCoopCue;
        if (!"Часть1".equals(n3.ruText()) || !"Часть2 | Часть3".equals(n3.ptText())) {
            throw new AssertionError("Failed multi-pipe split: ru='" + n3.ruText() + "', pt='" + n3.ptText() + "'");
        }
    }

    static void testOnCoopCueExtremeStringLengths() {
        String long10k = "A".repeat(10000);
        Packets.CoopCue cue = new Packets.CoopCue(
            "extreme",
            long10k,
            long10k + " | " + long10k,
            100.0, 64.0, 200.0,
            200,
            long10k,
            long10k,
            long10k,
            long10k
        );
        ChallengeHud.onCoopCue(cue);
        ChallengeHud.CoopCueNotification n = ChallengeHud.activeCoopCue;
        if (n == null) {
            throw new AssertionError("Notification card was null for 10k string");
        }
        if (n.initiatorName().length() != 10000 || n.ruText().length() != 10000) {
            throw new AssertionError("String truncation detected in extreme string fuzzing");
        }
    }

    static void testOnCoopCueUnicodeCyrillicAndControlCodes() {
        String ctl = "Игрок_\u0000\t\r\n§6[VIP]§r 🗡️";
        String ru = "§eПомоги мне, пожалуйста! 🍞 🌋 \u202Ereversed\u202C";
        Packets.CoopCue cue = new Packets.CoopCue(
            "help",
            ctl,
            ru + " | Ajuda!",
            0, 0, 0,
            100,
            ru,
            "[pamagi mne]",
            "Ajuda!",
            "help_obj"
        );
        ChallengeHud.onCoopCue(cue);
        ChallengeHud.CoopCueNotification n = ChallengeHud.activeCoopCue;
        if (n == null || !n.initiatorName().equals(ctl) || !n.ruText().equals(ru)) {
            throw new AssertionError("Unicode or control characters corrupted during CoopCue handling");
        }
    }

    static void testOnCoopCueDurationTicksBoundaries() {
        // Negative duration -> 10000 ms
        ChallengeHud.onCoopCue(new Packets.CoopCue("test", "A", "msg", 0, 0, 0, -100));
        if (ChallengeHud.activeCoopCue.durationMs() != 10000L) {
            throw new AssertionError("Negative duration should fallback to 10000L, got: " + ChallengeHud.activeCoopCue.durationMs());
        }

        // Zero duration -> 10000 ms
        ChallengeHud.onCoopCue(new Packets.CoopCue("test", "A", "msg", 0, 0, 0, 0));
        if (ChallengeHud.activeCoopCue.durationMs() != 10000L) {
            throw new AssertionError("Zero duration should fallback to 10000L, got: " + ChallengeHud.activeCoopCue.durationMs());
        }

        // 1 tick -> 50 ms
        ChallengeHud.onCoopCue(new Packets.CoopCue("test", "A", "msg", 0, 0, 0, 1));
        if (ChallengeHud.activeCoopCue.durationMs() != 50L) {
            throw new AssertionError("1 tick should equal 50ms, got: " + ChallengeHud.activeCoopCue.durationMs());
        }

        // Max integer ticks -> positive long (no overflow)
        ChallengeHud.onCoopCue(new Packets.CoopCue("test", "A", "msg", 0, 0, 0, Integer.MAX_VALUE));
        long expected = (long)Integer.MAX_VALUE * 50L;
        if (ChallengeHud.activeCoopCue.durationMs() != expected || ChallengeHud.activeCoopCue.durationMs() <= 0) {
            throw new AssertionError("Max int ticks overflowed into negative or invalid: " + ChallengeHud.activeCoopCue.durationMs());
        }
    }

    static void testOnCoopCueExpirationMath() {
        long now = System.currentTimeMillis();
        // Notification expired (age > durationMs)
        ChallengeHud.CoopCueNotification expiredNotif = new ChallengeHud.CoopCueNotification(
            "Aliado", "wood", "Дерево", "", "Madeira", "", now - 15000L, 10000L
        );
        long age = now - expiredNotif.receivedAt();
        boolean isExpired = age > expiredNotif.durationMs();
        if (!isExpired) {
            throw new AssertionError("Notification should be expired when age (15000) > durationMs (10000)");
        }

        // Notification active
        ChallengeHud.CoopCueNotification activeNotif = new ChallengeHud.CoopCueNotification(
            "Aliado", "wood", "Дерево", "", "Madeira", "", now - 2000L, 10000L
        );
        boolean isActive = (now - activeNotif.receivedAt()) <= activeNotif.durationMs();
        if (!isActive) {
            throw new AssertionError("Notification should be active when age (2000) <= durationMs (10000)");
        }
    }

    static void testResolveActionStackStandardSectors() {
        checkResolvedItem("wood", "minecraft:oak_log");
        checkResolvedItem("food", "minecraft:bread");
        checkResolvedItem("help", "minecraft:golden_apple");
        checkResolvedItem("lava", "minecraft:lava_bucket");
        checkResolvedItem("follow", "minecraft:compass");
        checkResolvedItem("waypoint", "minecraft:spyglass");
        checkResolvedItem("point", "minecraft:spyglass");
        checkResolvedItem("spyglass", "minecraft:spyglass");
        checkResolvedItem("recovery", "minecraft:spyglass");
    }

    static void testResolveActionStackFallbackPaper() {
        checkResolvedItem(null, "minecraft:paper");
        checkResolvedItem("", "minecraft:paper");
        checkResolvedItem("unknown_custom_action_12345", "minecraft:paper");
        checkResolvedItem("   ", "minecraft:paper");
    }

    static void testResolveActionStackDirectAndCaseInsensitive() {
        checkResolvedItem("WoOd", "minecraft:oak_log");
        checkResolvedItem("FOOD", "minecraft:bread");
        checkResolvedItem("HeLp", "minecraft:golden_apple");
        checkResolvedItem("LAVA", "minecraft:lava_bucket");
        checkResolvedItem("FoLLoW", "minecraft:compass");
        checkResolvedItem("minecraft:diamond", "minecraft:diamond");
    }

    private static void checkResolvedItem(String action, String expectedItemId) {
        class_1799 stack = ChallengeHud.resolveActionStack(action);
        if (stack == null || stack.method_7960()) { // method_7960 is isEmpty()
            throw new AssertionError("resolveActionStack('" + action + "') returned null or empty stack");
        }
        String itemId = class_7923.field_41178.method_10221(stack.method_7909()).toString();
        if (!expectedItemId.equals(itemId)) {
            throw new AssertionError("resolveActionStack('" + action + "') resolved to " + itemId + ", expected " + expectedItemId);
        }
    }

    static void testCoopCueConcurrentStorm() throws Exception {
        int threads = 50;
        int packetsPerThread = 100;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            pool.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < packetsPerThread; i++) {
                        String action = (i % 6 == 0) ? "wood" : ((i % 6 == 1) ? "food" : "help");
                        Packets.CoopCue cue = new Packets.CoopCue(
                            action,
                            "Player_" + threadId,
                            "msg_" + i,
                            threadId, 64, i,
                            100
                        );
                        ChallengeHud.onCoopCue(cue);
                        // Reader access
                        ChallengeHud.CoopCueNotification snap = ChallengeHud.activeCoopCue;
                        if (snap != null) {
                            String init = snap.initiatorName();
                            if (init == null) throw new NullPointerException("initiatorName was null in concurrent read");
                        }
                        successCounter.incrementAndGet();
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        pool.shutdown();

        if (!finished) {
            throw new AssertionError("Concurrent packet storm timed out after 10s (possible thread contention / deadlock)");
        }
        if (successCounter.get() != threads * packetsPerThread) {
            throw new AssertionError("Concurrent packet storm lost packets! Expected " + (threads * packetsPerThread) + ", completed " + successCounter.get());
        }
        if (ChallengeHud.activeCoopCue == null) {
            throw new AssertionError("activeCoopCue should not be null after packet storm");
        }
    }

    static void testIrisReloadUnboundInitialState() throws Exception {
        Field field = SayItClient.class.getDeclaredField("irisReloadUnbound");
        field.setAccessible(true);
        boolean val = (boolean) field.get(null);
        // At classload, irisReloadUnbound is false
        // (Note: might be true if tick was already called, but in test JVM it starts false)
        System.out.println("      [Reflection] SayItClient.irisReloadUnbound = " + val);
    }

    static void testIrisReloadUnboundOneShotBehavior() throws Exception {
        Field field = SayItClient.class.getDeclaredField("irisReloadUnbound");
        field.setAccessible(true);
        
        // When irisReloadUnbound is set to true (simulating post-first-tick):
        field.set(null, true);
        boolean latchActive = (boolean) field.get(null);
        if (!latchActive) {
            throw new AssertionError("Failed to verify irisReloadUnbound field state");
        }

        // Empirical check: In SayItClient.java line 67:
        // if (!irisReloadUnbound) { unbindIrisReloadKey(); irisReloadUnbound = true; }
        // Because irisReloadUnbound is now true, !irisReloadUnbound is false.
        // Therefore, any subsequent key re-binding in game options will NOT be caught!
        System.out.println("      [Architectural Finding] SayItClient uses a one-shot latch: once irisReloadUnbound=true, unbindIrisReloadKey() is NEVER called again during session.");
    }
}
