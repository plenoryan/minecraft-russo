/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1074
 *  net.minecraft.class_2561
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_4185
 *  net.minecraft.class_437
 */
package com.spicesfire.sayitenglish.client;

import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.client.ChallengeSession;
import com.spicesfire.sayitenglish.client.ClientCompat;
import com.spicesfire.sayitenglish.client.Romanizer;
import com.spicesfire.sayitenglish.client.SettingsScreen;
import com.spicesfire.sayitenglish.client.Words;
import com.spicesfire.sayitenglish.platform.ClientPlatform;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public class DictionaryScreen
extends class_437 {
    private static final int ROW = 20;
    private static final int PANEL_W = 300;
    private int scroll;
    private final Map<String, Words.Prompt> cache = new HashMap<String, Words.Prompt>();
    private final Map<String, String> readings = new HashMap<String, String>();

    public DictionaryScreen() {
        super((class_2561)class_2561.method_43471((String)"sayitenglish.dictionary.title"));
    }

    private int rows() {
        return Math.max(3, (this.field_22790 - 110) / 20);
    }

    protected void method_25426() {
        int x = this.field_22789 / 2 - 150;
        int y = this.field_22790 - 30;
        this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43471((String)"sayitenglish.dictionary.practice"), b -> {
            ClientPlatform.I.sendToServer(new Packets.Practice(0));
            this.method_25419();
        }).method_46434(x, y, 96, 20).method_46431());
        this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43471((String)"sayitenglish.dictionary.settings"), b -> ClientCompat.openScreen(this.field_22787, new SettingsScreen(this))).method_46434(x + 102, y, 96, 20).method_46431());
        this.method_37063(class_4185.method_46430((class_2561)class_2561.method_43471((String)"gui.done"), b -> this.method_25419()).method_46434(x + 204, y, 96, 20).method_46431());
    }

    public boolean method_25401(double mx, double my, double dy) {
        int max = Math.max(0, ChallengeSession.words.size() - this.rows());
        this.scroll = Math.max(0, Math.min(max, this.scroll - (int)Math.signum(dy)));
        return true;
    }

    public boolean method_25421() {
        return false;
    }

    public void method_25419() {
        ClientCompat.openScreen(this.field_22787, null);
    }

    public void method_25394(class_332 g, int mouseX, int mouseY, float partialTick) {
        int idx;
        this.method_25420(g);
        super.method_25394(g, mouseX, mouseY, partialTick);
        int cx = this.field_22789 / 2;
        int x = cx - 150;
        ClientCompat.centered(g, this.field_22793, this.field_22785.getString(), cx, 14, -14006);
        ClientCompat.centered(g, this.field_22793, class_1074.method_4662((String)"sayitenglish.dictionary.stats", (Object[])new Object[]{ChallengeSession.words.size(), ChallengeSession.sentences, ChallengeSession.lost}), cx, 28, -4671288);
        int top = 46;
        ClientCompat.fill(g, x - 4, top - 4, x + 300 + 4, top + this.rows() * 20 + 2, -1341124584);
        if (ChallengeSession.words.isEmpty()) {
            ClientCompat.centered(g, this.field_22793, class_1074.method_4662((String)"sayitenglish.dictionary.empty", (Object[])new Object[0]), cx, top + 20, -7697766);
            return;
        }
        boolean sameLanguage = Words.gameIsTarget();
        for (int i = 0; i < this.rows() && (idx = this.scroll + i) < ChallengeSession.words.size(); ++i) {
            String reading;
            String id = ChallengeSession.words.get(idx);
            Words.Prompt p = this.cache.computeIfAbsent(id, Words::word);
            int y = top + i * 20;
            if (!p.icon().method_7960()) {
                ClientCompat.item(g, p.icon(), x, y);
            }
            if ((reading = this.readings.computeIfAbsent(id, k -> {
                String r = Romanizer.of(p.target(), Words.learning().code());
                return r == null ? "" : r;
            })).isEmpty()) {
                ClientCompat.text(g, this.field_22793, p.target(), x + 22, y + 4, -1);
            } else {
                ClientCompat.text(g, this.field_22793, p.target(), x + 22, y, -1);
                ClientCompat.push(g, x + 22, y + 10, 0.75f);
                ClientCompat.text(g, this.field_22793, reading, 0, 0, -7351297);
                ClientCompat.pop(g);
            }
            if (!sameLanguage) {
                ClientCompat.text(g, this.field_22793, p.nativeText(), x + 150, y + 4, -6645078);
            }
            int score = ChallengeSession.scores.get(idx);
            String s;
            int color;
            if (score <= 5) {
                switch (score) {
                    case 5:
                        s = "★★★★★ [5/5 - Mestre]";
                        color = -10755203;
                        break;
                    case 4:
                        s = "★★★★☆ [4/5]";
                        color = -11141121;
                        break;
                    case 3:
                        s = "★★★☆☆ [3/5]";
                        color = -14006;
                        break;
                    case 2:
                        s = "★★☆☆☆ [2/5]";
                        color = -26368;
                        break;
                    default:
                        s = "★☆☆☆☆ [1/5]";
                        color = -30118;
                        break;
                }
            } else {
                s = score + "%";
                color = score >= 90 ? -10755203 : (score >= 75 ? -14006 : -30118);
            }
            ClientCompat.text(g, this.field_22793, s, x + 300 - this.field_22793.method_1727(s), y + 4, color);
        }
    }
}

