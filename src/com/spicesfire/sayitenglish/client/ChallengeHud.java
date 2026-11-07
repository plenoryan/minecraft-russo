/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.spicesfire.sayitenglish.Config
 *  com.spicesfire.sayitenglish.Packets$Challenge
 *  com.spicesfire.sayitenglish.client.ClientCompat
 *  com.spicesfire.sayitenglish.client.Microphone
 *  com.spicesfire.sayitenglish.client.Romanizer
 *  com.spicesfire.sayitenglish.client.SayItClient
 *  com.spicesfire.sayitenglish.client.SpeechEngine
 *  com.spicesfire.sayitenglish.client.Words
 *  com.spicesfire.sayitenglish.client.Words$Prompt
 *  net.minecraft.class_1074
 *  net.minecraft.class_1799
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 */
package com.spicesfire.sayitenglish.client;

import com.spicesfire.sayitenglish.Alphabet;
import com.spicesfire.sayitenglish.ChallengeManager;
import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.Scorer;
import com.spicesfire.sayitenglish.client.ChallengeSession;
import com.spicesfire.sayitenglish.client.ClientCompat;
import com.spicesfire.sayitenglish.client.Microphone;
import com.spicesfire.sayitenglish.client.Romanizer;
import com.spicesfire.sayitenglish.client.SayItClient;
import com.spicesfire.sayitenglish.client.SpeechEngine;
import com.spicesfire.sayitenglish.client.Words;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1068;
import net.minecraft.class_1074;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_640;
import net.minecraft.class_7532;
import net.minecraft.class_7923;

public final class ChallengeHud {
    private static final int GOLD = -14006;
    private static final int GREEN = -10755203;
    private static final int RED = -42406;
    private static final int GRAY = -4671288;
    private static final int DIM = -7697766;
    private static final int PANEL = -803726810;
    private static final int BAR_BG = -13881794;
    private static final int READING = -7351297;

    public record SpeechFeedback(String heard, String nativeTranslation, String target, int score, boolean success, String kind, long shownAt) {
    }

    public static volatile SpeechFeedback speechFeedback = null;

    public record CoopCueNotification(
        String initiatorName,
        String action,
        String ruText,
        String phonetic,
        String ptText,
        String objective,
        long receivedAt,
        long durationMs
    ) {
    }

    public static volatile CoopCueNotification activeCoopCue = null;

    public static void onCoopCue(Packets.CoopCue cue) {
        if (cue == null) {
            return;
        }
        long durationMs = cue.durationTicks() > 0 ? (long)cue.durationTicks() * 50L : 10000L;
        String initiator = cue.initiatorName() != null && !cue.initiatorName().isEmpty() ? cue.initiatorName() : "Aliado";
        String action = cue.cueType() != null && !cue.cueType().isEmpty() ? cue.cueType() : "help";
        String ru = cue.ruText() != null ? cue.ruText() : "";
        String pt = cue.ptText() != null ? cue.ptText() : "";
        if (ru.isEmpty() && cue.messageBilingual() != null && !cue.messageBilingual().isEmpty()) {
            String[] parts = cue.messageBilingual().split("\\|", 2);
            ru = parts[0].trim();
            if (pt.isEmpty() && parts.length > 1) {
                pt = parts[1].trim();
            }
        }
        activeCoopCue = new CoopCueNotification(
            initiator,
            action,
            ru,
            cue.phonetic() != null ? cue.phonetic() : "",
            pt,
            cue.objective() != null ? cue.objective() : "",
            System.currentTimeMillis(),
            durationMs
        );
    }

    private ChallengeHud() {
    }

    public static void render(class_332 g, float partialTick) {
        ChallengeSession.Banner b;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        class_327 font = mc.field_1772;
        if (ChallengeSession.current != null && ChallengeSession.prompt != null) {
            ChallengeHud.panel(g, mc, font);
        }
        if ((b = ChallengeSession.banner) != null) {
            long age = System.currentTimeMillis() - b.shownAt();
            if (age > 2800L) {
                ChallengeSession.banner = null;
            } else {
                ChallengeHud.banner(g, mc, font, b);
            }
        }
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
        CoopCueNotification cue = activeCoopCue;
        if (cue != null) {
            long age = System.currentTimeMillis() - cue.receivedAt();
            if (age > cue.durationMs()) {
                activeCoopCue = null;
            } else {
                ChallengeHud.renderCoopCueNotification(g, mc, font, cue, age);
            }
        }
    }

    private static void panel(class_332 g, class_310 mc, class_327 font) {
        Object status;
        String reading;
        Packets.Challenge c = ChallengeSession.current;
        Words.Prompt p = ChallengeSession.prompt;
        boolean sentence = c.kind().equals("sentence");
        boolean sameLanguage = Words.gameIsTarget();
        float left = ChallengeSession.remaining();
        float frac = left / (float)Math.max(1, ChallengeSession.secondsTotal());
        boolean recall = Config.get().mode.equals("recall") && !sameLanguage;
        boolean reveal = !recall || frac < 0.5f;
        boolean isRu = "ru".equals(Words.learning().code());
        Map<String, Integer> wordStars = new HashMap<>();
        if (ChallengeSession.words != null && ChallengeSession.scores != null) {
            for (int i = 0; i < ChallengeSession.words.size() && i < ChallengeSession.scores.size(); i++) {
                String id = ChallengeSession.words.get(i);
                int sc = ChallengeSession.scores.get(i);
                wordStars.put(id, sc);
            }
        }

        Set<Character> unlocked = Alphabet.computeUnlockedLetters(wordStars, Alphabet.CLIENT_HEAT);

        String bigTarget = p.target();
        if (isRu) {
            bigTarget = Alphabet.toSmartHybrid(p.target(), unlocked, true);
        }
        String big = reveal ? bigTarget : ChallengeHud.hint(bigTarget);
        String small = sameLanguage ? "" : p.nativeText();
        if (isRu && !sameLanguage && !small.isEmpty()) {
            small += " \u00a77[\u00a76Dourado=Russo\u00a77|\u00a7fBranco=Portugu\u00eas\u00a77]";
        }

        if (isRu) {
            Character nextFocus = Alphabet.getNextFocusLetter(p.target(), unlocked);
            int masteredCount = Alphabet.getLetterCount(unlocked);
            java.util.List<String> ffAlerts = Alphabet.getFalseFriendAlerts(p.target(), unlocked);
            String ffSuffix = ffAlerts.isEmpty() ? "" : " \u2022 " + String.join(" | ", ffAlerts);
            if (nextFocus == null) {
                reading = "Palavra 100% em Cir\u00edlico Fluente! \u2605\u2605\u2605\u2605\u2605 \u2022 (" + masteredCount + "/33 letras)" + ffSuffix;
            } else {
                int lvl = Alphabet.LETTER_LEVEL.getOrDefault(nextFocus, 1);
                int heat = Alphabet.getHeat(nextFocus);
                int thresh = Alphabet.getHeatThreshold(nextFocus);
                reading = "Em Foco: " + Alphabet.LEVEL_NAME[lvl] + " (" + Alphabet.LEVEL_MNEM[lvl] + ") \u2022 Calor: " + heat + "/" + thresh + "x" + ffSuffix;
            }
        } else {
            reading = reveal ? Romanizer.of((String)p.target(), (String)Words.learning().code()) : null;
            if (reading == null) {
                reading = "";
            }
        }
        float bigScale = sentence ? 1.0f : 1.5f;
        int screenW = mc.method_22683().method_4486();
        int textW = Math.max(Math.max((int)((float)font.method_1727(big) * bigScale), font.method_1727(small)), font.method_1727(reading));
        int x = 8;
        int y = 8;
        int w = Math.min(screenW - 16, Math.max(200, textW + 44));
        int h = 50 + (small.isEmpty() ? 0 : 10) + (reading.isEmpty() ? 0 : 11);
        ClientCompat.fill((class_332)g, (int)x, (int)y, (int)(x + w), (int)(y + h), (int)-803726810);
        ClientCompat.fill((class_332)g, (int)x, (int)y, (int)(x + 2), (int)(y + h), (int)-14006);
        if (!p.icon().method_7960()) {
            ClientCompat.item((class_332)g, (class_1799)p.icon(), (int)(x + 8), (int)(y + 14));
        }
        int tx = x + 30;
        String langName = class_1074.method_4662((String)("sayitenglish.lang." + Words.learning().code()), (Object[])new Object[0]);
        String label = class_1074.method_4662((String)(sentence ? "sayitenglish.hud.say_sentence" : (c.kind().equals("practice") ? "sayitenglish.hud.practice" : "sayitenglish.hud.say_word")), (Object[])new Object[]{langName}).toUpperCase(Locale.forLanguageTag(mc.method_1526().method_4669().replace('_', '-')));
        ClientCompat.text((class_332)g, (class_327)font, (String)label, (int)tx, (int)(y + 5), (int)-7697766);
        String secs = (int)Math.ceil(left) + "s";
        ClientCompat.text((class_332)g, (class_327)font, (String)secs, (int)(x + w - 6 - font.method_1727(secs)), (int)(y + 5), (int)(left < 4.0f ? -42406 : -4671288));
        ClientCompat.push((class_332)g, (float)tx, (float)(y + 16), (float)bigScale);
        ClientCompat.text((class_332)g, (class_327)font, (String)big, (int)0, (int)0, (int)-14006);
        ClientCompat.pop((class_332)g);
        int line = y + 16 + (int)(10.0f * bigScale) + 2;
        if (!reading.isEmpty()) {
            ClientCompat.text((class_332)g, (class_327)font, (String)reading, (int)tx, (int)line, (int)-7351297);
            line += 11;
        }
        if (!small.isEmpty()) {
            ClientCompat.text((class_332)g, (class_327)font, (String)small, (int)tx, (int)line, (int)-4671288);
            line += 11;
        }
        ClientCompat.fill((class_332)g, (int)tx, (int)line, (int)(x + w - 6), (int)(line + 2), (int)-13881794);
        ClientCompat.fill((class_332)g, (int)tx, (int)line, (int)(tx + (int)((float)(x + w - 6 - tx) * frac)), (int)(line + 2), (int)(left < 4.0f ? -42406 : -14006));
        line += 5;
        int color = -7697766;
        SpeechEngine e = SpeechEngine.forConfig((String)Config.get().engine, (String)ChallengeSession.language);
        if (!ChallengeSession.heard.isEmpty()) {
            int best = ChallengeSession.best;
            status = class_1074.method_4662((String)"sayitenglish.hud.heard", (Object[])new Object[]{ChallengeSession.bestHeard, best});
            color = best >= c.passScore() ? -10755203 : -42406;
        } else {
            status = !e.ready() ? class_1074.method_4662((String)e.statusKey(), (Object[])new Object[]{e.statusArg()}) + "  " + class_1074.method_4662((String)"sayitenglish.hud.type_hint", (Object[])new Object[]{SayItClient.typeKeyName()}) : (ChallengeSession.micTried && !ChallengeSession.micOk ? class_1074.method_4662((String)"sayitenglish.hud.no_mic", (Object[])new Object[]{SayItClient.typeKeyName()}) : class_1074.method_4662((String)"sayitenglish.hud.listening", (Object[])new Object[0]));
        }
        ClientCompat.text((class_332)g, (class_327)font, (String)status, (int)tx, (int)line, (int)color);
        if (ChallengeSession.micOk) {
            int mh = (int)(Microphone.level * (float)(h - 8));
            ClientCompat.fill((class_332)g, (int)(x + w - 4), (int)(y + h - 4 - mh), (int)(x + w - 2), (int)(y + h - 4), (int)-10755203);
        }
    }

    private static void banner(class_332 g, class_310 mc, class_327 font, ChallengeSession.Banner b) {
        int cx = mc.method_22683().method_4486() / 2;
        int y = mc.method_22683().method_4502() / 4;
        String head = b.success() ? class_1074.method_4662((String)(b.kind().equals("word") ? "sayitenglish.hud.unlocked" : "sayitenglish.hud.correct"), (Object[])new Object[0]) : class_1074.method_4662((String)(b.kind().equals("word") ? "sayitenglish.hud.lost." + Config.get().penalty : "sayitenglish.hud.missed"), (Object[])new Object[0]);
        int color = b.success() ? -10755203 : -42406;
        ClientCompat.push((class_332)g, (float)cx, (float)y, (float)2.0f);
        ClientCompat.centered((class_332)g, (class_327)font, (String)head, (int)0, (int)0, (int)color);
        ClientCompat.pop((class_332)g);
        ClientCompat.centered((class_332)g, (class_327)font, (String)b.text(), (int)cx, (int)(y + 22), (int)-14006);
        ClientCompat.centered((class_332)g, (class_327)font, (String)class_1074.method_4662((String)(b.success() ? "sayitenglish.hud.score" : "sayitenglish.hud.best_try"), (Object[])new Object[]{b.score()}), (int)cx, (int)(y + 34), (int)-4671288);
    }

    static String hint(String s) {
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (char ch : s.toCharArray()) {
            if (Character.isLetter(ch)) {
                b.append(first ? ch : (char)'_');
                first = false;
                continue;
            }
            b.append(ch);
            first = true;
        }
        return b.toString();
    }

    private static void renderSpeechFeedback(class_332 g, class_310 mc, class_327 font, SpeechFeedback fb, long age) {
        int screenW = mc.method_22683().method_4486();

        // R2: Cabecalho estritamente "disse: " + badge de porcentagem
        String header = "disse: ";
        String scoreBadge = fb.score() + "%";
        String spoken = fb.heard().isEmpty() ? "\u00a77(sem \u00e1udio)" : "\u00a7f\"" + fb.heard() + "\"";
        String trans = fb.nativeTranslation().isEmpty() ? "" : "\u00a77" + fb.nativeTranslation();

        int textW = Math.max(font.method_1727(header) + font.method_1727(scoreBadge) + 16, Math.max(font.method_1727(spoken), font.method_1727(trans)));
        int w = Math.min(screenW - 16, Math.max(150, textW + 16));
        int x = screenW - w - 8;
        int y = 8;
        int h = 28 + (trans.isEmpty() ? 0 : 10);

        // Fundo semitransparente escuro
        ClientCompat.fill((class_332)g, (int)x, (int)y, (int)(x + w), (int)(y + h), (int)-803726810);
        // Borda lateral de status (Verde para sucesso, Vermelha para erro)
        ClientCompat.fill((class_332)g, (int)(x + w - 2), (int)y, (int)(x + w), (int)(y + h), (int)(fb.success() ? -10755203 : -42406));

        int curY = y + 4;
        // Linha 1: "disse: " a esquerda e badge de pontuacao a direita
        ClientCompat.text((class_332)g, (class_327)font, (String)header, (int)(x + 6), (int)curY, (int)-7697766);
        int badgeW = font.method_1727(scoreBadge);
        ClientCompat.text((class_332)g, (class_327)font, (String)scoreBadge, (int)(x + w - 6 - badgeW), (int)curY, (int)(fb.success() ? -10755203 : -42406));

        curY += 10;
        // Linha 2: Transcricao ouvida
        ClientCompat.text((class_332)g, (class_327)font, (String)spoken, (int)(x + 6), (int)curY, (int)-1);

        // Linha 3 (se presente): Traducao em portugues
        if (!trans.isEmpty()) {
            curY += 10;
            ClientCompat.text((class_332)g, (class_327)font, (String)trans, (int)(x + 6), (int)curY, (int)-4671288);
        }
    }

    public static class_1799 resolveActionStack(String action) {
        if (action == null) {
            return new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960("minecraft:paper")));
        }
        String act = action.toLowerCase();
        String itemId;
        if (act.contains("wood")) {
            itemId = "minecraft:oak_log";
        } else if (act.contains("food")) {
            itemId = "minecraft:bread";
        } else if (act.contains("help")) {
            itemId = "minecraft:golden_apple";
        } else if (act.contains("lava")) {
            itemId = "minecraft:lava_bucket";
        } else if (act.contains("follow")) {
            itemId = "minecraft:compass";
        } else if (act.contains("waypoint") || act.contains("point") || act.contains("spyglass") || act.contains("recovery")) {
            itemId = "minecraft:spyglass";
        } else {
            class_1792 direct = ChallengeManager.item(action);
            if (direct != null) {
                return new class_1799((class_1935)direct);
            }
            itemId = "minecraft:paper";
        }
        class_1792 it = ChallengeManager.item(itemId);
        if (it != null) {
            return new class_1799((class_1935)it);
        }
        return new class_1799((class_1935)class_7923.field_41178.method_10223(new class_2960(itemId)));
    }

    private static void renderCoopCueNotification(
        class_332 g,
        class_310 mc,
        class_327 font,
        CoopCueNotification cue,
        long age
    ) {
        int screenW = mc.method_22683().method_4486();
        int baseW = 240;
        int h = 38;

        String title = "\u00a7e" + cue.initiatorName() + " \u00a7fpediu \u00a76[?]:";
        String sub = "\u00a76" + cue.ruText() + (!cue.ptText().isEmpty() ? " \u00a77(" + cue.ptText() + ")" : "");

        int textW = Math.max(font.method_1727(title), font.method_1727(sub));
        int w = Math.min(screenW - 16, Math.max(baseW, textW + 66));
        int x = (screenW - w) / 2;
        int y = 12;

        // Dark panel background with gold accent borders
        ClientCompat.fill((class_332)g, (int)x, (int)y, (int)(x + w), (int)(y + h), (int)PANEL);
        ClientCompat.fill((class_332)g, (int)x, (int)y, (int)(x + w), (int)(y + 2), (int)GOLD);
        ClientCompat.fill((class_332)g, (int)x, (int)y, (int)(x + 2), (int)(y + h), (int)GOLD);
        ClientCompat.fill((class_332)g, (int)(x + w - 2), (int)y, (int)(x + w), (int)(y + h), (int)GOLD);

        // 1. Initiator player face
        class_2960 skin = null;
        if (mc.method_1562() != null && cue.initiatorName() != null) {
            class_640 entry = mc.method_1562().method_2874(cue.initiatorName());
            if (entry != null) {
                skin = entry.method_2968();
            }
        }
        if (skin == null) {
            UUID offlineUuid = UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + (cue.initiatorName() != null ? cue.initiatorName() : "Player")).getBytes(StandardCharsets.UTF_8)
            );
            skin = class_1068.method_4648(offlineUuid);
        }
        if (skin == null) {
            skin = class_1068.method_4649();
        }
        int headX = x + 6;
        int headY = y + 7;
        int headSize = 24;
        class_7532.method_44443(g, skin, headX, headY, headSize);

        // 2. Evocative action item + '?' overlay
        class_1799 actionStack = resolveActionStack(cue.action());
        int iconX = headX + headSize + 6;
        int iconY = y + 11;
        ClientCompat.item((class_332)g, (class_1799)actionStack, (int)iconX, (int)iconY);
        g.method_51432(font, actionStack, iconX, iconY, "?");

        // 3. Bilingual request text
        int tx = iconX + 22;
        ClientCompat.text((class_332)g, (class_327)font, (String)title, (int)tx, (int)(y + 6), (int)-1);
        ClientCompat.text((class_332)g, (class_327)font, (String)sub, (int)tx, (int)(y + 18), (int)-1);

        // 4. Time remaining bar
        float frac = Math.max(0.0f, Math.min(1.0f, 1.0f - ((float)age / (float)cue.durationMs())));
        ClientCompat.fill((class_332)g, (int)(x + 2), (int)(y + h - 2), (int)(x + w - 2), (int)(y + h), (int)BAR_BG);
        int barW = (int)((float)(w - 4) * frac);
        int barColor = frac < 0.25f ? RED : GOLD;
        ClientCompat.fill((class_332)g, (int)(x + 2), (int)(y + h - 2), (int)(x + 2 + barW), (int)(y + h), (int)barColor);
    }
}
