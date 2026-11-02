package com.spicesfire.sayitenglish;

import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.Languages;
import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.Progress;
import com.spicesfire.sayitenglish.SayIt;
import com.spicesfire.sayitenglish.platform.ModPacket;
import com.spicesfire.sayitenglish.platform.Platform;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1542;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3468;
import net.minecraft.class_5321;
import net.minecraft.class_7923;
import net.minecraft.server.MinecraftServer;

public final class ChallengeManager {
    private static final Random RANDOM = new Random();
    private static final Map<UUID, State> STATES = new HashMap<UUID, State>();
    private static final class_2960[] CUSTOM_STATS = new class_2960[]{class_3468.field_15381, class_3468.field_15391, class_3468.field_15410, class_3468.field_15378, class_3468.field_15420};
    private static final String[] CUSTOM_STEMS = new String[]{"sleep", "fish", "breed", "trade", "enchant"};

    private ChallengeManager() {
    }

    public static void init() {
        Platform p = Platform.I;
        p.onServerTickEnd(ChallengeManager::tick);
        p.onJoin(ChallengeManager::join);
        p.onLeave(player -> {
            State s = STATES.remove(player.method_5667());
            if (s != null) {
                s.progress.save(ChallengeManager.server(player), player.method_5667());
            }
        });
        p.onServerStopped(server -> STATES.clear());
        p.onBlockBroken((level, player, pos, state, be) -> {
            if (player instanceof class_3222) {
                class_3222 sp = (class_3222)player;
                State s = STATES.get(sp.method_5667());
                class_1792 item = state.method_26204().method_8389();
                if (s != null && item != class_1802.field_8162 && RANDOM.nextInt(4) == 0) {
                    s.pending = new Event("mine", "item:" + ChallengeManager.itemId(item));
                }
            }
        });
        p.serverboundPacket(Packets.Result.TYPE, ChallengeManager::result);
        p.serverboundPacket(Packets.Practice.TYPE, (pkt, player) -> ChallengeManager.practice(player));
        p.serverboundPacket(Packets.ChooseLanguage.TYPE, (pkt, player) -> ChallengeManager.chooseLanguage(player, pkt.code()));
    }

    private static void chooseLanguage(class_3222 player, String code) {
        if (!Languages.known((String)code)) {
            return;
        }
        State s = ChallengeManager.state(player);
        s.progress.language = code;
        s.queue.clear();
        s.cooldown.clear();
        s.active = null;
        s.progress.save(ChallengeManager.server(player), player.method_5667());
        ChallengeManager.sync(player);
    }

    private static MinecraftServer server(class_3222 player) {
        return player.field_13995;
    }

    private static State state(class_3222 player) {
        return STATES.computeIfAbsent(player.method_5667(), u -> {
            State s = new State();
            s.progress = Progress.load((MinecraftServer)ChallengeManager.server(player), (UUID)u);
            return s;
        });
    }

    private static void join(class_3222 player) {
        State s = ChallengeManager.state(player);
        if (Config.get().giveDictionary && !s.progress.gotDictionary && SayIt.DICTIONARY != null) {
            s.progress.gotDictionary = true;
            s.progress.starterDictionary = true;
            class_1799 book = new class_1799((class_1935)SayIt.DICTIONARY);
            if (!player.method_31548().method_7394(book)) {
                ChallengeManager.drop(player, book);
            }
        }
        ChallengeManager.sync(player);
        if (s.progress.language == null && Platform.I.canSendTo(player, Packets.AskLanguage.TYPE)) {
            Platform.I.sendToPlayer(player, (ModPacket)new Packets.AskLanguage(0));
        }
    }

    public static void sync(class_3222 player) {
        if (!Platform.I.canSendTo(player, Packets.Sync.TYPE)) {
            return;
        }
        Progress pr = ChallengeManager.state((class_3222)player).progress;
        ArrayList w = new ArrayList(pr.words().keySet());
        ArrayList sc = new ArrayList(pr.words().values());
        Platform.I.sendToPlayer(player, (ModPacket)new Packets.Sync(pr.language == null ? "" : pr.language, w, sc, pr.sentences, pr.lost));
    }

    private static void tick(MinecraftServer server) {
        long now = server.method_3780();
        if (now % 5L != 0L) {
            return;
        }
        for (class_3222 player : server.method_3760().method_14571()) {
            if (!Platform.I.canSendTo(player, Packets.Challenge.TYPE) || player.method_7325()) continue;
            State s = ChallengeManager.state(player);
            if (s.progress.language == null) continue;
            ChallengeManager.scanInventory(player, s, now);
            if (s.active != null) {
                if (now <= s.deadline) continue;
                ChallengeManager.finish(player, s, 0, "");
                continue;
            }
            if (!s.queue.isEmpty()) {
                String id = s.queue.poll();
                class_1792 item = ChallengeManager.item(id);
                if (item == null || ChallengeManager.count(player, item) <= 0) continue;
                ChallengeManager.start(player, s, now, "word", id, "", Config.get().wordSeconds);
                continue;
            }
            if (now % 40L != 0L) continue;
            Event e = ChallengeManager.watch(player, s, now);
            Config c = Config.get();
            if (!c.sentences || e == null || now - s.lastSentence <= (long)c.sentenceCooldown * 20L) continue;
            s.lastSentence = now;
            ChallengeManager.start(player, s, now, "sentence", e.stem(), e.arg(), c.sentenceSeconds);
        }
    }

    private static int checkDecay(class_3222 player, Progress pr, String id, long nowMs) {
        Integer lvl = pr.words().get(id);
        if (lvl == null || lvl <= 0) return 0;
        Long lastMs = pr.timestamps().get(id);
        if (lastMs == null || lastMs <= 0) {
            pr.timestamps().put(id, nowMs);
            return lvl;
        }
        long elapsedHours = (nowMs - lastMs) / (1000L * 3600L);
        int regressed = lvl;

        // Ebbinghaus Forgetting Curve Thresholds:
        if (lvl == 5 && elapsedHours >= 48) {
            regressed = 4;
            pr.timestamps().put(id, nowMs - (24L * 3600L * 1000L));
        } else if (lvl == 4 && elapsedHours >= 24) {
            regressed = 3;
            pr.timestamps().put(id, nowMs - (16L * 3600L * 1000L));
        } else if (lvl == 3 && elapsedHours >= 16) {
            regressed = 2;
            pr.timestamps().put(id, nowMs - (8L * 3600L * 1000L));
        } else if (lvl == 2 && elapsedHours >= 8) {
            regressed = 1;
            pr.timestamps().put(id, nowMs - (4L * 3600L * 1000L));
        } else if (lvl == 1 && elapsedHours >= 4) {
            regressed = 0;
            pr.timestamps().remove(id);
        }

        if (regressed != lvl) {
            if (regressed > 0) {
                pr.words().put(id, regressed);
            } else {
                pr.words().remove(id);
            }
            player.method_43496((class_2561)class_2561.method_43470("§6⚠ [Curva do Esquecimento] Faz " + elapsedHours + "h sem praticar! Fixação regrediu de " + lvl + "/5 para " + regressed + "/5."));
            pr.save(ChallengeManager.server(player), player.method_5667());
            ChallengeManager.sync(player);
        }
        return regressed;
    }

    private static void scanInventory(class_3222 player, State s, long now) {
        class_1661 inv = player.method_31548();
        List exempt = Config.get().exempt;
        long nowMs = System.currentTimeMillis();

        if (s.progress.starterDictionary && SayIt.DICTIONARY != null && ChallengeManager.count(player, SayIt.DICTIONARY) == 0) {
            s.progress.starterDictionary = false;
            s.progress.save(ChallengeManager.server(player), player.method_5667());
        }
        for (int i = 0; i < inv.method_5439(); ++i) {
            Long until;
            class_1799 st = inv.method_5438(i);
            if (st.method_7960() || (s.progress.starterDictionary && st.method_31574(SayIt.DICTIONARY))) continue;
            String id = ChallengeManager.itemId(st.method_7909());

            // 1. Check & apply Ebbinghaus forgetting decay
            ChallengeManager.checkDecay(player, s.progress, id, nowMs);

            // 2. Check spaced repetition cooldown
            until = s.cooldown.get(id);
            if (until != null && until > now) {
                continue;
            }

            if (s.queue.contains(id) || exempt.contains(id) || (s.active != null && !s.active.kind().equals("sentence") && s.active.subject().equals(id))) {
                continue;
            }

            s.queue.add(id);
        }
    }

    private static Event watch(class_3222 player, State s, long now) {
        boolean hurt;
        ArrayList<Event> events = new ArrayList<Event>();
        HashMap<class_1792, Integer> crafted = new HashMap<class_1792, Integer>();
        for (class_1792 class_17922 : class_7923.field_41178) {
            int v = player.method_14248().method_15025(class_3468.field_15370.method_14956(class_17922));
            if (v <= 0) continue;
            crafted.put(class_17922, v);
        }
        if (s.crafted != null) {
            for (Map.Entry entry : crafted.entrySet()) {
                if ((Integer)entry.getValue() <= s.crafted.getOrDefault(entry.getKey(), 0)) continue;
                events.add(new Event("craft", "item:" + ChallengeManager.itemId((class_1792)entry.getKey())));
            }
        }
        s.crafted = crafted;
        HashMap<class_1299<?>, Integer> killed = new HashMap<>();
        for (class_1299 type : class_7923.field_41177) {
            int v = player.method_14248().method_15025(class_3468.field_15403.method_14956(type));
            if (v <= 0) continue;
            killed.put(type, v);
        }
        if (s.killed != null) {
            for (Map.Entry<class_1299<?>, Integer> e : killed.entrySet()) {
                if ((Integer)e.getValue() <= s.killed.getOrDefault(e.getKey(), 0)) continue;
                events.add(new Event("kill", "entity:" + String.valueOf(class_7923.field_41177.method_10221(e.getKey()))));
            }
        }
        s.killed = killed;
        int[] nArray = new int[CUSTOM_STATS.length];
        for (int i = 0; i < CUSTOM_STATS.length; ++i) {
            nArray[i] = player.method_14248().method_15025(class_3468.field_15419.method_14956(CUSTOM_STATS[i]));
            if (s.custom == null || nArray[i] <= s.custom[i]) continue;
            events.add(new Event(CUSTOM_STEMS[i], ""));
        }
        s.custom = nArray;
        class_5321 dim = player.method_37908().method_27983();
        if (s.dimension != null && !dim.equals(s.dimension)) {
            events.add(new Event(dim.equals(class_1937.field_25180) ? "nether" : (dim.equals(class_1937.field_25181) ? "end" : "overworld"), ""));
        }
        s.dimension = dim;
        boolean bl = hurt = player.method_6032() <= 6.0f;
        if (hurt && !s.hurt) {
            events.add(new Event("hurt", ""));
        }
        s.hurt = hurt;
        if (s.pending != null) {
            events.add(s.pending);
            s.pending = null;
        }
        if (!events.isEmpty()) {
            s.quietSince = now;
            return (Event)events.get(RANDOM.nextInt(events.size()));
        }
        if (now - s.quietSince > (long)Config.get().sentenceCooldown * 40L) {
            s.quietSince = now;
            class_1937 level = player.method_37908();
            if (level.method_8419()) {
                return new Event("rain", "");
            }
            boolean night = level.method_23886();
            return new Event(night ? "night" : "day", "");
        }
        return null;
    }

    private static void start(class_3222 player, State s, long now, String kind, String subject, String arg, int seconds) {
        Packets.Challenge c;
        s.active = c = new Packets.Challenge(s.nextId++, kind, subject, arg, seconds, Config.get().passScore);
        s.deadline = now + ((long)seconds + 20L) * 20L;
        Platform.I.sendToPlayer(player, (ModPacket)c);
    }

    private static void practice(class_3222 player) {
        State s = ChallengeManager.state(player);
        if (s.active != null || s.progress.words().isEmpty()) {
            return;
        }
        ArrayList words = new ArrayList(s.progress.words().keySet());
        String id = (String)words.get(RANDOM.nextInt(words.size()));
        ChallengeManager.start(player, s, ChallengeManager.server(player).method_3780(), "practice", id, "", Config.get().wordSeconds);
    }

    private static void result(Packets.Result r, class_3222 player) {
        State s = ChallengeManager.state(player);
        if (s.active == null || s.active.id() != r.id()) {
            return;
        }
        ChallengeManager.finish(player, s, Math.max(0, Math.min(100, r.score())), r.heard());
    }

    private static void finish(class_3222 player, State s, int score, String heard) {
        Packets.Challenge c = s.active;
        s.active = null;
        Config cfg = Config.get();
        boolean passed = score >= c.passScore();
        Progress pr = s.progress;
        long now = ChallengeManager.server(player).method_3780();

        switch (c.kind()) {
            case "word": {
                class_1792 item = ChallengeManager.item(c.subject());
                int currentLevel = pr.words().getOrDefault(c.subject(), 0);

                if (passed || cfg.penalty.equals("none")) {
                    int nextLevel = Math.min(5, currentLevel + 1);
                    pr.words().put(c.subject(), nextLevel);
                    pr.timestamps().put(c.subject(), System.currentTimeMillis());

                    if (nextLevel >= 5) {
                        // Master Level 5/5: 30 minutes maintenance review (36000 ticks)
                        s.cooldown.put(c.subject(), now + 36000L);
                        player.method_43496((class_2561)class_2561.method_43470("§a✔ [MESTRE 5/5] Dominado com Maestria! Próxima revisão em 30 minutos de jogo."));
                    } else {
                        // Quick reinforcement intervals: 2s (lvl 1), 3s (lvl 2), 4s (lvl 3), 5s (lvl 4)
                        long cdTicks;
                        switch (nextLevel) {
                            case 1: cdTicks = 40L; break;   // 2s
                            case 2: cdTicks = 60L; break;   // 3s
                            case 3: cdTicks = 80L; break;   // 4s
                            case 4: cdTicks = 100L; break;  // 5s
                            default: cdTicks = 40L; break;
                        }
                        s.cooldown.put(c.subject(), now + cdTicks);
                        int remaining = 5 - nextLevel;
                        int nextSec = (int)(cdTicks / 20L);
                        player.method_43496((class_2561)class_2561.method_43470("§e★ [Fixação " + nextLevel + "/5] Correto (" + score + "%)! Próxima repetição em " + nextSec + "s (" + remaining + " etapa(s) restante(s))."));
                    }
                    break;
                }

                if (item == null) break;

                // Decision A3: On fail, take ONLY 1 item (never wipe whole inventory)
                int n = cfg.penalty.equals("drop") ? ChallengeManager.dropOne(player, item) : ChallengeManager.takeOne(player, item);
                pr.lost += n;

                // Decision A3: Regress fixation level by 1
                int regressed = Math.max(0, currentLevel - 1);
                if (regressed > 0) {
                    pr.words().put(c.subject(), regressed);
                } else {
                    pr.words().remove(c.subject());
                }
                pr.timestamps().put(c.subject(), System.currentTimeMillis());

                // Grace cooldown: 5 seconds (100 ticks) to breathe and speak again
                s.cooldown.put(c.subject(), now + 100L);

                player.method_43496((class_2561)class_2561.method_43470("§c✖ [Fixação " + regressed + "/5] Pronúncia não reconhecida (" + score + "%). 1 item foi removido."));
                break;
            }
            case "sentence": {
                if (!passed) break;
                ++pr.sentences;
                player.method_7255(cfg.sentenceXp);
                break;
            }
            case "practice": {
                if (!passed) break;
                int currentLevel = pr.words().getOrDefault(c.subject(), 0);
                int nextLevel = Math.min(5, currentLevel + 1);
                pr.words().put(c.subject(), nextLevel);
                pr.timestamps().put(c.subject(), System.currentTimeMillis());
                player.method_7255(Math.max(1, cfg.sentenceXp / 3));
                if (nextLevel >= 5) {
                    player.method_43496((class_2561)class_2561.method_43470("§a✔ [MESTRE 5/5] Dominado com Maestria na prática!"));
                } else {
                    player.method_43496((class_2561)class_2561.method_43470("§e★ [Fixação " + nextLevel + "/5] Treino concluído com sucesso!"));
                }
                break;
            }
        }
        pr.save(ChallengeManager.server(player), player.method_5667());
        ChallengeManager.sync(player);
    }

    private static int count(class_3222 player, class_1792 item) {
        class_1661 inv = player.method_31548();
        int n = 0;
        for (int i = 0; i < inv.method_5439(); ++i) {
            if (!inv.method_5438(i).method_31574(item)) continue;
            n += inv.method_5438(i).method_7947();
        }
        return n;
    }

    private static int takeOne(class_3222 player, class_1792 item) {
        class_1799 taken = ChallengeManager.extractOne(player, item);
        return taken.method_7960() ? 0 : 1;
    }

    private static int dropOne(class_3222 player, class_1792 item) {
        class_1799 taken = ChallengeManager.extractOne(player, item);
        if (!taken.method_7960()) {
            ChallengeManager.drop(player, taken);
            return 1;
        }
        return 0;
    }

    private static class_1799 extractOne(class_3222 player, class_1792 item) {
        // 1. Check cursor
        class_1799 carried = player.field_7512.method_34255();
        if (carried.method_31574(item) && !carried.method_7960()) {
            carried.method_7933(1);
            if (carried.method_7960()) {
                player.field_7512.method_34254(class_1799.field_8037);
            }
            player.field_7512.method_7623();
            return new class_1799((class_1935)item, 1);
        }
        class_1661 inv = player.method_31548();
        // 2. Check active slot
        int sel = inv.field_7545;
        class_1799 selStack = inv.method_5438(sel);
        if (selStack.method_31574(item) && !selStack.method_7960()) {
            selStack.method_7933(1);
            if (selStack.method_7960()) {
                inv.method_5447(sel, class_1799.field_8037);
            }
            player.field_7512.method_7623();
            return new class_1799((class_1935)item, 1);
        }
        // 3. Check entire inventory
        for (int i = 0; i < inv.method_5439(); ++i) {
            class_1799 st = inv.method_5438(i);
            if (st.method_31574(item) && !st.method_7960()) {
                st.method_7933(1);
                if (st.method_7960()) {
                    inv.method_5447(i, class_1799.field_8037);
                }
                player.field_7512.method_7623();
                return new class_1799((class_1935)item, 1);
            }
        }
        return class_1799.field_8037;
    }

    private static int deleteAll(class_3222 player, class_1792 item) {
        int n = 0;
        for (class_1799 st : ChallengeManager.takeAll(player, item)) {
            n += st.method_7947();
        }
        return n;
    }

    private static int dropAll(class_3222 player, class_1792 item) {
        int n = 0;
        for (class_1799 st : ChallengeManager.takeAll(player, item)) {
            n += st.method_7947();
            ChallengeManager.drop(player, st);
        }
        return n;
    }

    private static List<class_1799> takeAll(class_3222 player, class_1792 item) {
        ArrayList<class_1799> taken = new ArrayList<class_1799>();
        class_1661 inv = player.method_31548();
        for (int i = 0; i < inv.method_5439(); ++i) {
            if (!inv.method_5438(i).method_31574(item)) continue;
            taken.add(inv.method_5438(i));
            inv.method_5447(i, class_1799.field_8037);
        }
        class_1799 carried = player.field_7512.method_34255();
        if (carried.method_31574(item)) {
            taken.add(carried);
            player.field_7512.method_34254(class_1799.field_8037);
        }
        player.field_7512.method_7623();
        return taken;
    }

    private static void drop(class_3222 player, class_1799 stack) {
        class_1542 e = new class_1542(player.method_37908(), player.method_23317(), player.method_23320() - 0.3, player.method_23321(), stack);
        e.method_6982(40);
        player.method_37908().method_8649((class_1297)e);
    }

    public static String itemId(class_1792 item) {
        return class_7923.field_41178.method_10221(item).toString();
    }

    public static class_1792 item(String id) {
        class_2960 key = class_2960.method_12829((String)id);
        if (key == null) {
            return null;
        }
        class_1792 item = (class_1792)class_7923.field_41178.method_10223(key);
        return item == class_1802.field_8162 ? null : item;
    }

    public static void reset(class_3222 player) {
        State s = ChallengeManager.state(player);
        s.progress.words().clear();
        s.progress.timestamps().clear();
        s.progress.sentences = 0;
        s.progress.lost = 0;
        s.queue.clear();
        s.progress.save(ChallengeManager.server(player), player.method_5667());
        ChallengeManager.sync(player);
    }

    public static Progress progress(class_3222 player) {
        return ChallengeManager.state((class_3222)player).progress;
    }

    public static void requestPractice(class_3222 player) {
        ChallengeManager.practice(player);
    }

    private static final class State {
        Progress progress;
        final Deque<String> queue = new ArrayDeque<String>();
        Packets.Challenge active;
        long deadline;
        int nextId = 1;
        long lastSentence;
        final Map<String, Long> cooldown = new HashMap<String, Long>();
        Map<class_1792, Integer> crafted;
        Map<class_1299<?>, Integer> killed;
        int[] custom;
        class_5321<class_1937> dimension;
        boolean hurt;
        Event pending;
        long quietSince;

        private State() {
        }
    }

    private record Event(String stem, String arg) {
    }
}
