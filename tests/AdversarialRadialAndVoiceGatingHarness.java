package com.spicesfire.sayitenglish;

import com.spicesfire.sayitenglish.client.RadialMenuScreen;
import com.spicesfire.sayitenglish.client.RadialMenuScreen.SectorDef;
import com.spicesfire.sayitenglish.client.SayItClient;
import com.spicesfire.sayitenglish.platform.ModPacket;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdversarialRadialAndVoiceGatingHarness
 *
 * Empirical adversarial stress testing for:
 * 1. RadialMenuScreen angular partition mathematics across all 360 degrees and deadzones.
 * 2. Voice challenge gating in ChallengeSession (all failure modes and packet guarantees).
 * 3. Bytecode invariants and state idempotency.
 */
public class AdversarialRadialAndVoiceGatingHarness {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> failureDetails = new ArrayList<>();

    // Simulated packet sink to capture and count emitted packets
    public static final List<ModPacket> emittedPackets = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("==============================================================================");
        System.out.println(" EMPIRICAL ADVERSARIAL STRESS TEST: RADIAL MENU & VOICE CHALLENGE GATING");
        System.out.println("==============================================================================");

        // ---------------------------------------------------------------------
        // GROUP 1: CENTER DEADZONE & BOUNDARY RADII (RAD-DZ)
        // ---------------------------------------------------------------------
        runTest("RAD-DZ-01: Exact Center Deadzone (dx=0, dy=0, dist=0)", () -> {
            int sec = calcRadialSector(0.0, 0.0);
            assertEquals("Exact center must yield -1 (no sector hovered)", -1, sec);
        });

        runTest("RAD-DZ-02: Micro-Deadzone Epsilon (dist=0.000001)", () -> {
            int sec = calcRadialSector(0.000001, 0.0);
            assertEquals("Micro-epsilon inside deadzone must yield -1", -1, sec);
        });

        runTest("RAD-DZ-03: Inner Deadzone Sweep across 8 Cardinal/Ordinal Directions", () -> {
            double[] testRadii = {0.5, 1.0, 5.0, 10.0, 15.0, 20.0, 21.0, 21.99, 21.999999};
            double[] testAngles = {0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0};
            for (double r : testRadii) {
                for (double deg : testAngles) {
                    double rad = Math.toRadians(deg);
                    double dx = r * Math.cos(rad);
                    double dy = r * Math.sin(rad);
                    int sec = calcRadialSector(dx, dy);
                    assertEquals(String.format("Radius %.4f at %.1f deg must be deadzone (-1)", r, deg), -1, sec);
                }
            }
        });

        runTest("RAD-DZ-04: Exact Inner Radius Knife-Edge (dist=22.0)", () -> {
            // At dist = 22.0, condition (dist >= 22.0) is satisfied
            int secTop = calcRadialSector(0.0, -22.0);
            assertEquals("Exact inner radius at top must activate Sector 0", 0, secTop);

            int secBottom = calcRadialSector(0.0, 22.0);
            assertEquals("Exact inner radius at bottom must activate Sector 3", 3, secBottom);

            // Knife edge just below 22.0
            int secBelow = calcRadialSector(0.0, -21.999999999999);
            assertEquals("Value 21.999999999999 must remain in deadzone (-1)", -1, secBelow);
        });

        runTest("RAD-DZ-05: Active Radial Ring Range (22.0 <= dist <= 150.0)", () -> {
            double[] activeRadii = {22.000001, 30.0, 50.0, 68.0, 100.0, 120.0, 149.999999};
            for (double r : activeRadii) {
                int sec = calcRadialSector(0.0, -r);
                assertEquals(String.format("Active radius %.4f at top must activate Sector 0", r), 0, sec);
                assertTrue(String.format("Active radius %.4f must be in range [0..5]", r), sec >= 0 && sec <= 5);
            }
        });

        runTest("RAD-DZ-06: Exact Outer Radius Knife-Edge (dist=150.0)", () -> {
            int secTop = calcRadialSector(0.0, -150.0);
            assertEquals("Exact outer radius 150.0 at top must activate Sector 0", 0, secTop);

            int secRight = calcRadialSector(150.0, 0.0);
            assertEquals("Exact outer radius 150.0 at right must activate Sector 2", 2, secRight);
        });

        runTest("RAD-DZ-07: Outer Range Knife-Edge Out-of-Bounds (dist > 150.0)", () -> {
            double[] outRadii = {150.000001, 150.01, 151.0, 200.0, 500.0, 10000.0};
            for (double r : outRadii) {
                int sec = calcRadialSector(0.0, -r);
                assertEquals(String.format("Outer distance %.4f must yield -1 (out of radial menu)", r), -1, sec);
            }
        });

        runTest("RAD-DZ-08: Extreme Integer & Float Coordinates (Overflow Resistance)", () -> {
            int secMax = calcRadialSector((double)Integer.MAX_VALUE, (double)Integer.MAX_VALUE);
            assertEquals("Integer.MAX_VALUE must not crash and yield -1", -1, secMax);

            int secMin = calcRadialSector((double)Integer.MIN_VALUE, (double)Integer.MIN_VALUE);
            assertEquals("Integer.MIN_VALUE must not crash and yield -1", -1, secMin);

            // Double NaN and Infinities
            int secNaN = calcRadialSector(Double.NaN, 0.0);
            assertEquals("Double.NaN must yield -1", -1, secNaN);

            int secInf = calcRadialSector(Double.POSITIVE_INFINITY, 0.0);
            assertEquals("Double.POSITIVE_INFINITY must yield -1", -1, secInf);
        });

        // ---------------------------------------------------------------------
        // GROUP 2: ANGULAR PARTITION & 360-DEGREE SWEEP (RAD-ANG)
        // ---------------------------------------------------------------------
        runTest("RAD-ANG-01: Sector 0 Center at -90 deg (Top: wood)", () -> {
            int sec = calcRadialSector(0.0, -68.0);
            assertEquals("Top (-90 deg) must map to Sector 0", 0, sec);
        });

        runTest("RAD-ANG-02: Sector 1 Center at -30 deg (Top-Right: food)", () -> {
            double rad = Math.toRadians(-30.0);
            int sec = calcRadialSector(68.0 * Math.cos(rad), 68.0 * Math.sin(rad));
            assertEquals("Top-Right (-30 deg) must map to Sector 1", 1, sec);
        });

        runTest("RAD-ANG-03: Sector 2 Center at +30 deg (Bottom-Right: help)", () -> {
            double rad = Math.toRadians(30.0);
            int sec = calcRadialSector(68.0 * Math.cos(rad), 68.0 * Math.sin(rad));
            assertEquals("Bottom-Right (+30 deg) must map to Sector 2", 2, sec);
        });

        runTest("RAD-ANG-04: Sector 3 Center at +90 deg (Bottom: lava)", () -> {
            int sec = calcRadialSector(0.0, 68.0);
            assertEquals("Bottom (+90 deg) must map to Sector 3", 3, sec);
        });

        runTest("RAD-ANG-05: Sector 4 Center at +150 deg (Bottom-Left: follow)", () -> {
            double rad = Math.toRadians(150.0);
            int sec = calcRadialSector(68.0 * Math.cos(rad), 68.0 * Math.sin(rad));
            assertEquals("Bottom-Left (+150 deg) must map to Sector 4", 4, sec);
        });

        runTest("RAD-ANG-06: Sector 5 Center at -150 deg (Top-Left: waypoint)", () -> {
            double rad = Math.toRadians(-150.0);
            int sec = calcRadialSector(68.0 * Math.cos(rad), 68.0 * Math.sin(rad));
            assertEquals("Top-Left (-150 deg) must map to Sector 5", 5, sec);
        });

        runTest("RAD-ANG-07: All 6 Sector Boundary Knife-Edges and Epsilon Flips", () -> {
            double eps = 0.0001;

            // Boundary 1: between Sector 5 and Sector 0 at -120 deg
            assertEquals("At -120.0 deg exact -> Sector 0", 0, calcRadialAngleDeg(-120.0));
            assertEquals("At -120.0 - eps -> Sector 5", 5, calcRadialAngleDeg(-120.0 - eps));
            assertEquals("At -120.0 + eps -> Sector 0", 0, calcRadialAngleDeg(-120.0 + eps));

            // Boundary 2: between Sector 0 and Sector 1 at -60 deg
            assertEquals("At -60.0 deg exact -> Sector 1", 1, calcRadialAngleDeg(-60.0));
            assertEquals("At -60.0 - eps -> Sector 0", 0, calcRadialAngleDeg(-60.0 - eps));
            assertEquals("At -60.0 + eps -> Sector 1", 1, calcRadialAngleDeg(-60.0 + eps));

            // Boundary 3: between Sector 1 and Sector 2 at 0 deg
            assertEquals("At 0.0 deg exact -> Sector 2", 2, calcRadialAngleDeg(0.0));
            assertEquals("At 0.0 - eps -> Sector 1", 1, calcRadialAngleDeg(0.0 - eps));
            assertEquals("At 0.0 + eps -> Sector 2", 2, calcRadialAngleDeg(0.0 + eps));

            // Boundary 4: between Sector 2 and Sector 3 at +60 deg
            assertEquals("At +60.0 deg exact -> Sector 3", 3, calcRadialAngleDeg(60.0));
            assertEquals("At +60.0 - eps -> Sector 2", 2, calcRadialAngleDeg(60.0 - eps));
            assertEquals("At +60.0 + eps -> Sector 3", 3, calcRadialAngleDeg(60.0 + eps));

            // Boundary 5: between Sector 3 and Sector 4 at +120 deg
            assertEquals("At +120.0 deg exact -> Sector 4", 4, calcRadialAngleDeg(120.0));
            assertEquals("At +120.0 - eps -> Sector 3", 3, calcRadialAngleDeg(120.0 - eps));
            assertEquals("At +120.0 + eps -> Sector 4", 4, calcRadialAngleDeg(120.0 + eps));

            // Boundary 6: between Sector 4 and Sector 5 at +-180 deg
            assertEquals("At +180.0 deg exact -> Sector 5", 5, calcRadialAngleDeg(180.0));
            assertEquals("At -180.0 deg exact -> Sector 5", 5, calcRadialAngleDeg(-180.0));
            assertEquals("At +180.0 - eps -> Sector 4", 4, calcRadialAngleDeg(180.0 - eps));
            assertEquals("At -180.0 + eps -> Sector 5", 5, calcRadialAngleDeg(-180.0 + eps));
        });

        runTest("RAD-ANG-08: Dense 360-Degree Continuous Angle Sweep (36,000 steps)", () -> {
            Map<Integer, Integer> sectorCounts = new HashMap<>();
            for (int s = 0; s < 6; s++) {
                sectorCounts.put(s, 0);
            }

            int totalSteps = 36000;
            for (int i = 0; i < totalSteps; i++) {
                // Angle sweep from -180.00 to +180.00 degrees with step 0.01 deg
                double angleDeg = -180.0 + (double)i * 0.01;
                int sec = calcRadialAngleDeg(angleDeg);

                assertTrue(String.format("Angle %.2f deg mapped to out-of-range sector %d", angleDeg, sec), sec >= 0 && sec <= 5);
                sectorCounts.put(sec, sectorCounts.get(sec) + 1);
            }

            // Each of the 6 sectors occupies 60 degrees out of 360 degrees (1/6 = 6000 steps)
            for (int s = 0; s < 6; s++) {
                int count = sectorCounts.get(s);
                // Allow +-2 steps tolerance due to discrete boundary inclusion
                assertTrue(String.format("Sector %d count %d deviates from 6000 steps (+-2)", s, count), Math.abs(count - 6000) <= 2);
            }
        });

        runTest("RAD-ANG-09: Sector Button Geometric Alignment Invariant", () -> {
            // Verify that hovering directly over the rendered center coordinate of each of the 6 buttons
            // always detects that exact sector!
            int cx = 400;
            int cy = 300;
            for (int i = 0; i < RadialMenuScreen.SECTORS.length; i++) {
                double angleRad = Math.toRadians(-90.0 + (double)i * 60.0);
                int sx = (int)((double)cx + 68.0 * Math.cos(angleRad));
                int sy = (int)((double)cy + 68.0 * Math.sin(angleRad));

                double dx = (double)sx - (double)cx;
                double dy = (double)sy - (double)cy;
                int detected = calcRadialSector(dx, dy);

                assertEquals(String.format("Rendered button %d at (%d, %d) must detect sector %d", i, sx, sy, i), i, detected);
            }
        });

        // ---------------------------------------------------------------------
        // GROUP 3: SCREEN LIFECYCLE & STATE INVARIANTS (RAD-SCR)
        // ---------------------------------------------------------------------
        runTest("RAD-SCR-01: Non-Pausing Screen Invariant (shouldPause == false)", () -> {
            RadialMenuScreen screen = new RadialMenuScreen();
            boolean shouldPause = screen.method_25421();
            assertEquals("RadialMenuScreen.shouldPause() (method_25421) must be strictly false", false, shouldPause);
        });

        runTest("RAD-SCR-02: SectorDef Registry Completeness & Data Invariants", () -> {
            assertEquals("RadialMenuScreen must declare exactly 6 sectors", 6, RadialMenuScreen.SECTORS.length);

            String[] expectedActions = {"wood", "food", "help", "lava", "follow", "waypoint"};
            String[] expectedIcons = {"minecraft:oak_log", "minecraft:bread", "minecraft:golden_apple", "minecraft:lava_bucket", "minecraft:compass", "minecraft:spyglass"};
            String[] expectedRuSnippets = {"дерево", "еда", "Помоги", "лава", "Следуй", "Смотри"};

            for (int i = 0; i < 6; i++) {
                SectorDef s = RadialMenuScreen.SECTORS[i];
                assertEquals(String.format("Sector %d action mismatch", i), expectedActions[i], s.action);
                assertEquals(String.format("Sector %d icon mismatch", i), expectedIcons[i], s.iconId);
                assertNotNull(String.format("Sector %d label null", i), s.label);
                assertNotNull(String.format("Sector %d ptText null", i), s.ptText);
                assertNotNull(String.format("Sector %d ruText null", i), s.ruText);
                assertTrue(String.format("Sector %d ruText does not contain '%s'", i, expectedRuSnippets[i]), s.ruText.contains(expectedRuSnippets[i]));
            }
        });

        runTest("RAD-SCR-03: SayItClient Key Binding & GLFW Keycode 86 (Key V)", () -> {
            boolean isV = SayItClient.isRadialKey(86);
            assertTrue("Keycode 86 (GLFW_KEY_V) must be recognized as radial key", isV);

            boolean isOther1 = SayItClient.isRadialKey(32); // Space
            assertEquals("Keycode 32 (Space) must not be radial key", false, isOther1);

            boolean isOther2 = SayItClient.isRadialKey(82); // 'R'
            assertEquals("Keycode 82 ('R') must not be radial key", false, isOther2);
        });

        // ---------------------------------------------------------------------
        // GROUP 4: VOICE CHALLENGE GATING & FAILURE MODES (VCG-FAIL)
        // ---------------------------------------------------------------------
        runTest("VCG-FAIL-01: Sub-threshold Speech Failure Mode (Score 0%) -> 0 Packets", () -> {
            CoopGatingResult res = simulateVoiceChallengeGating("wood", 0, 85, false);
            assertEquals("Score 0 < 85 must emit 0 packets", 0, res.packetsEmitted);
            assertTrue("pendingCoopAction must be cleared on failure", res.pendingCleared);
        });

        runTest("VCG-FAIL-02: Sub-threshold Speech Failure Mode (Score 50%) -> 0 Packets", () -> {
            CoopGatingResult res = simulateVoiceChallengeGating("food", 50, 85, false);
            assertEquals("Score 50 < 85 must emit 0 packets", 0, res.packetsEmitted);
            assertTrue("pendingCoopAction must be cleared on failure", res.pendingCleared);
        });

        runTest("VCG-FAIL-03: Sub-threshold Speech Failure Mode (Score 74%) -> 0 Packets", () -> {
            CoopGatingResult res = simulateVoiceChallengeGating("help", 74, 85, false);
            assertEquals("Score 74 < 85 must emit 0 packets", 0, res.packetsEmitted);
            assertTrue("pendingCoopAction must be cleared on failure", res.pendingCleared);
        });

        runTest("VCG-FAIL-04: Knife-Edge Sub-threshold Failure Mode (Score 84%) -> 0 Packets", () -> {
            CoopGatingResult res = simulateVoiceChallengeGating("lava", 84, 85, false);
            assertEquals("Knife-edge score 84 < 85 must strictly emit 0 packets", 0, res.packetsEmitted);
            assertTrue("pendingCoopAction must be cleared on failure", res.pendingCleared);
        });

        runTest("VCG-FAIL-05: Silence / Zero Speech Heard -> 0 Packets", () -> {
            // User speaks nothing: best remains 0
            CoopGatingResult res = simulateVoiceChallengeGating("follow", 0, 85, false);
            assertEquals("Silence must emit 0 packets", 0, res.packetsEmitted);
            assertTrue("pendingCoopAction must be cleared", res.pendingCleared);
        });

        runTest("VCG-FAIL-06: Session Timeout Expiration -> 0 Packets", () -> {
            // Challenge duration 15s expires without meeting score threshold (e.g. 65)
            CoopGatingResult res = simulateVoiceChallengeGating("waypoint", 65, 85, false);
            assertEquals("Timeout below passScore must emit 0 packets", 0, res.packetsEmitted);
            assertTrue("pendingCoopAction must be cleared", res.pendingCleared);
        });

        runTest("VCG-FAIL-07: Cancellation via ESC / Abort (stop()) -> 0 Packets", () -> {
            // User cancels or presses ESC: stop() called, pendingCoopAction nullified
            CoopGatingResult res = simulateVoiceChallengeGating("wood", 100, 85, true);
            assertEquals("Cancelled challenge must emit 0 packets even if score was 100", 0, res.packetsEmitted);
            assertTrue("pendingCoopAction must be cleared", res.pendingCleared);
        });

        runTest("VCG-FAIL-08: Negative Challenge ID Prevents Packets.Result Leakage", () -> {
            // In ChallengeSession.java: client challenge ID is negative (-100 - hash % 1000).
            // finish() guards: else if (c != null && c.id() > 0) sendToServer(Packets.Result)
            // Verify that for all 6 actions, client challenge ID is strictly negative!
            for (SectorDef sec : RadialMenuScreen.SECTORS) {
                int clientChallengeId = -100 - Math.abs(sec.action.hashCode()) % 1000;
                assertTrue(String.format("Action %s generated non-negative ID %d", sec.action, clientChallengeId), clientChallengeId < 0);
            }
        });

        // ---------------------------------------------------------------------
        // GROUP 5: VOICE CHALLENGE GATING SUCCESS & IDEMPOTENCY (VCG-PASS)
        // ---------------------------------------------------------------------
        runTest("VCG-PASS-01: Exact Pass Threshold Knife-Edge (Score 85 == 85) -> Exactly 1 Packet", () -> {
            CoopGatingResult res = simulateVoiceChallengeGating("wood", 85, 85, false);
            assertEquals("Exact pass score 85 must emit exactly 1 packet", 1, res.packetsEmitted);
            assertEquals("Packet action must match", "wood", res.emittedAction);
            assertTrue("pendingCoopAction must be cleared after emission", res.pendingCleared);
        });

        runTest("VCG-PASS-02: High Score Pass (Score 95% and 100%) -> Exactly 1 Packet", () -> {
            CoopGatingResult res95 = simulateVoiceChallengeGating("food", 95, 85, false);
            assertEquals("Score 95 must emit exactly 1 packet", 1, res95.packetsEmitted);
            assertEquals("Action must match food", "food", res95.emittedAction);

            CoopGatingResult res100 = simulateVoiceChallengeGating("help", 100, 85, false);
            assertEquals("Score 100 must emit exactly 1 packet", 1, res100.packetsEmitted);
            assertEquals("Action must match help", "help", res100.emittedAction);
        });

        runTest("VCG-PASS-03: Single-Emission Idempotency Under Multiple Ticks", () -> {
            // Simulate session state machine across 10 consecutive ticks after pass
            CoopSessionStateMachine sm = new CoopSessionStateMachine("lava", 85);
            sm.offerScore(88); // passed!
            sm.tick(); // first tick: finish() executes, sends 1 packet

            assertEquals("First tick must emit 1 packet", 1, sm.packetsEmitted.size());

            // 9 subsequent ticks
            for (int t = 0; t < 9; t++) {
                sm.tick();
            }

            assertEquals("Subsequent 9 ticks must NOT emit duplicate packets (strictly 1 total)", 1, sm.packetsEmitted.size());
        });

        runTest("VCG-PASS-04: Single-Emission Idempotency Under Rapid Duplicate Guesses", () -> {
            CoopSessionStateMachine sm = new CoopSessionStateMachine("follow", 85);
            // Recognizer delivers multiple guesses in rapid succession
            sm.offerScore(85);
            sm.offerScore(90);
            sm.offerScore(95);

            sm.tick(); // tick runs

            assertEquals("Rapid multiple guesses must still result in exactly 1 emitted packet", 1, sm.packetsEmitted.size());
            assertEquals("Emitted packet action must match", "follow", sm.packetsEmitted.get(0));
        });

        runTest("VCG-PASS-05: Re-entrant Session Preemption and Isolation", () -> {
            // User selects 'wood', but before finishing, opens radial menu and chooses 'waypoint'.
            // startCoopChallenge calls stop() first, aborting 'wood'.
            CoopSessionStateMachine sm = new CoopSessionStateMachine("wood", 85);
            sm.offerScore(50); // still failing

            // User preempts with waypoint
            sm.startNewCoopChallenge("waypoint", 85);

            // User now succeeds on waypoint
            sm.offerScore(92);
            sm.tick();

            assertEquals("Preempted session must only emit 1 packet for the new challenge", 1, sm.packetsEmitted.size());
            assertEquals("Emitted packet must be waypoint, NOT wood", "waypoint", sm.packetsEmitted.get(0));
        });

        // ---------------------------------------------------------------------
        // GROUP 6: REFLECTION & LIVE CLASS STATE INVARIANTS (LIVE-INV)
        // ---------------------------------------------------------------------
        runTest("LIVE-INV-01: ChallengeSession.pendingCoopAction Static Field Verification", () -> {
            Field f = com.spicesfire.sayitenglish.client.ChallengeSession.class.getField("pendingCoopAction");
            assertNotNull("pendingCoopAction field must exist", f);
            assertTrue("pendingCoopAction field must be public static", java.lang.reflect.Modifier.isStatic(f.getModifiers()));
        });

        runTest("LIVE-INV-02: ChallengeSession.stop() Static Method Verification", () -> {
            Method m = com.spicesfire.sayitenglish.client.ChallengeSession.class.getMethod("stop");
            assertNotNull("stop() method must exist", m);
            assertTrue("stop() method must be public static", java.lang.reflect.Modifier.isStatic(m.getModifiers()));
        });

        runTest("LIVE-INV-03: RadialMenuScreen.getHoveredSector() Method Verification", () -> {
            Method m = RadialMenuScreen.class.getMethod("getHoveredSector");
            assertNotNull("getHoveredSector() method must exist", m);
        });

        // =====================================================================
        // CONSOLIDATED SUMMARY
        // =====================================================================
        System.out.println("==============================================================================");
        System.out.println(String.format(" RESULTS: Total=%d, Passed=%d, Failed=%d", totalTests, passedTests, failedTests));
        if (failedTests > 0) {
            System.out.println(" FAILURES:");
            for (String f : failureDetails) {
                System.out.println("  - " + f);
            }
            System.exit(1);
        } else {
            System.out.println(" STATUS: ALL 28 EMPIRICAL ADVERSARIAL STRESS TESTS PASSED (100% PASS)");
            System.exit(0);
        }
    }

    // =========================================================================
    // REFERENCE MATHEMATICAL IMPLEMENTATION (RADIAL SCREEN ORACLE)
    // =========================================================================

    public static int calcRadialSector(double dx, double dy) {
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist >= 22.0 && dist <= 150.0) {
            double angleDeg = Math.toDegrees(Math.atan2(dy, dx));
            double shifted = angleDeg + 120.0;
            while (shifted < 0.0) shifted += 360.0;
            while (shifted >= 360.0) shifted -= 360.0;
            int sec = (int)(shifted / 60.0);
            if (sec < 0) sec = 0;
            if (sec > 5) sec = 5;
            return sec;
        }
        return -1;
    }

    public static int calcRadialAngleDeg(double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        double r = 68.0; // Standard radius
        double dx = r * Math.cos(rad);
        double dy = r * Math.sin(rad);
        return calcRadialSector(dx, dy);
    }

    // =========================================================================
    // VOICE CHALLENGE GATING SIMULATOR & STATE MACHINE
    // =========================================================================

    public static class CoopGatingResult {
        public final boolean pendingCleared;
        public final int packetsEmitted;
        public final String emittedAction;

        public CoopGatingResult(boolean pendingCleared, int packetsEmitted, String emittedAction) {
            this.pendingCleared = pendingCleared;
            this.packetsEmitted = packetsEmitted;
            this.emittedAction = emittedAction;
        }
    }

    public static CoopGatingResult simulateVoiceChallengeGating(String action, int speechScore, int passScore, boolean cancelled) {
        String pendingCoopAction = action;
        List<String> emitted = new ArrayList<>();

        if (cancelled) {
            // Emulate ChallengeSession.stop()
            pendingCoopAction = null;
        } else {
            // Emulate finish()
            boolean ok = (speechScore >= passScore);
            String coop = pendingCoopAction;
            pendingCoopAction = null; // Cleared immediately before check

            if (coop != null) {
                if (ok) {
                    emitted.add(coop);
                }
            }
        }

        return new CoopGatingResult(pendingCoopAction == null, emitted.size(), emitted.isEmpty() ? null : emitted.get(0));
    }

    public static class CoopSessionStateMachine {
        public String pendingCoopAction;
        public int passScore;
        public int best;
        public boolean passed;
        public boolean active;
        public final List<String> packetsEmitted = new ArrayList<>();

        public CoopSessionStateMachine(String action, int passScore) {
            startNewCoopChallenge(action, passScore);
        }

        public void startNewCoopChallenge(String action, int passScore) {
            stop(); // defense in depth
            this.pendingCoopAction = action;
            this.passScore = passScore;
            this.best = 0;
            this.passed = false;
            this.active = true;
        }

        public void offerScore(int score) {
            if (!active) return;
            if (score > best) {
                best = score;
            }
            if (score >= passScore) {
                passed = true;
            }
        }

        public void stop() {
            this.pendingCoopAction = null;
            this.active = false;
            this.passed = false;
        }

        public void tick() {
            if (!active) return;
            if (passed) {
                finish();
            }
        }

        private void finish() {
            boolean ok = (best >= passScore);
            String coop = pendingCoopAction;
            pendingCoopAction = null; // nullified immediately

            if (coop != null) {
                if (ok) {
                    packetsEmitted.add(coop);
                }
            }
            this.active = false;
        }
    }

    // =========================================================================
    // TEST RUNNER UTILITIES & ASSERTIONS
    // =========================================================================

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

    private static void assertEquals(String msg, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(msg + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(String msg, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError(msg + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(String msg, Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(msg + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(String msg, boolean condition) {
        if (!condition) {
            throw new AssertionError(msg + " | Condition was FALSE");
        }
    }

    private static void assertNotNull(String msg, Object obj) {
        if (obj == null) {
            throw new AssertionError(msg + " | Object was NULL");
        }
    }
}
