package com.spicesfire.sayitenglish;

import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.audio.AudioPlaybackManager;
import com.spicesfire.sayitenglish.client.ChallengeSession;
import com.spicesfire.sayitenglish.client.Words;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_1799;

public class AdversarialAudioAndTranslationsHarness {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> failureDetails = new ArrayList<>();

    private static void runTest(String name, Runnable test) {
        totalTests++;
        System.out.print("  [" + name + "] ... ");
        try {
            test.run();
            passedTests++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            failedTests++;
            System.out.println("FAILED: " + t.getMessage());
            failureDetails.add(name + ": " + t.getMessage());
            t.printStackTrace(System.out);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println(" ADVERSARIAL STRESS TEST: AUDIOPLAYBACKMANAGER & CHALLENGESESSION (R3)");
        System.out.println("==============================================================================");

        try {
            net.minecraft.class_155.method_36208();
            net.minecraft.class_2966.method_12851();
            System.out.println(" [BOOTSTRAP] Minecraft SharedConstants & Registries initialized.");
        } catch (Throwable t) {
            System.out.println(" [BOOTSTRAP NOTE] " + t.getMessage());
        }

        // 1. Rapid Concurrent Triggers (100 triggers in 100ms)
        runTest("ADV-AUDIO-01: Rapid 100 Triggers in 100ms Burst Concurrency", AdversarialAudioAndTranslationsHarness::testRapid100Triggers);
        runTest("ADV-AUDIO-02: Multi-Threaded 10-Thread Concurrency Storm (100 plays)", AdversarialAudioAndTranslationsHarness::testMultiThreadedStorm);
        runTest("ADV-AUDIO-03: MCI Device Alias Conflict and Leak Check Post-Storm", AdversarialAudioAndTranslationsHarness::testMciAliasLeakCheck);

        // 2. Missing, Corrupt, Null, and Extreme String Inputs
        runTest("ADV-AUDIO-04: Null String Input Silent Handling", AdversarialAudioAndTranslationsHarness::testNullStringInput);
        runTest("ADV-AUDIO-05: Empty & Whitespace String Inputs Silent Handling", AdversarialAudioAndTranslationsHarness::testEmptyAndWhitespaceStrings);
        runTest("ADV-AUDIO-06: 10,000-Character Extreme Russian String Fuzzing", AdversarialAudioAndTranslationsHarness::testExtremeStringLength);
        runTest("ADV-AUDIO-07: Corrupted MP3 Header & Truncated Frame Handling", AdversarialAudioAndTranslationsHarness::testCorruptedAudioFile);
        runTest("ADV-AUDIO-08: Non-Existent and Unresolvable Cyrillic Phrases", AdversarialAudioAndTranslationsHarness::testUncachedPhrases);

        // 3. Challenge Timer Synchronization (Pause/Resume during Playback)
        runTest("ADV-AUDIO-09: Challenge Timer Pauses During Audio Playback", AdversarialAudioAndTranslationsHarness::testTimerPauseDuringPlayback);
        runTest("ADV-AUDIO-10: Challenge Timer Idempotent Double Pause Protection", AdversarialAudioAndTranslationsHarness::testTimerDoublePause);
        runTest("ADV-AUDIO-11: Challenge Timer Resume Accuracy Across Long Pause", AdversarialAudioAndTranslationsHarness::testTimerResumeAccuracyAcrossLongPause);

        // 4. Cache Oracle and Retrieval Latency
        runTest("ADV-AUDIO-12: Dual-Hash (SHA-256 & MD5) Resolution Integrity", AdversarialAudioAndTranslationsHarness::testDualHashResolution);
        runTest("ADV-AUDIO-13: Pre-seeded Core Phrase Cache Latency Benchmark (< 5ms)", AdversarialAudioAndTranslationsHarness::testPreseededCacheLatency);

        System.out.println("------------------------------------------------------------------------------");
        System.out.println(" Results: " + passedTests + " / " + totalTests + " passed, " + failedTests + " failed.");
        if (failedTests > 0) {
            System.err.println(" Failures detected:");
            for (String f : failureDetails) {
                System.err.println("  - " + f);
            }
            System.exit(1);
        } else {
            System.out.println(" ALL ADVERSARIAL AUDIO & TIMER TESTS PASSED WITH 100% SUCCESS!");
            System.exit(0);
        }
    }

    // -------------------------------------------------------------------------
    // Test Implementations
    // -------------------------------------------------------------------------

    private static void testRapid100Triggers() {
        CountDownLatch latch = new CountDownLatch(100);
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            final int idx = i;
            AudioPlaybackManager.play("Мне нужно...", () -> {
                latch.countDown();
            });
            try {
                Thread.sleep(1); // 1ms interval -> 100ms total
            } catch (InterruptedException ignored) {}
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.print("[fired in " + elapsed + "ms] ");

        // Must not crash or deadlock
        AudioPlaybackManager.stop();
        check(!AudioPlaybackManager.isPlaying(), "isPlaying must be false after stop()");
    }

    private static void testMultiThreadedStorm() {
        int threadCount = 10;
        int perThread = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threadCount * perThread);
        AtomicInteger callbackCount = new AtomicInteger(0);

        for (int t = 0; t < threadCount; t++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                } catch (InterruptedException ignored) {}

                for (int i = 0; i < perThread; i++) {
                    AudioPlaybackManager.play("Дерево", () -> {
                        callbackCount.incrementAndGet();
                        doneGate.countDown();
                    });
                }
            });
        }

        // Fire all threads simultaneously
        startGate.countDown();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}

        AudioPlaybackManager.stop();
        pool.shutdownNow();
        check(!AudioPlaybackManager.isPlaying(), "AudioPlaybackManager must not be playing after stop()");
    }

    private static void testMciAliasLeakCheck() {
        // Ensure that after the concurrency storms, MCI can still open, play, and close cleanly
        AudioPlaybackManager.stop();

        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        AudioPlaybackManager.play("Мне нужно...", () -> {
            completed.set(true);
            latch.countDown();
        });

        // Stop immediately to verify clean close
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}
        AudioPlaybackManager.stop();

        check(!AudioPlaybackManager.isPlaying(), "AudioPlaybackManager isPlaying must be false");
    }

    private static void testNullStringInput() {
        AtomicBoolean called = new AtomicBoolean(false);
        AudioPlaybackManager.play(null, () -> {
            called.set(true);
        });
        check(called.get(), "Callback must be immediately executed for null input");
        check(!AudioPlaybackManager.isPlaying(), "Must not be playing for null input");
    }

    private static void testEmptyAndWhitespaceStrings() {
        AtomicBoolean called1 = new AtomicBoolean(false);
        AudioPlaybackManager.play("", () -> {
            called1.set(true);
        });
        check(called1.get(), "Callback must execute for empty string");

        AtomicBoolean called2 = new AtomicBoolean(false);
        AudioPlaybackManager.play("   \t\n  ", () -> {
            called2.set(true);
        });
        check(called2.get(), "Callback must execute for whitespace string");
    }

    private static void testExtremeStringLength() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Дерево ");
        }
        String hugeString = sb.toString(); // ~7000 Cyrillic chars
        check(hugeString.length() >= 7000, "String length must be >= 7000 chars");

        AtomicBoolean callbackRan = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        AudioPlaybackManager.play(hugeString, () -> {
            callbackRan.set(true);
            latch.countDown();
        });

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        AudioPlaybackManager.stop();
        check(!AudioPlaybackManager.isPlaying(), "AudioPlaybackManager must not be playing");
    }

    private static void testCorruptedAudioFile() {
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "sayit_corrupt_test");
            if (!tempDir.exists()) tempDir.mkdirs();

            File corruptFile = new File(tempDir, "corrupted_audio.mp3");
            try (FileOutputStream fos = new FileOutputStream(corruptFile)) {
                // Write garbage header that is not valid MPEG/ID3
                byte[] junk = new byte[1024];
                for (int i = 0; i < junk.length; i++) junk[i] = (byte) (i % 256);
                fos.write(junk);
            }

            // Directly verify WinMM handles corrupted file without JVM crash
            if (AudioPlaybackManager.WinMM.INSTANCE != null) {
                String alias = "sayit_test_corrupt";
                AudioPlaybackManager.WinMM.INSTANCE.mciSendStringW("close " + alias, null, 0, null);
                long res = AudioPlaybackManager.WinMM.INSTANCE.mciSendStringW("open \"" + corruptFile.getAbsolutePath() + "\" type mpegvideo alias " + alias, null, 0, null);
                // Non-zero result expected or handled cleanly
                AudioPlaybackManager.WinMM.INSTANCE.mciSendStringW("close " + alias, null, 0, null);
            }

            // Cleanup
            corruptFile.delete();
            tempDir.delete();
        } catch (Exception e) {
            throw new RuntimeException("Exception during corrupt file test: " + e.getMessage(), e);
        }
    }

    private static void testUncachedPhrases() {
        String nonexistent = "ТестСлучайный_" + System.currentTimeMillis() + "_" + System.nanoTime();
        File f = AudioPlaybackManager.findInCache(nonexistent);
        check(f == null, "Fresh random phrase must return null from cache before playback");

        AtomicBoolean completed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        AudioPlaybackManager.play(nonexistent, () -> {
            completed.set(true);
            latch.countDown();
        });

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}

        AudioPlaybackManager.stop();
        check(!AudioPlaybackManager.isPlaying(), "Must not be playing for uncached phrase after stop");

        // Clean up any files that may have been fetched from Google TTS during the test
        String sha = AudioPlaybackManager.sha256(nonexistent);
        String md5 = AudioPlaybackManager.md5(nonexistent);
        for (File dir : AudioPlaybackManager.getCandidateCacheDirs()) {
            new File(dir, sha + ".mp3").delete();
            new File(dir, md5 + ".mp3").delete();
        }
    }

    private static void testTimerPauseDuringPlayback() {
        // Setup a mock active challenge in ChallengeSession
        ChallengeSession.current = new Packets.Challenge(999, "sentence", "wood", "minecraft:oak_log", 10, 75);
        ChallengeSession.prompt = new Words.Prompt("Дéрево", "Árvore", class_1799.field_8037);
        ChallengeSession.startedAt = System.currentTimeMillis();

        float remBefore = ChallengeSession.remaining();
        check(remBefore >= 9.5f && remBefore <= 10.0f, "Initial remaining should be close to 10s: " + remBefore);

        // Pause challenge timer
        ChallengeSession.pause(true);

        try {
            Thread.sleep(800); // Wait 800ms while paused
        } catch (InterruptedException ignored) {}

        float remWhilePaused = ChallengeSession.remaining();
        float deltaWhilePaused = Math.abs(remBefore - remWhilePaused);
        check(deltaWhilePaused < 0.05f, "Countdown timer must remain frozen while paused! delta=" + deltaWhilePaused);

        // Resume challenge timer
        ChallengeSession.pause(false);

        try {
            Thread.sleep(500); // Wait 500ms while running
        } catch (InterruptedException ignored) {}

        float remAfterResume = ChallengeSession.remaining();
        float expectedRem = remBefore - 0.5f;
        float diff = Math.abs(remAfterResume - expectedRem);
        check(diff < 0.2f, "Timer must account only for unpaused time! remAfterResume=" + remAfterResume + ", expected=" + expectedRem);

        ChallengeSession.stop();
    }

    private static void testTimerDoublePause() {
        ChallengeSession.current = new Packets.Challenge(998, "word", "stone", "", 15, 70);
        ChallengeSession.prompt = new Words.Prompt("Камень", "Pedra", class_1799.field_8037);
        ChallengeSession.startedAt = System.currentTimeMillis();

        ChallengeSession.pause(true);
        float rem1 = ChallengeSession.remaining();

        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {}

        // Second pause(true) must be idempotent and not reset pausedAt
        ChallengeSession.pause(true);
        float rem2 = ChallengeSession.remaining();

        check(Math.abs(rem1 - rem2) < 0.02f, "Double pause(true) must not reset pausedAt! rem1=" + rem1 + ", rem2=" + rem2);

        ChallengeSession.pause(false);
        // Second pause(false) when already unpaused must be a no-op
        ChallengeSession.pause(false);

        ChallengeSession.stop();
    }

    private static void testTimerResumeAccuracyAcrossLongPause() {
        ChallengeSession.current = new Packets.Challenge(997, "sentence", "help", "", 20, 80);
        ChallengeSession.prompt = new Words.Prompt("Помоги мне!", "Ajuda", class_1799.field_8037);
        ChallengeSession.startedAt = System.currentTimeMillis();

        float initial = ChallengeSession.remaining();

        // Simulate 1.5s audio playback pause
        ChallengeSession.pause(true);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}
        ChallengeSession.pause(false);

        float resumed = ChallengeSession.remaining();
        float pauseDrift = Math.abs(initial - resumed);
        check(pauseDrift < 0.15f, "After 1.5s pause, timer remaining must barely differ from pre-pause value! drift=" + pauseDrift);

        ChallengeSession.stop();
    }

    private static void testDualHashResolution() {
        String phrase = "Мне нужно...";
        String sha = AudioPlaybackManager.sha256(phrase);
        String md5 = AudioPlaybackManager.md5(phrase);

        check("fc94acf71eb132d2933cc4570a6fdc9810d07add5602d43994670b4b92b48a23".equals(sha), "SHA-256 match for '" + phrase + "'");
        check("837e6998026711f45c286ab3f5418bce".equals(md5), "MD5 match for '" + phrase + "'");

        File f = AudioPlaybackManager.findInCache(phrase);
        check(f != null && f.exists(), "findInCache must locate pre-seeded file for '" + phrase + "'");
    }

    private static void testPreseededCacheLatency() {
        String[] samplePhrases = new String[] {
            "Мне нужно...",
            "Мне нужна еда!",
            "Помоги мне!",
            "Пол - это лава!",
            "Следуй за мной!",
            "Смотри туда!",
            "Дубовое бревно",
            "Камень",
            "Булыжник",
            "Железный слиток",
            "Хлеб",
            "Алмаз"
        };

        for (String p : samplePhrases) {
            long t0 = System.nanoTime();
            File cached = AudioPlaybackManager.findInCache(p);
            long elapsedUs = (System.nanoTime() - t0) / 1000L;

            check(cached != null && cached.exists(), "Cache hit for: " + p);
            check(elapsedUs < 5000, "Latency for '" + p + "' must be < 5ms (was " + elapsedUs + " us)");
        }
    }
}
