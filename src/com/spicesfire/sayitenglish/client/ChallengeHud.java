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
import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.client.ChallengeSession;
import com.spicesfire.sayitenglish.client.ClientCompat;
import com.spicesfire.sayitenglish.client.Microphone;
import com.spicesfire.sayitenglish.client.Romanizer;
import com.spicesfire.sayitenglish.client.SayItClient;
import com.spicesfire.sayitenglish.client.SpeechEngine;
import com.spicesfire.sayitenglish.client.Words;
import java.util.Locale;
import net.minecraft.class_1074;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;

public final class ChallengeHud {
    private static final int GOLD = -14006;
    private static final int GREEN = -10755203;
    private static final int RED = -42406;
    private static final int GRAY = -4671288;
    private static final int DIM = -7697766;
    private static final int PANEL = -803726810;
    private static final int BAR_BG = -13881794;
    private static final int READING = -7351297;

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
        int totalStars = 0;
        if (ChallengeSession.scores != null) {
            for (int s : ChallengeSession.scores) {
                totalStars += Math.max(0, s);
            }
        }
        int alphaLvl = Alphabet.calculateLevel(totalStars);

        String bigTarget = p.target();
        if (isRu) {
            bigTarget = Alphabet.toHybrid(p.target(), alphaLvl, false);
        }
        String big = reveal ? bigTarget : ChallengeHud.hint(bigTarget);
        String small = sameLanguage ? "" : p.nativeText();

        if (isRu) {
            if (alphaLvl == 0) {
                reading = "N\u00edvel 0/32 [Latim] \u2022 Conquiste 1 \u2605 para ativar \u0410";
            } else if (alphaLvl >= 32) {
                reading = "N\u00edvel 32/32 \u2022 100% Cir\u00edlico Fluente";
            } else {
                reading = "N\u00edvel " + alphaLvl + "/32 \u2022 " + Alphabet.LEVEL_NAME[alphaLvl] + " (" + Alphabet.LEVEL_MNEM[alphaLvl] + ")";
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
}
