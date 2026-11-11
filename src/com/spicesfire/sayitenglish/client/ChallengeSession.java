/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spicesfire.sayitenglish.Config
 *  com.spicesfire.sayitenglish.Packets$Challenge
 *  com.spicesfire.sayitenglish.Packets$Result
 *  com.spicesfire.sayitenglish.Packets$Sync
 *  com.spicesfire.sayitenglish.Scorer
 *  com.spicesfire.sayitenglish.client.Microphone
 *  com.spicesfire.sayitenglish.client.Romanizer
 *  com.spicesfire.sayitenglish.client.SpeechEngine
 *  com.spicesfire.sayitenglish.client.SpeechEngine$Guess
 *  com.spicesfire.sayitenglish.client.SpeechEngine$Request
 *  com.spicesfire.sayitenglish.client.Words
 *  com.spicesfire.sayitenglish.client.Words$Prompt
 *  com.spicesfire.sayitenglish.platform.ClientPlatform
 *  com.spicesfire.sayitenglish.platform.ModPacket
 *  net.minecraft.class_310
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 */
package com.spicesfire.sayitenglish.client;

import com.spicesfire.sayitenglish.Alphabet;
import com.spicesfire.sayitenglish.ChallengeManager;
import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.Scorer;
import com.spicesfire.sayitenglish.audio.AudioPlaybackManager;
import com.spicesfire.sayitenglish.client.Microphone;
import com.spicesfire.sayitenglish.client.Romanizer;
import com.spicesfire.sayitenglish.client.SpeechEngine;
import com.spicesfire.sayitenglish.client.Words;
import com.spicesfire.sayitenglish.platform.ClientPlatform;
import com.spicesfire.sayitenglish.platform.ModPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;

public final class ChallengeSession {
    public static String pendingCoopAction = null;
    public static Packets.Challenge current;
    public static Words.Prompt prompt;
    public static long startedAt;
    private static long pausedAt;
    public static volatile int best;
    public static volatile String heard;
    public static volatile String bestHeard;
    public static volatile boolean micOk;
    public static volatile boolean micTried;
    private static boolean micRequested;
    private static volatile int generation;
    private static final ExecutorService AUDIO;
    private static SpeechEngine engine;
    private static volatile boolean passed;
    public static Banner banner;
    public static String language;
    public static boolean askLanguage;
    public static boolean synced;
    private static List<String> alternatives;
    public static List<String> words;
    public static List<Integer> scores;
    public static int sentences;
    public static int lost;

    private ChallengeSession() {
    }

    public static void start(Packets.Challenge c) {
        if (pendingCoopAction != null) {
            // A cooperative request is actively running! Do not allow server challenges to overwrite it.
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc != null && (mc.field_1755 instanceof RadialMenuScreen || mc.field_1755 instanceof ItemSearchScreen)) {
            // Player is actively in the radial menu or item search screen!
            return;
        }
        ChallengeSession.stop();
        current = c;
        prompt = c.kind().equals("sentence") ? Words.sentence((String)c.subject(), (String)c.arg()) : Words.word((String)c.subject());
        startedAt = System.currentTimeMillis();
        pausedAt = 0L;
        best = 0;
        heard = "";
        bestHeard = "";
        passed = false;
        banner = null;
        engine = SpeechEngine.forConfig((String)Config.get().engine, (String)language);
        engine.prepare(language);
        alternatives = c.kind().equals("sentence") ? Words.otherSentences((String)prompt.target()) : Words.otherWords((String)c.subject(), (int)40);
        if (prompt != null && prompt.target() != null) {
            ArrayList<String> alts = new ArrayList<>(alternatives);
            String targetStr = prompt.target();
            List<String> tWords = Scorer.words(targetStr);
            if (tWords.size() > 1) {
                for (String tw : tWords) {
                    if (tw.length() >= 2 && !alts.contains(tw)) {
                        alts.add(tw);
                        String twNoSoft = tw.replace("\u044c", "").replace("\u044a", "");
                        if (!twNoSoft.isEmpty() && !alts.contains(twNoSoft)) {
                            alts.add(twNoSoft);
                        }
                    }
                }
            }
            String targetNoSoft = targetStr.replace("\u044c", "").replace("\u044a", "");
            if (!targetNoSoft.equals(targetStr) && !alts.contains(targetNoSoft)) {
                alts.add(targetNoSoft);
            }
            alternatives = alts;
        }
        micOk = false;
        micTried = false;
        micRequested = false;
        ChallengeSession.tryListen();
        if (mc.field_1724 != null) {
            mc.field_1724.method_5783((class_3414)class_3417.field_14622.comp_349(), 0.6f, 1.4f);
        }
    }

    public static void startCoopChallenge(String action, String ruText, String ptText, String icon, int seconds, int passScore) {
        ChallengeSession.stop();
        pendingCoopAction = action;
        ClientPlatform.I.sendToServer((ModPacket)new Packets.CoopInvoke("coop_busy:true", ""));
        int clientChallengeId = -100 - Math.abs(action.hashCode()) % 1000;
        current = new Packets.Challenge(clientChallengeId, "sentence", action, icon != null ? icon : "", seconds, passScore);

        class_1799 iconStack = class_1799.field_8037;
        if (icon != null && !icon.isEmpty()) {
            class_1792 it = ChallengeManager.item(icon);
            if (it != null) {
                iconStack = new class_1799((class_1935)it);
            }
        }
        prompt = new Words.Prompt(ruText, ptText != null ? ptText : "", iconStack);
        startedAt = System.currentTimeMillis();
        pausedAt = 0L;
        best = 0;
        heard = "";
        bestHeard = "";
        passed = false;
        banner = null;
        engine = SpeechEngine.forConfig((String)Config.get().engine, (String)language);
        engine.prepare(language);

        alternatives = Words.otherSentences(ruText);
        ArrayList<String> alts = new ArrayList<>(alternatives);
        List<String> tWords = Scorer.words(ruText);
        if (tWords.size() > 1) {
            for (String tw : tWords) {
                if (tw.length() >= 2 && !alts.contains(tw)) {
                    alts.add(tw);
                    String twNoSoft = tw.replace("\u044c", "").replace("\u044a", "");
                    if (!twNoSoft.isEmpty() && !alts.contains(twNoSoft)) {
                        alts.add(twNoSoft);
                    }
                }
            }
        }
        String targetNoSoft = ruText.replace("\u044c", "").replace("\u044a", "");
        if (!targetNoSoft.equals(ruText) && !alts.contains(targetNoSoft)) {
            alts.add(targetNoSoft);
        }
        alternatives = alts;

        micOk = false;
        micTried = false;
        micRequested = false;
        ChallengeSession.tryListen();
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null) {
            mc.field_1724.method_5783((class_3414)class_3417.field_14622.comp_349(), 0.6f, 1.4f);
        }
    }

    private static void tryListen() {
        if (micRequested || current == null || !engine.ready()) {
            return;
        }
        micRequested = true;
        String target = prompt.target();
        SpeechEngine e = engine;
        int gen = generation;
        SpeechEngine.Request request = new SpeechEngine.Request(language, target, alternatives, guess -> {
            if (gen == generation) {
                ChallengeSession.offer(guess, target);
            }
        });
        AUDIO.execute(() -> {
            if (gen != generation) {
                return;
            }
            e.begin(request);
            boolean ok = Microphone.start((arg_0, arg_1) -> ((SpeechEngine)e).feed(arg_0, arg_1));
            if (!ok) {
                e.end();
            }
            if (gen != generation) {
                Microphone.stop();
                e.end();
                return;
            }
            micOk = ok;
            micTried = true;
        });
    }

    private static void release() {
        ++generation;
        micOk = false;
        SpeechEngine e = engine;
        AUDIO.execute(() -> {
            Microphone.stop();
            if (e != null) {
                e.end();
            }
        });
    }

    public static void offer(String text, String target) {
        String reading = Romanizer.plain((String)target, (String)language);
        if (reading != null && Scorer.score((String)text, (String)reading, (boolean)false) > Scorer.score((String)text, (String)target, (boolean)false)) {
            target = reading;
        }
        ChallengeSession.offer(new SpeechEngine.Guess(text, -1.0), target);
    }

    public static void offer(SpeechEngine.Guess guess, String target) {
        String text = guess.text();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        boolean eng = Words.learning().english();
        int s = Scorer.score((String)text, (String)target, (boolean)eng);

        // Cross-match against Romanized target whole phrase
        String reading = Romanizer.plain((String)target, (String)language);
        if (reading != null) {
            s = Math.max(s, Scorer.score((String)text, (String)reading, (boolean)false));
        }

        // Handle guesses from restricted recognizer
        if (guess.confidence() >= 0.0) {
            int passReq = current != null ? current.passScore() : 70;
            // Only discard candidates from restricted recognizer if they completely fail to match target/keywords
            if (s < 60 && s < passReq) {
                return;
            }
            int confScore = (int)Math.round(Math.min(1.0, guess.confidence()) * 100.0);
            s = Math.max(s, confScore);
        }

        heard = text;
        if (s >= best) {
            best = s;
            bestHeard = text;
        }
        if (current != null) {
            boolean isSentence = "sentence".equals(current.kind())
                || (prompt != null && prompt.target() != null && Scorer.words(prompt.target()).size() > 1);
            int passThreshold = current.passScore();
            List<String> targetWords = Scorer.words(target);
            List<String> heardWords = Scorer.words(text);
            if (heardWords.size() >= targetWords.size() && s >= passThreshold) {
                passed = true;
            }
        }
    }

    public static int secondsTotal() {
        return current == null ? 0 : current.seconds();
    }

    public static float remaining() {
        if (current == null) {
            return 0.0f;
        }
        long now = pausedAt != 0L ? pausedAt : System.currentTimeMillis();
        return Math.max(0.0f, (float)current.seconds() - (float)(now - startedAt) / 1000.0f);
    }

    public static void pause(boolean paused) {
        if (current == null) {
            return;
        }
        if (paused && pausedAt == 0L) {
            pausedAt = System.currentTimeMillis();
        } else if (!paused && pausedAt != 0L) {
            startedAt += System.currentTimeMillis() - pausedAt;
            pausedAt = 0L;
        }
    }

    public static void triggerHear() {
        if (current == null || prompt == null || prompt.target() == null) {
            return;
        }
        String targetText = prompt.target();
        pause(true);
        AudioPlaybackManager.play(targetText, () -> {
            class_310 mc = class_310.method_1551();
            if (mc != null) {
                mc.execute(() -> {
                    pause(false);
                });
            } else {
                pause(false);
            }
        });
    }

    public static void tick() {
        if (current == null) {
            return;
        }
        engine.prepare(language);
        ChallengeSession.tryListen();
        if (passed || ChallengeSession.remaining() <= 0.0f) {
            ChallengeSession.finish();
        }
    }

    private static void finish() {
        AudioPlaybackManager.stop();
        Packets.Challenge c = current;
        ChallengeSession.release();
        List<String> targetWords = prompt != null && prompt.target() != null ? Scorer.words(prompt.target()) : List.of();
        List<String> heardWords = Scorer.words(bestHeard != null && !bestHeard.isEmpty() ? bestHeard : heard);
        boolean phraseComplete = targetWords.isEmpty() || heardWords.size() >= targetWords.size();
        boolean ok = c != null && best >= c.passScore() && phraseComplete;
        String coop = pendingCoopAction;
        pendingCoopAction = null;
        if (coop != null) {
            ClientPlatform.I.sendToServer((ModPacket)new Packets.CoopInvoke("coop_busy:false", ""));
            if (ok) {
                ClientPlatform.I.sendToServer((ModPacket)new Packets.CoopInvoke(coop, ""));
            }
        } else if (c != null && c.id() > 0) {
            ClientPlatform.I.sendToServer((ModPacket)new Packets.Result(c.id(), best, bestHeard.length() > 200 ? bestHeard.substring(0, 200) : bestHeard));
        }
        if (prompt != null && c != null) {
            banner = new Banner(prompt.target(), best, ok, c.kind(), System.currentTimeMillis());
            // R2: Restringir speechFeedback exclusivamente para frases completas (suprimir em palavras de item)
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
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null) {
            mc.field_1724.method_5783((class_3414)(ok ? class_3417.field_14725 : class_3417.field_14624).comp_349(), 0.8f, ok ? 1.2f : 0.6f);
        }
        current = null;
        prompt = null;
    }

    public static void stop() {
        AudioPlaybackManager.stop();
        if (pendingCoopAction != null) {
            ClientPlatform.I.sendToServer((ModPacket)new Packets.CoopInvoke("coop_busy:false", ""));
            pendingCoopAction = null;
        }
        ChallengeSession.release();
        current = null;
        prompt = null;
    }

    public static void sync(Packets.Sync s) {
        Alphabet.CLIENT_HEAT.clear();
        if (!s.language().isEmpty()) {
            language = s.language();
        }
        synced = true;
        words = new ArrayList<String>(s.words());
        scores = new ArrayList<Integer>(s.scores());
        sentences = s.sentences();
        lost = s.lost();
    }

    static {
        heard = "";
        bestHeard = "";
        AUDIO = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sayitenglish-audio");
            t.setDaemon(true);
            return t;
        });
        language = "en";
        alternatives = List.of();
        words = new ArrayList<String>();
        scores = new ArrayList<Integer>();
    }

    public record Banner(String text, int score, boolean success, String kind, long shownAt) {
    }
}
