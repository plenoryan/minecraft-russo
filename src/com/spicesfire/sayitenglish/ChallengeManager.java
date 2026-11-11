package com.spicesfire.sayitenglish;

import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.Languages;
import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.Progress;
import com.spicesfire.sayitenglish.SayIt;
import com.spicesfire.sayitenglish.platform.ModPacket;
import com.spicesfire.sayitenglish.platform.Platform;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1542;
import net.minecraft.class_1661;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2338;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3468;
import net.minecraft.class_5321;
import net.minecraft.class_7157;
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
        CoopManager.init();
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
        CoopManager.syncCoop(player);
    }

    private static void tick(MinecraftServer server) {
        long now = server.method_3780();
        if (now % 5L != 0L) {
            return;
        }
        CoopManager.tick(server, now);
        for (class_3222 player : server.method_3760().method_14571()) {
            if (!Platform.I.canSendTo(player, Packets.Challenge.TYPE) || player.method_7325()) continue;
            State s = ChallengeManager.state(player);
            if (s.progress.language == null) continue;
            if (s.coopBusy) continue;
            ChallengeManager.scanInventory(player, s, now);
            if (s.active != null) {
                if (now <= s.deadline) continue;
                ChallengeManager.finish(player, s, 0, "");
                continue;
            }
            if (now - s.lastChallengeEnd < 300L) continue;
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
                pr.errors().remove(id);
            }
            pr.reps().put(id, 0);
            player.method_43496((class_2561)class_2561.method_43470("\u00a76[Revis\u00e3o] Inativo: " + elapsedHours + "h. Fixa\u00e7\u00e3o ajustada: " + lvl + "/5 -> " + regressed + "/5."));
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

        boolean swimming = player.method_5799();
        if (swimming && !s.swimming) {
            events.add(new Event("swim", ""));
        }
        s.swimming = swimming;

        boolean onFire = player.method_20802() > 0;
        if (onFire && !s.onFire) {
            events.add(new Event("fire", ""));
        }
        s.onFire = onFire;

        boolean riding = player.method_5854() != null;
        if (riding && !s.riding) {
            events.add(new Event("ride", ""));
        }
        s.riding = riding;

        boolean sneaking = player.method_5715();
        if (sneaking && !s.sneaking) {
            events.add(new Event("sneak", ""));
        }
        s.sneaking = sneaking;

        boolean climbing = player.method_6101();
        if (climbing && !s.climbing) {
            events.add(new Event("climb", ""));
        }
        s.climbing = climbing;

        boolean flying = player.method_6128();
        if (flying && !s.flying) {
            events.add(new Event("fly", ""));
        }
        s.flying = flying;

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

    public static boolean isRare(String id) {
        if (id == null) return false;
        String lower = id.toLowerCase();
        return lower.contains("diamond")
            || lower.contains("netherite")
            || lower.contains("emerald")
            || lower.contains("turtle")
            || lower.contains("scute")
            || lower.contains("golden_apple")
            || lower.contains("enchanted_golden_apple")
            || lower.contains("trident")
            || lower.contains("elytra")
            || lower.contains("totem_of_undying")
            || lower.contains("nether_star")
            || lower.contains("beacon")
            || lower.contains("heart_of_the_sea")
            || lower.contains("nautilus_shell")
            || lower.contains("conduit")
            || lower.contains("dragon_egg")
            || lower.contains("dragon_breath")
            || lower.contains("wither_skeleton_skull")
            || lower.contains("shulker")
            || lower.contains("saddle")
            || lower.contains("name_tag")
            || lower.contains("music_disc")
            || lower.contains("echo_shard")
            || lower.contains("recovery_compass")
            || lower.contains("heavy_core")
            || lower.contains("mace");
    }

    public static int targetReps(int currentStars, boolean rare) {
        if (rare) {
            return 7;
        } else {
            switch (currentStars) {
                case 0: return 5;
                case 1: return 4;
                case 2: return 3;
                case 3: return 2;
                case 4: return 1;
                default: return 1;
            }
        }
    }

    public static long repCooldownTicks(int currentStars, int repIndex, boolean rare) {
        if (rare) {
            switch (repIndex) {
                case 1: return 40L;  // 2s
                case 2: return 60L;  // 3s
                case 3: return 80L;  // 4s
                case 4: return 100L; // 5s
                case 5: return 100L; // 5s
                case 6: return 120L; // 6s
                default: return 120L; // 6s
            }
        }
        if (currentStars == 0) {
            switch (repIndex) {
                case 1: return 200L; // 10s
                case 2: return 300L; // 15s
                case 3: return 400L; // 20s
                case 4: return 500L; // 25s
                default: return 600L; // 30s
            }
        } else if (currentStars == 1) {
            switch (repIndex) {
                case 1: return 200L; // 10s
                case 2: return 300L; // 15s
                case 3: return 400L; // 20s
                default: return 500L; // 25s
            }
        } else if (currentStars == 2) {
            switch (repIndex) {
                case 1: return 600L; // 30s
                default: return 900L; // 45s
            }
        } else if (currentStars == 3) {
            return 1200L; // 60s
        } else {
            return 1800L; // 90s
        }
    }

    public static long starCooldownTicks(int newStars, boolean rare) {
        if (rare) {
            switch (newStars) {
                case 1: return 3600L;  // 3 min
                case 2: return 7200L;  // 6 min
                case 3: return 14400L; // 12 min
                case 4: return 30000L; // 25 min
                case 5:
                default: return 54000L; // 45 min
            }
        } else {
            switch (newStars) {
                case 1: return 3000L;  // 2.5 min
                case 2: return 6000L;  // 5 min
                case 3: return 12000L; // 10 min
                case 4: return 24000L; // 20 min
                case 5:
                default: return 42000L; // 35 min
            }
        }
    }

    private static void finish(class_3222 player, State s, int score, String heard) {
        Packets.Challenge c = s.active;
        s.active = null;
        Config cfg = Config.get();
        boolean passed = score >= c.passScore();
        Progress pr = s.progress;
        boolean isRu = "ru".equals(pr.language);
        Set<Character> oldUnlocked = isRu ? Alphabet.computeUnlockedLetters(pr.words(), pr.letterHeat()) : Collections.emptySet();
        long now = ChallengeManager.server(player).method_3780();
        s.lastChallengeEnd = now;

        switch (c.kind()) {
            case "word": {
                class_1792 item = ChallengeManager.item(c.subject());
                int currentStars = pr.words().getOrDefault(c.subject(), 0);
                boolean rare = ChallengeManager.isRare(c.subject());
                int neededReps = ChallengeManager.targetReps(currentStars, rare);

                if (rare) {
                    // RARE ITEM TRIAL (ALL STARS): 7 attempts battery.
                    // If player fails 4 times (majority of 7), they lose 1 rare item!
                    // ZERO loss protection across all stars.
                    int successes = pr.reps().getOrDefault(c.subject(), 0);
                    int errors = pr.errors().getOrDefault(c.subject(), 0);
                    int attemptsDone = successes + errors;
                    int targetStar = Math.min(5, currentStars + 1);

                    if (passed || cfg.penalty.equals("none")) {
                        successes++;
                        attemptsDone++;
                        pr.reps().put(c.subject(), successes);
                        pr.timestamps().put(c.subject(), System.currentTimeMillis());
                        if (!pr.words().containsKey(c.subject())) {
                            pr.words().put(c.subject(), 0);
                        }

                        if (attemptsDone >= 7) {
                            // 7 attempts completed with errors < 4 (so successes >= 4, majority achieved)!
                            int newStars = targetStar;
                            pr.words().put(c.subject(), newStars);
                            pr.reps().put(c.subject(), 0);
                            pr.errors().remove(c.subject());

                            long cdTicks = ChallengeManager.starCooldownTicks(newStars, true);
                            s.cooldown.put(c.subject(), now + cdTicks);
                            int delayMin = Math.max(1, (int)(cdTicks / 1200L));

                            if (newStars == 1) {
                                player.method_43496((class_2561)class_2561.method_43470(
                                    "\u00a7a[Progresso] Item Raro: Estrela 1/5 (" + successes + "/7 acertos). Pr\u00f3ximo teste em " + delayMin + " min."
                                ));
                            } else if (newStars >= 5) {
                                player.method_43496((class_2561)class_2561.method_43470(
                                    "\u00a76[Progresso] Item Raro: Estrela 5/5 (" + successes + "/7 acertos). Revis\u00e3o em " + delayMin + " min."
                                ));
                            } else {
                                player.method_43496((class_2561)class_2561.method_43470(
                                    "\u00a7a[Progresso] Item Raro: Estrela " + newStars + "/5 (" + successes + "/7 acertos). Pr\u00f3ximo teste em " + delayMin + " min."
                                ));
                            }
                        } else {
                            long cdTicks = ChallengeManager.repCooldownTicks(currentStars, attemptsDone, true);
                            s.cooldown.put(c.subject(), now + cdTicks);
                            int nextSec = Math.max(1, (int)(cdTicks / 20L));
                            player.method_7353((class_2561)class_2561.method_43470(
                                "\u00a7e[Item Raro " + attemptsDone + "/7] Correto (" + score + "%) \u2022 Acertos: " + successes + " \u2022 Pr\u00f3ximo em " + nextSec + "s"
                            ), true);
                        }
                        break;
                    }

                    // Failed pronunciation on rare item:
                    if (item == null) break;

                    errors++;
                    attemptsDone++;
                    pr.errors().put(c.subject(), errors);
                    pr.timestamps().put(c.subject(), System.currentTimeMillis());

                    if (errors >= 4) {
                        // 4 errors = majority of 7! ITEM IS LOST!
                        int n = ChallengeManager.applyPenaltyOne(player, item);
                        pr.lost += n;
                        pr.reps().remove(c.subject());
                        pr.errors().remove(c.subject());

                        int regressedStars = Math.max(0, currentStars - 1);
                        if (regressedStars == 0) {
                            pr.words().remove(c.subject());
                        } else {
                            pr.words().put(c.subject(), regressedStars);
                        }

                        s.cooldown.put(c.subject(), now + 300L); // 15s cooldown

                        String regrMsg = regressedStars == 0
                            ? "Progresso zerado."
                            : "Fixa\u00e7\u00e3o: Estrela " + regressedStars + "/5.";

                        String actionMsg = "drop".equalsIgnoreCase(cfg.penalty)
                            ? "1 item dropado."
                            : "1 item removido.";

                        player.method_43496((class_2561)class_2561.method_43470(
                            "\u00a7c[Falha] Item Raro: 4 erros atingidos (" + score + "%). " + actionMsg + " " + regrMsg
                        ));
                    } else if (attemptsDone >= 7) {
                        // 7 attempts completed with errors < 4 (so successes >= 4, majority achieved)!
                        int newStars = targetStar;
                        pr.words().put(c.subject(), newStars);
                        pr.reps().put(c.subject(), 0);
                        pr.errors().remove(c.subject());

                        long cdTicks = ChallengeManager.starCooldownTicks(newStars, true);
                        s.cooldown.put(c.subject(), now + cdTicks);
                        int delayMin = Math.max(1, (int)(cdTicks / 1200L));

                        player.method_43496((class_2561)class_2561.method_43470(
                            "\u00a7a[Progresso] Item Raro: Estrela " + newStars + "/5 (" + successes + "/7 acertos). Pr\u00f3ximo teste em " + delayMin + " min."
                        ));
                    } else {
                        // Not lost yet, warning with strikes remaining
                        s.cooldown.put(c.subject(), now + 100L); // 5s grace interval
                        player.method_7353((class_2561)class_2561.method_43470(
                            "\u00a76[Item Raro " + attemptsDone + "/7] Erro (" + score + "%) \u2022 Falhas: " + errors + "/4 \u2022 Pr\u00f3ximo em 5s"
                        ), true);
                    }
                    break;
                }

                int currentReps = pr.reps().getOrDefault(c.subject(), 0);

                if (passed || cfg.penalty.equals("none")) {
                    int nextReps = currentReps + 1;
                    pr.timestamps().put(c.subject(), System.currentTimeMillis());

                    if (nextReps >= neededReps) {
                        int newStars = Math.min(5, currentStars + 1);
                        pr.words().put(c.subject(), newStars);
                        pr.reps().put(c.subject(), 0);

                        long cdTicks = ChallengeManager.starCooldownTicks(newStars, rare);
                        s.cooldown.put(c.subject(), now + cdTicks);
                        int delayMin = Math.max(1, (int)(cdTicks / 1200L));

                        if (newStars == 1) {
                            player.method_43496((class_2561)class_2561.method_43470(
                                "\u00a7a[Progresso] Estrela 1/5 conclu\u00edda. Pr\u00f3ximo teste em " + delayMin + " min."
                            ));
                        } else if (newStars >= 5) {
                            player.method_43496((class_2561)class_2561.method_43470(
                                "\u00a76[Progresso] Estrela 5/5 (Dom\u00ednio). Revis\u00e3o em " + delayMin + " min."
                            ));
                        } else {
                            player.method_43496((class_2561)class_2561.method_43470(
                                "\u00a7a[Progresso] Estrela " + newStars + "/5 conclu\u00edda. Pr\u00f3ximo teste em " + delayMin + " min."
                            ));
                        }
                    } else {
                        pr.reps().put(c.subject(), nextReps);
                        if (!pr.words().containsKey(c.subject())) {
                            pr.words().put(c.subject(), 0);
                        }
                        long cdTicks = ChallengeManager.repCooldownTicks(currentStars, nextReps, rare);
                        s.cooldown.put(c.subject(), now + cdTicks);
                        int nextSec = Math.max(1, (int)(cdTicks / 20L));
                        player.method_7353((class_2561)class_2561.method_43470(
                            "\u00a7e[Teste " + nextReps + "/" + neededReps + "] Correto (" + score + "%) \u2022 Pr\u00f3ximo em " + nextSec + "s"
                        ), true);
                    }
                    break;
                }

                if (item == null) break;

                int n = ChallengeManager.applyPenaltyOne(player, item);
                pr.lost += n;

                int regressedReps = currentReps - 1;
                int regressedStars = currentStars;
                if (regressedReps < 0) {
                    if (regressedStars > 0) {
                        regressedStars--;
                        pr.words().put(c.subject(), regressedStars);
                        regressedReps = Math.max(0, ChallengeManager.targetReps(regressedStars, false) - 1);
                    } else {
                        regressedReps = 0;
                        pr.words().remove(c.subject());
                    }
                }
                pr.reps().put(c.subject(), regressedReps);
                pr.timestamps().put(c.subject(), System.currentTimeMillis());

                // Grace cooldown: 5 seconds (100 ticks)
                s.cooldown.put(c.subject(), now + 100L);

                String actionMsg = "drop".equalsIgnoreCase(cfg.penalty)
                    ? "1 item dropado."
                    : "1 item removido.";

                player.method_43496((class_2561)class_2561.method_43470(
                    "\u00a7c[Falha] Pron\u00fancia: " + score + "% (meta: " + c.passScore() + "%). " + actionMsg + " Fixa\u00e7\u00e3o: Estrela " + regressedStars + "/5."
                ));
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
                int currentStars = pr.words().getOrDefault(c.subject(), 0);
                int nextStars = Math.min(5, currentStars + 1);
                pr.words().put(c.subject(), nextStars);
                pr.reps().put(c.subject(), 0);
                pr.timestamps().put(c.subject(), System.currentTimeMillis());
                player.method_7255(Math.max(1, cfg.sentenceXp / 3));
                player.method_7353((class_2561)class_2561.method_43470(
                    "\u00a7a[Pr\u00e1tica] Estrela " + nextStars + "/5 (+XP)"
                ), true);
                break;
            }
            case "coop_dialogue":
            case "coop": {
                CoopManager.handleChallengeResult(player, c, score, heard, passed);
                break;
            }
        }
        if (isRu) {
            String ruSubject = Alphabet.getRuWord(c.subject());
            if (passed) {
                pr.addHeat(ruSubject, 1);
            }
            Set<Character> newUnlocked = Alphabet.computeUnlockedLetters(pr.words(), pr.letterHeat());

            // Check if any brand new Cyrillic letters were unlocked
            for (char ch : newUnlocked) {
                if (Character.isLowerCase(ch) && !oldUnlocked.contains(ch)) {
                    int lvl = Alphabet.LETTER_LEVEL.getOrDefault(ch, 1);
                    String name = Alphabet.LEVEL_NAME[lvl];
                    String mnem = Alphabet.LEVEL_MNEM[lvl];
                    player.method_43496((class_2561)class_2561.method_43470(
                        "\u00a76[Alfabeto] Letra ativa: \u00a7e" + name + " \u00a77\u2022 " + mnem
                    ));
                }
            }

            int oldCount = Alphabet.getLetterCount(oldUnlocked);
            int newCount = Alphabet.getLetterCount(newUnlocked);
            if (newCount > oldCount) {
                try {
                    MinecraftServer srv = ChallengeManager.server(player);
                    if (srv != null) {
                        String pName = player.method_5477().getString();
                        if (newCount >= 6 && oldCount < 6) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 01_alfabeto_gemeas " + pName);
                        }
                        if (newCount >= 12 && oldCount < 12) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 02_alfabeto_falsos_amigos " + pName);
                        }
                        if (newCount >= 19 && oldCount < 19) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 03_alfabeto_construtores " + pName);
                        }
                        if (newCount >= 26 && oldCount < 26) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 04_alfabeto_chiantes " + pName);
                        }
                        if (newCount >= 32 && oldCount < 32) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 05_alfabeto_iodizadas_sinais " + pName);
                        }
                    }
                } catch (Exception ignored) {
                }
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

    private static int deleteOne(class_3222 player, class_1792 item) {
        // 1. Check cursor stack
        class_1799 carried = player.field_7512.method_34255();
        if (carried != null && !carried.method_7960() && carried.method_31574(item)) {
            int count = carried.method_7947();
            if (count <= 1) {
                player.field_7512.method_34254(class_1799.field_8037);
            } else {
                carried.method_7939(count - 1);
            }
            player.field_7512.method_7623();
            player.field_7498.method_7623();
            return 1;
        }

        class_1661 inv = player.method_31548();
        // 2. Check active hotbar slot
        int sel = inv.field_7545;
        class_1799 selStack = inv.method_5438(sel);
        if (selStack != null && !selStack.method_7960() && selStack.method_31574(item)) {
            int count = selStack.method_7947();
            if (count <= 1) {
                inv.method_5447(sel, class_1799.field_8037);
            } else {
                selStack.method_7939(count - 1);
            }
            player.field_7512.method_7623();
            player.field_7498.method_7623();
            return 1;
        }

        // 3. Check entire inventory (including armor and offhand)
        for (int i = 0; i < inv.method_5439(); ++i) {
            class_1799 st = inv.method_5438(i);
            if (st != null && !st.method_7960() && st.method_31574(item)) {
                int count = st.method_7947();
                if (count <= 1) {
                    inv.method_5447(i, class_1799.field_8037);
                } else {
                    st.method_7939(count - 1);
                }
                player.field_7512.method_7623();
                player.field_7498.method_7623();
                return 1;
            }
        }
        return 0;
    }

    private static int takeOne(class_3222 player, class_1792 item) {
        return ChallengeManager.deleteOne(player, item);
    }

    private static int dropOne(class_3222 player, class_1792 item) {
        int n = ChallengeManager.deleteOne(player, item);
        if (n > 0) {
            ChallengeManager.drop(player, new class_1799((class_1935)item, 1));
        }
        return n;
    }

    private static int applyPenaltyOne(class_3222 player, class_1792 item) {
        Config cfg = Config.get();
        if ("none".equalsIgnoreCase(cfg.penalty)) {
            return 0;
        }
        if ("drop".equalsIgnoreCase(cfg.penalty)) {
            return ChallengeManager.dropOne(player, item);
        }
        return ChallengeManager.deleteOne(player, item);
    }

    private static int deleteAll(class_3222 player, class_1792 item) {
        int n = 0;
        for (class_1799 st : ChallengeManager.takeAll(player, item)) {
            n += st.method_7947();
        }
        return n;
    }

    private static int dropAll(class_3222 player, class_1792 item) {
        return ChallengeManager.deleteAll(player, item);
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
        player.field_7498.method_7623();
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
        s.progress.reps().clear();
        s.progress.errors().clear();
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
        boolean swimming;
        boolean onFire;
        boolean riding;
        boolean sneaking;
        boolean climbing;
        boolean flying;
        Event pending;
        long quietSince;
        boolean coopBusy;
        long lastChallengeEnd;

        private State() {
        }
    }

    private record Event(String stem, String arg) {
    }

    // =========================================================================
    // COOP MANAGER: MULTIPLAYER COOPERATIVE MECHANICS (Requirement R1)
    // =========================================================================

    public static final class CoopManager {
        public static final class ActiveTask {
            final String id;
            final String type; // "FOLLOW", "WAYPOINT", "REQUEST_WOOD", "REQUEST_FOOD", "HELP", "LAVA", "PARKOUR", "RALLY", "DIALOGUE"
            final UUID initiatorUuid;
            final Set<UUID> targetUuids = new HashSet<UUID>();
            double targetX, targetY, targetZ;
            final long startTick;
            long deadlineTick;
            int phase;
            final Map<String, Object> data = new HashMap<String, Object>();

            ActiveTask(String id, String type, UUID initiatorUuid, double tx, double ty, double tz, long startTick, long deadlineTick, int phase) {
                this.id = id;
                this.type = type;
                this.initiatorUuid = initiatorUuid;
                this.targetX = tx;
                this.targetY = ty;
                this.targetZ = tz;
                this.startTick = startTick;
                this.deadlineTick = deadlineTick;
                this.phase = phase;
            }
        }

        private static final Map<String, ActiveTask> ACTIVE_TASKS = new HashMap<String, ActiveTask>();
        private static long lastDialogueCheckTick = 0L;
        private static int taskCounter = 1;

        // Dialogue repertory: {Theme, Part1Ru, Part1Phonetic, Part1Pt, Part2Ru, Part2Phonetic, Part2Pt, Keyword}
        private static final String[][] DIALOGUES = new String[][]{
            {
                "Sauda\u00e7\u00e3o",
                "\u041f\u0440\u0438\u0432\u0435\u0442, \u043a\u0430\u043a \u0434\u0435\u043b\u0430?",
                "[Privyet, kak dyela?]",
                "Ol\u00e1, como vai?",
                "\u0412\u0441\u0435 \u043e\u0442\u043b\u0438\u0447\u043d\u043e, \u0441\u043f\u0430\u0441\u0438\u0431\u043e!",
                "[Vsyo otlichno, spasibo!]",
                "Tudo \u00f3timo, obrigado!",
                "\u043f\u0440\u0438\u0432\u0435\u0442"
            },
            {
                "Minera\u00e7\u00e3o",
                "\u041a\u0443\u0434\u0430 \u043c\u044b \u0438\u0434\u0435\u043c?",
                "[Kuda my idyom?]",
                "Onde estamos indo?",
                "\u041c\u044b \u0438\u0434\u0435\u043c \u0432 \u0448\u0430\u0445\u0442\u0443!",
                "[My idyom v shahtu!]",
                "Vamos para a mina!",
                "\u0448\u0430\u0445\u0442\u0430"
            },
            {
                "Batalha",
                "\u0422\u044b \u0433\u043e\u0442\u043e\u0432 \u043a \u0431\u043e\u044e?",
                "[Ty gotov k boyu?]",
                "Voc\u00ea est\u00e1 pronto para a luta?",
                "\u0414\u0430, \u044f \u0433\u043e\u0442\u043e\u0432!",
                "[Da, ya gotov!]",
                "Sim, estou pronto!",
                "\u0431\u043e\u0439"
            },
            {
                "Recursos",
                "\u0423 \u0442\u0435\u0431\u044f \u0435\u0441\u0442\u044c \u0444\u0430\u043a\u0435\u043b\u044b?",
                "[U tebya yest' fakely?]",
                "Voc\u00ea tem tochas?",
                "\u0414\u0430, \u0443 \u043c\u0435\u043d\u044f \u0435\u0441\u0442\u044c \u0444\u0430\u043a\u0435\u043b\u044b!",
                "[Da, u menya yest' fakely!]",
                "Sim, eu tenho tochas!",
                "\u0444\u0430\u043a\u0435\u043b"
            },
            {
                "Perigo",
                "\u041e\u0441\u0442\u043e\u0440\u043e\u0436\u043d\u043e, \u043a\u0440\u0438\u043f\u0435\u0440!",
                "[Ostorozhno, kriper!]",
                "Cuidado, um creeper!",
                "\u042f \u0432\u0438\u0436\u0443 \u0435\u0433\u043e!",
                "[Ya vizhu yego!]",
                "Eu estou vendo ele!",
                "\u043a\u0440\u0438\u043f\u0435\u0440"
            }
        };

        public static void init() {
            Platform.I.clientboundPacket(Packets.CoopCue.TYPE);
            Platform.I.clientboundPacket(Packets.CoopSync.TYPE);
            Platform.I.serverboundPacket(Packets.CoopInvoke.TYPE, CoopManager::onCoopInvoke);
            Platform.I.commands((dispatcher, dedicated, env) -> registerCommands(dispatcher));
        }

        public static void onCoopInvoke(Packets.CoopInvoke pkt, class_3222 player) {
            if (pkt == null || player == null) return;
            String act = pkt.action();
            if ("coop_busy:true".equals(act)) {
                State s = ChallengeManager.state(player);
                s.coopBusy = true;
                if (s.active != null) {
                    if ("word".equals(s.active.kind()) && s.active.subject() != null) {
                        s.queue.add(s.active.subject());
                    }
                    s.active = null;
                }
                return;
            } else if ("coop_busy:false".equals(act)) {
                State s = ChallengeManager.state(player);
                s.coopBusy = false;
                s.lastChallengeEnd = ChallengeManager.server(player).method_3780();
                return;
            }
            startPhraseInvocation(player, pkt.action());
        }

        public static void syncCoop(class_3222 player) {
            if (player == null || !Platform.I.canSendTo(player, Packets.CoopSync.TYPE)) return;
            Progress pr = ChallengeManager.state(player).progress;
            Packets.CoopSync syncPkt = new Packets.CoopSync(
                pr.coopScore,
                pr.coopCompletedChallenges,
                pr.coopRankTitle(),
                "",
                0,
                pr.coopRankTier()
            );
            Platform.I.sendToPlayer(player, (ModPacket)syncPkt);
        }

        public static void awardCoopPoints(class_3222 player, int points, String reason) {
            if (player == null || points <= 0) return;
            State s = ChallengeManager.state(player);
            int oldTier = s.progress.coopRankTier();
            s.progress.addCoopScore(points);
            s.progress.incrementCoopCompleted();
            int newTier = s.progress.coopRankTier();

            player.method_5783(class_3417.field_14725.comp_349(), 1.0f, 1.2f);
            player.method_43496((class_2561)class_2561.method_43470(
                "\u00a7a[\u272a COOP +" + points + " pts] \u00a7f" + reason + " \u00a77(Total: " + s.progress.coopScore + " pts)"
            ));

            if (newTier > oldTier) {
                String title = s.progress.coopRankTitle();
                String broadcast = "\u00a76[\u2605 EVOLU\u00c7\u00c3O COOPERATIVA] \u00a7e" + player.method_5477().getString() +
                    " \u00a7falcan\u00e7ou a patente " + title + " \u00a7a(N\u00edvel " + newTier + "/5)!";
                MinecraftServer srv = ChallengeManager.server(player);
                if (srv != null) {
                    for (class_3222 p : srv.method_3760().method_14571()) {
                        p.method_43496((class_2561)class_2561.method_43470(broadcast));
                        p.method_5783(class_3417.field_14725.comp_349(), 1.0f, 1.0f);
                    }
                }
            }

            s.progress.save(ChallengeManager.server(player), player.method_5667());
            syncCoop(player);
        }

        public static void sendBilingualCue(
            class_3222 initiator,
            List<class_3222> targets,
            String action,
            String ruText,
            String phonetic,
            String ptText,
            String objective,
            double x,
            double y,
            double z,
            int seconds
        ) {
            String senderName = initiator != null ? initiator.method_5477().getString() : "Sistema";
            Packets.CoopCue pkt = new Packets.CoopCue(
                senderName,
                action,
                ruText,
                phonetic,
                ptText,
                objective,
                x,
                y,
                z,
                seconds
            );

            String chatMsg = "\u00a76[COOP] \u00a7e" + senderName + "\u00a7f: \u00a7e" + ruText +
                (phonetic.isEmpty() ? "" : " \u00a77" + phonetic) +
                " \u00a7f- \u00a7b\"" + ptText + "\"";
            String objMsg = "\u00a7a[\u25b6 Objetivo] \u00a7f" + objective + " \u00a77(" + seconds + "s)";
            String actionMsg = "\u00a76[COOP] \u00a7e" + ruText + " \u00a7f- \u00a7b" + ptText;

            for (class_3222 target : targets) {
                if (target == null) continue;
                if (Platform.I.canSendTo(target, Packets.CoopCue.TYPE)) {
                    Platform.I.sendToPlayer(target, (ModPacket)pkt);
                }
                target.method_7353((class_2561)class_2561.method_43470(actionMsg), true);
                target.method_43496((class_2561)class_2561.method_43470(chatMsg));
                target.method_43496((class_2561)class_2561.method_43470(objMsg));
                target.method_5783(class_3417.field_14622.comp_349(), 1.0f, 1.4f);
            }
        }

        public static List<class_3222> getNearbyTeammates(class_3222 player, double maxDistance) {
            List<class_3222> list = new ArrayList<class_3222>();
            if (player == null) return list;
            MinecraftServer srv = ChallengeManager.server(player);
            if (srv == null) return list;
            for (class_3222 other : srv.method_3760().method_14571()) {
                if (other.method_5667().equals(player.method_5667())) continue;
                if (!other.method_37908().equals(player.method_37908())) continue;
                if (distance(player, other) <= maxDistance) {
                    list.add(other);
                }
            }
            return list;
        }

        public static double distance(class_3222 p1, class_3222 p2) {
            if (p1 == null || p2 == null || !p1.method_37908().equals(p2.method_37908())) return Double.MAX_VALUE;
            double dx = p1.method_23317() - p2.method_23317();
            double dy = p1.method_23318() - p2.method_23318();
            double dz = p1.method_23321() - p2.method_23321();
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        public static double distanceToPoint(class_3222 p, double x, double y, double z) {
            if (p == null) return Double.MAX_VALUE;
            double dx = p.method_23317() - x;
            double dy = p.method_23318() - y;
            double dz = p.method_23321() - z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        public static boolean hasWood(class_3222 player) {
            if (player == null) return false;
            class_1661 inv = player.method_31548();
            for (int i = 0; i < inv.method_5439(); ++i) {
                class_1799 stack = inv.method_5438(i);
                if (stack.method_7960()) continue;
                String id = ChallengeManager.itemId(stack.method_7909());
                if (id.contains("log") || id.contains("wood") || id.contains("planks")) {
                    return true;
                }
            }
            return false;
        }

        public static boolean hasFood(class_3222 player) {
            if (player == null) return false;
            class_1661 inv = player.method_31548();
            for (int i = 0; i < inv.method_5439(); ++i) {
                class_1799 stack = inv.method_5438(i);
                if (stack.method_7960()) continue;
                if (stack.method_7909().method_19263()) {
                    return true;
                }
                String id = ChallengeManager.itemId(stack.method_7909());
                if (id.contains("bread") || id.contains("apple") || id.contains("beef") || id.contains("carrot") || id.contains("potato") || id.contains("porkchop")) {
                    return true;
                }
            }
            return false;
        }

        public static boolean hasItem(class_3222 player, String targetItemId) {
            if (player == null || targetItemId == null || targetItemId.isEmpty()) return false;
            class_1661 inv = player.method_31548();
            for (int i = 0; i < inv.method_5439(); ++i) {
                class_1799 stack = inv.method_5438(i);
                if (stack.method_7960()) continue;
                String id = ChallengeManager.itemId(stack.method_7909());
                if (id.equals(targetItemId)) {
                    return true;
                }
            }
            return false;
        }

        public static void startPhraseInvocation(class_3222 initiator, String action) {
            if (initiator == null) return;
            String act = action != null ? action.toLowerCase().trim() : "follow";
            if (act.startsWith("coop.")) act = act.substring(5);

            MinecraftServer server = ChallengeManager.server(initiator);
            long now = server != null ? server.method_3780() : 0L;
            List<class_3222> teammates = getNearbyTeammates(initiator, 48.0);

            if (teammates.isEmpty() && !"lava".equals(act) && !"parkour".equals(act)) {
                initiator.method_43496((class_2561)class_2561.method_43470(
                    "\u00a77[COOP] Nenhum aliado pr\u00f3ximo encontrado em 48 blocos. Pratique individualmente!"
                ));
            }

            String taskId = "task_" + (taskCounter++);

            if (act.startsWith("item:") || act.startsWith("request_item:")) {
                String reqItemId = act.substring(act.indexOf(':') + 1).trim();
                class_1792 reqItem = ChallengeManager.item(reqItemId);
                String reqPtName = reqItemId;
                String reqRuName = Alphabet.getRuWord(reqItemId);
                if (reqItem != null) {
                    class_1799 st = new class_1799((class_1935)reqItem);
                    reqPtName = st.method_7964().getString();
                }
                if (reqRuName == null || reqRuName.isEmpty() || reqRuName.equals(reqItemId)) {
                    reqRuName = reqPtName;
                }
                sendBilingualCue(
                    initiator,
                    teammates,
                    "item:" + reqItemId,
                    "\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e: " + reqRuName + "!",
                    "[Mnye nuzhno: " + reqRuName + "!]",
                    "Preciso de " + reqPtName + "!",
                    "Aproxime-se do aliado com o item solicitado!",
                    0, 0, 0,
                    20
                );
                ActiveTask task = new ActiveTask(taskId, "REQUEST_ITEM", initiator.method_5667(), 0, 0, 0, now, now + 400L, 1);
                task.data.put("reqItemId", reqItemId);
                task.data.put("reqPtName", reqPtName);
                task.data.put("reqRuName", reqRuName);
                for (class_3222 t : teammates) task.targetUuids.add(t.method_5667());
                ACTIVE_TASKS.put(taskId, task);
                return;
            }

            switch (act) {
                case "follow": {
                    initiator.method_6092(new class_1293(class_1294.field_5914, 200, 0));
                    sendBilingualCue(
                        initiator,
                        teammates,
                        "follow",
                        "\u0421\u043b\u0435\u0434\u0443\u0439 \u0437\u0430 \u043c\u043d\u043e\u0439!",
                        "[Sleduy za mnoy!]",
                        "Siga-me!",
                        "Permane\u00e7a pr\u00f3ximo do aliado por 5 segundos!",
                        0, 0, 0,
                        10
                    );
                    ActiveTask task = new ActiveTask(taskId, "FOLLOW", initiator.method_5667(), 0, 0, 0, now, now + 200L, 1);
                    for (class_3222 t : teammates) task.targetUuids.add(t.method_5667());
                    ACTIVE_TASKS.put(taskId, task);
                    break;
                }
                case "waypoint": {
                    class_243 look = initiator.method_5720();
                    double tx = initiator.method_23317() + look.field_1352 * 16.0;
                    double ty = initiator.method_23318() + look.field_1351 * 16.0;
                    double tz = initiator.method_23321() + look.field_1350 * 16.0;
                    sendBilingualCue(
                        initiator,
                        teammates,
                        "waypoint",
                        "\u0421\u043c\u043e\u0442\u0440\u0438 \u0442\u0443\u0434\u0430!",
                        "[Smotri tuda!]",
                        "Olhe para l\u00e1!",
                        "Alcance o ponto marcado pelas part\u00edculas!",
                        tx, ty, tz,
                        15
                    );
                    ActiveTask task = new ActiveTask(taskId, "WAYPOINT", initiator.method_5667(), tx, ty, tz, now, now + 300L, 1);
                    for (class_3222 t : teammates) task.targetUuids.add(t.method_5667());
                    ACTIVE_TASKS.put(taskId, task);
                    break;
                }
                case "request_wood":
                case "wood": {
                    sendBilingualCue(
                        initiator,
                        teammates,
                        "request_wood",
                        "\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e \u0434\u0435\u0440\u0435\u0432\u043e!",
                        "[Mnye nuzhno dyerevo!]",
                        "Preciso de madeira!",
                        "Aproxime-se do aliado com madeira!",
                        0, 0, 0,
                        20
                    );
                    ActiveTask task = new ActiveTask(taskId, "REQUEST_WOOD", initiator.method_5667(), 0, 0, 0, now, now + 400L, 1);
                    for (class_3222 t : teammates) task.targetUuids.add(t.method_5667());
                    ACTIVE_TASKS.put(taskId, task);
                    break;
                }
                case "request_food":
                case "food": {
                    sendBilingualCue(
                        initiator,
                        teammates,
                        "request_food",
                        "\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u0430 \u0435\u0434\u0430!",
                        "[Mnye nuzhna yeda!]",
                        "Preciso de comida!",
                        "Aproxime-se do aliado com comida!",
                        0, 0, 0,
                        20
                    );
                    ActiveTask task = new ActiveTask(taskId, "REQUEST_FOOD", initiator.method_5667(), 0, 0, 0, now, now + 400L, 1);
                    for (class_3222 t : teammates) task.targetUuids.add(t.method_5667());
                    ACTIVE_TASKS.put(taskId, task);
                    break;
                }
                case "help": {
                    sendBilingualCue(
                        initiator,
                        teammates,
                        "help",
                        "\u041f\u043e\u043c\u043e\u0433\u0438 \u043c\u043d\u0435!",
                        "[Pomogi mnye!]",
                        "Ajude-me!",
                        "Aproxime-se rapidamente para socorrer!",
                        0, 0, 0,
                        15
                    );
                    ActiveTask task = new ActiveTask(taskId, "HELP", initiator.method_5667(), 0, 0, 0, now, now + 300L, 1);
                    for (class_3222 t : teammates) task.targetUuids.add(t.method_5667());
                    ACTIVE_TASKS.put(taskId, task);
                    break;
                }
                case "lava": {
                    startLavaChallenge(initiator, server);
                    break;
                }
                case "parkour": {
                    startParkourChallenge(initiator, server);
                    break;
                }
                case "rally": {
                    startRallyChallenge(initiator, server);
                    break;
                }
                default: {
                    initiator.method_43496((class_2561)class_2561.method_43470(
                        "\u00a7c[COOP] A\u00e7\u00e3o desconhecida: " + act + ". Op\u00e7\u00f5es: follow, waypoint, wood, food, help, lava, parkour, rally"
                    ));
                    break;
                }
            }
        }

        public static void startLavaChallenge(class_3222 initiator, MinecraftServer server) {
            if (server == null) return;
            long now = server.method_3780();
            String taskId = "lava_" + (taskCounter++);
            ActiveTask task = new ActiveTask(taskId, "LAVA", initiator != null ? initiator.method_5667() : UUID.randomUUID(), 0, 0, 0, now, now + 60L + 240L, 1);

            for (class_3222 p : server.method_3760().method_14571()) {
                task.targetUuids.add(p.method_5667());
                task.data.put("y_" + p.method_5667().toString(), p.method_23318());
                p.method_5783(class_3417.field_14605, 1.0f, 1.0f);
            }

            ACTIVE_TASKS.put(taskId, task);

            String senderName = initiator != null ? initiator.method_5477().getString() : "Sistema";
            broadcastServer(server, "\u00a7c[\u26a0 MINIDESAFIO: " + senderName + "] \u00a7e\u041f\u043e\u043b - \u044d\u0442\u043e \u043b\u0430\u0432\u0430! \u00a7c(O CH\u00c3O \u00c9 LAVA EM 3 SEGUNDOS!)");
            broadcastServer(server, "\u00a7cSuba em um bloco ou pule para n\u00e3o queimar!");
        }

        public static void startParkourChallenge(class_3222 initiator, MinecraftServer server) {
            if (server == null) return;
            long now = server.method_3780();
            String taskId = "parkour_" + (taskCounter++);
            ActiveTask task = new ActiveTask(taskId, "PARKOUR", initiator != null ? initiator.method_5667() : UUID.randomUUID(), 0, 0, 0, now, now + 300L, 1);

            for (class_3222 p : server.method_3760().method_14571()) {
                task.targetUuids.add(p.method_5667());
                p.method_5783(class_3417.field_14622.comp_349(), 1.0f, 1.2f);
            }

            ACTIVE_TASKS.put(taskId, task);
            broadcastServer(server, "\u00a7b[\u26a0 MOVIMENTO] \u00a7e\u041f\u0440\u044b\u0433\u0430\u0439 \u0432\u044b\u0448\u0435! \u00a7f(Pule mais alto!) - Corra e salte por 15 segundos!");
        }

        public static void startRallyChallenge(class_3222 initiator, MinecraftServer server) {
            if (server == null || initiator == null) return;
            long now = server.method_3780();
            String taskId = "rally_" + (taskCounter++);
            ActiveTask task = new ActiveTask(taskId, "RALLY", initiator.method_5667(), 0, 0, 0, now, now + 200L, 1);

            for (class_3222 p : server.method_3760().method_14571()) {
                task.targetUuids.add(p.method_5667());
                p.method_5783(class_3417.field_14622.comp_349(), 1.0f, 1.4f);
            }

            ACTIVE_TASKS.put(taskId, task);
            broadcastServer(server, "\u00a76[\u26a0 REUNI\u00c3O] \u00a7e" + initiator.method_5477().getString() + " convoca: \u0414\u0435\u0440\u0436\u0438\u043c\u0441\u044f \u0432\u043c\u0435\u0441\u0442\u0435! (Fiquem juntos!) Re\u00fanam-se em 10s!");
        }

        public static void tryTriggerRandomDialogue(MinecraftServer server, long now) {
            if (server == null) return;
            List<class_3222> online = new ArrayList<class_3222>(server.method_3760().method_14571());
            if (online.size() < 2) return;

            for (int i = 0; i < online.size(); ++i) {
                class_3222 p1 = online.get(i);
                State s1 = ChallengeManager.state(p1);
                if (s1.active != null) continue;

                for (int j = i + 1; j < online.size(); ++j) {
                    class_3222 p2 = online.get(j);
                    State s2 = ChallengeManager.state(p2);
                    if (s2.active != null) continue;

                    if (p1.method_37908().equals(p2.method_37908()) && distance(p1, p2) <= 24.0) {
                        startDialogueBetween(p1, p2, RANDOM.nextInt(DIALOGUES.length), now);
                        return;
                    }
                }
            }
        }

        public static void startDialogueBetween(class_3222 playerA, class_3222 playerB, int dialogueIdx, long now) {
            if (playerA == null || playerB == null) return;
            int idx = Math.abs(dialogueIdx) % DIALOGUES.length;
            String[] d = DIALOGUES[idx];
            String theme = d[0];
            String ru1 = d[1];
            String phon1 = d[2];
            String pt1 = d[3];

            String taskId = "dialogue_" + (taskCounter++);
            ActiveTask task = new ActiveTask(taskId, "DIALOGUE", playerA.method_5667(), 0, 0, 0, now, now + 400L, 1);
            task.targetUuids.add(playerB.method_5667());
            task.data.put("dialogueIndex", idx);
            task.data.put("partnerUuid", playerB.method_5667());
            ACTIVE_TASKS.put(taskId, task);

            sendBilingualCue(
                playerA,
                List.of(playerA),
                "dialogue_1",
                ru1,
                phon1,
                pt1,
                "Diga a frase para iniciar o di\u00e1logo!",
                0, 0, 0,
                20
            );

            playerB.method_43496((class_2561)class_2561.method_43470(
                "\u00a76[Di\u00e1logo Cooperativo: " + theme + "] \u00a77Aguardando \u00a7e" + playerA.method_5477().getString() +
                " \u00a77falar: \u00a7e\"" + ru1 + "\" \u00a77(\"" + pt1 + "\")"
            ));
            playerB.method_7353((class_2561)class_2561.method_43470(
                "\u00a76[Di\u00e1logo] \u00a77Aguardando " + playerA.method_5477().getString() + ": \"" + pt1 + "\""
            ), true);
            playerB.method_5783(class_3417.field_14622.comp_349(), 1.0f, 1.4f);

            ChallengeManager.start(playerA, ChallengeManager.state(playerA), now, "coop_dialogue", "dialogue." + idx + ".1", "", 20);
        }

        public static void handleChallengeResult(class_3222 player, Packets.Challenge c, int score, String heard, boolean passed) {
            if (player == null || c == null) return;
            MinecraftServer server = ChallengeManager.server(player);
            long now = server != null ? server.method_3780() : 0L;

            for (Iterator<Map.Entry<String, ActiveTask>> it = ACTIVE_TASKS.entrySet().iterator(); it.hasNext(); ) {
                ActiveTask task = it.next().getValue();
                if (!"DIALOGUE".equals(task.type)) continue;
                int idx = (int)task.data.getOrDefault("dialogueIndex", 0);
                String[] d = DIALOGUES[idx];

                if (task.phase == 1 && player.method_5667().equals(task.initiatorUuid)) {
                    if (passed || score >= 60) {
                        String ru1 = d[1];
                        String phon1 = d[2];
                        String pt1 = d[3];
                        broadcastServer(server, "\u00a76[Di\u00e1logo] \u00a7e" + player.method_5477().getString() +
                            "\u00a7f: \u00a7e" + ru1 + " \u00a77" + phon1 + " \u00a7f- \u00a7b\"" + pt1 + "\"");

                        task.phase = 2;
                        task.deadlineTick = now + 400L;
                        UUID partnerUuid = (UUID)task.data.get("partnerUuid");
                        class_3222 playerB = partnerUuid != null ? getPlayerByUuid(server, partnerUuid) : null;
                        if (playerB != null) {
                            String ru2 = d[4];
                            String phon2 = d[5];
                            String pt2 = d[6];

                            sendBilingualCue(
                                playerB,
                                List.of(playerB),
                                "dialogue_2",
                                ru2,
                                phon2,
                                pt2,
                                "Responda ao di\u00e1logo cooperativo!",
                                0, 0, 0,
                                20
                            );

                            player.method_7353((class_2561)class_2561.method_43470(
                                "\u00a76[Di\u00e1logo] \u00a77Aguardando resposta de " + playerB.method_5477().getString() + ": \"" + pt2 + "\""
                            ), true);

                            ChallengeManager.start(playerB, ChallengeManager.state(playerB), now, "coop_dialogue", "dialogue." + idx + ".2", "", 20);
                        }
                    } else {
                        it.remove();
                        player.method_43496((class_2561)class_2561.method_43470(
                            "\u00a7c[Di\u00e1logo] Pron\u00fancia insuficiente (" + score + "%). O di\u00e1logo foi cancelado."
                        ));
                    }
                    return;
                } else if (task.phase == 2 && task.targetUuids.contains(player.method_5667())) {
                    it.remove();
                    if (passed || score >= 60) {
                        String ru2 = d[4];
                        String phon2 = d[5];
                        String pt2 = d[6];
                        broadcastServer(server, "\u00a76[Di\u00e1logo] \u00a7e" + player.method_5477().getString() +
                            "\u00a7f: \u00a7e" + ru2 + " \u00a77" + phon2 + " \u00a7f- \u00a7b\"" + pt2 + "\"");

                        class_3222 playerA = getPlayerByUuid(server, task.initiatorUuid);
                        if (playerA != null) {
                            awardCoopPoints(playerA, 30, "Completar di\u00e1logo cooperativo (" + d[0] + ")");
                            playerA.method_7255(15);
                            ChallengeManager.state(playerA).progress.addHeat(d[7], 2);
                        }
                        awardCoopPoints(player, 30, "Completar di\u00e1logo cooperativo (" + d[0] + ")");
                        player.method_7255(15);
                        ChallengeManager.state(player).progress.addHeat(d[7], 2);

                        broadcastServer(server, "\u00a7a[\u2605 DI\u00c1LOGO CONCLU\u00cdDO] Conversa\u00e7\u00e3o bilateral conclu\u00edda com sucesso! +30 Pontos Cooperativos para ambos!");
                    } else {
                        player.method_43496((class_2561)class_2561.method_43470(
                            "\u00a7c[Di\u00e1logo] Pron\u00fancia insuficiente na resposta (" + score + "%). Di\u00e1logo n\u00e3o completado."
                        ));
                    }
                    return;
                }
            }
        }

        public static void tick(MinecraftServer server, long now) {
            if (server == null) return;

            Iterator<Map.Entry<String, ActiveTask>> it = ACTIVE_TASKS.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, ActiveTask> entry = it.next();
                ActiveTask task = entry.getValue();
                class_3222 initiator = getPlayerByUuid(server, task.initiatorUuid);

                if (now > task.deadlineTick) {
                    it.remove();
                    handleTaskTimeout(server, task, initiator);
                    continue;
                }

                if (evaluateTaskTick(server, task, initiator, now)) {
                    it.remove();
                    handleTaskSuccess(server, task, initiator);
                }
            }

            if (now - lastDialogueCheckTick >= 3600L) {
                lastDialogueCheckTick = now;
                tryTriggerRandomDialogue(server, now);
            }
        }

        private static boolean evaluateTaskTick(MinecraftServer server, ActiveTask task, class_3222 initiator, long now) {
            switch (task.type) {
                case "FOLLOW": {
                    if (initiator == null) return false;
                    for (UUID uid : task.targetUuids) {
                        class_3222 target = getPlayerByUuid(server, uid);
                        if (target != null && distance(initiator, target) <= 6.0) {
                            int cnt = (int)task.data.getOrDefault("nearCount", 0) + 1;
                            task.data.put("nearCount", cnt);
                            task.data.put("lastNearPlayer", uid);
                            if (cnt >= 3) return true;
                        }
                    }
                    return false;
                }
                case "WAYPOINT": {
                    if (initiator != null) {
                        class_3218 world = (class_3218)initiator.method_37908();
                        world.method_14199(class_2398.field_11234, task.targetX, task.targetY, task.targetZ, 12, 0.2, 0.8, 0.2, 0.05);
                    }
                    for (UUID uid : task.targetUuids) {
                        class_3222 target = getPlayerByUuid(server, uid);
                        if (target != null && distanceToPoint(target, task.targetX, task.targetY, task.targetZ) <= 5.0) {
                            task.data.put("winner", uid);
                            return true;
                        }
                    }
                    return false;
                }
                case "REQUEST_ITEM": {
                    if (initiator == null) return false;
                    String reqItemId = (String)task.data.get("reqItemId");
                    for (UUID uid : task.targetUuids) {
                        class_3222 target = getPlayerByUuid(server, uid);
                        if (target != null && distance(initiator, target) <= 6.0 && hasItem(target, reqItemId)) {
                            task.data.put("winner", uid);
                            return true;
                        }
                    }
                    return false;
                }
                case "REQUEST_WOOD": {
                    if (initiator == null) return false;
                    for (UUID uid : task.targetUuids) {
                        class_3222 target = getPlayerByUuid(server, uid);
                        if (target != null && distance(initiator, target) <= 6.0 && hasWood(target)) {
                            task.data.put("winner", uid);
                            return true;
                        }
                    }
                    return false;
                }
                case "REQUEST_FOOD": {
                    if (initiator == null) return false;
                    for (UUID uid : task.targetUuids) {
                        class_3222 target = getPlayerByUuid(server, uid);
                        if (target != null && distance(initiator, target) <= 6.0 && hasFood(target)) {
                            task.data.put("winner", uid);
                            return true;
                        }
                    }
                    return false;
                }
                case "HELP": {
                    if (initiator == null) return false;
                    for (UUID uid : task.targetUuids) {
                        class_3222 target = getPlayerByUuid(server, uid);
                        if (target != null && distance(initiator, target) <= 5.0) {
                            task.data.put("winner", uid);
                            return true;
                        }
                    }
                    return false;
                }
                case "LAVA": {
                    if (task.phase == 1) {
                        int sLeft = (int)((task.startTick + 60L - now) / 20L);
                        if (now % 20L == 0L) {
                            String warn = "\u00a7c\u26a0 ПОЛ \u2014 \u042d\u0422\u041e \u041b\u0410\u0412\u0410 \u0427\u0415\u0420\u0415\u0417 " + Math.max(1, sLeft) + "s! \u26a0";
                            for (UUID uid : task.targetUuids) {
                                class_3222 p = getPlayerByUuid(server, uid);
                                if (p != null) p.method_7353((class_2561)class_2561.method_43470(warn), true);
                            }
                        }
                        if (now >= task.startTick + 60L) {
                            task.phase = 2;
                            broadcastServer(server, "\u00a7c\u26a0 O CH\u00c3O \u00c9 LAVA AGORA! SUBA EM UM BLOCO OU SOFRA QUEIMADURAS!");
                            for (UUID uid : task.targetUuids) {
                                class_3222 p = getPlayerByUuid(server, uid);
                                if (p != null) p.method_5783(class_3417.field_14605, 1.0f, 1.2f);
                            }
                        }
                        return false;
                    }
                    int remSec = Math.max(1, (int)((task.deadlineTick - now) / 20L));
                    for (UUID uid : task.targetUuids) {
                        class_3222 p = getPlayerByUuid(server, uid);
                        if (p == null) continue;
                        if (p.method_7325()) continue;

                        Double baseY = (Double)task.data.get("y_" + uid.toString());
                        double initY = baseY != null ? baseY : p.method_23318();

                        class_2338 pos = p.method_24515();
                        class_3218 world = (class_3218)p.method_37908();
                        class_2680 blockDown = world.method_8320(pos.method_10074());
                        boolean solidGround = !blockDown.method_26215();

                        boolean touchingLava = p.method_24828() && solidGround && (p.method_23318() <= initY + 0.5)
                            && !p.method_5799() && p.method_5854() == null && !p.method_6101();

                        if (touchingLava) {
                            p.method_20803(30);
                            world.method_14199(class_2398.field_11244, p.method_23317(), p.method_23318(), p.method_23321(), 8, 0.2, 0.1, 0.2, 0.05);
                            p.method_7353((class_2561)class_2561.method_43470("\u00a7c\ud83d\udd25 VOC\u00ca EST\u00c1 NA LAVA! PULE OU SUBA EM UM BLOCO! (" + remSec + "s) \ud83d\udd25"), true);
                            int burns = (int)task.data.getOrDefault("burn_" + uid.toString(), 0) + 1;
                            task.data.put("burn_" + uid.toString(), burns);
                        } else {
                            p.method_7353((class_2561)class_2561.method_43470("\u00a7a\u2714 SEGURO! (" + remSec + "s restantes)"), true);
                        }
                    }
                    if (now >= task.deadlineTick) {
                        return true;
                    }
                    return false;
                }
                case "PARKOUR": {
                    if (now >= task.deadlineTick) return true;
                    return false;
                }
                case "RALLY": {
                    if (initiator == null) return false;
                    boolean allNear = true;
                    for (UUID uid : task.targetUuids) {
                        if (uid.equals(task.initiatorUuid)) continue;
                        class_3222 target = getPlayerByUuid(server, uid);
                        if (target == null || distance(initiator, target) > 5.0) {
                            allNear = false;
                            break;
                        }
                    }
                    if (allNear && task.targetUuids.size() > 1) return true;
                    return false;
                }
                default:
                    return false;
            }
        }

        private static void handleTaskSuccess(MinecraftServer server, ActiveTask task, class_3222 initiator) {
            switch (task.type) {
                case "FOLLOW": {
                    UUID winUid = (UUID)task.data.get("lastNearPlayer");
                    class_3222 winner = winUid != null ? getPlayerByUuid(server, winUid) : null;
                    if (initiator != null) awardCoopPoints(initiator, 15, "Manter-se unido com aliado (\u0421\u043b\u0435\u0434\u0443\u0439 \u0437\u0430 \u043c\u043d\u043e\u0439!)");
                    if (winner != null && !winner.method_5667().equals(task.initiatorUuid)) {
                        awardCoopPoints(winner, 15, "Seguir o companheiro (\u0421\u043b\u0435\u0434\u0443\u0439 \u0437\u0430 \u043c\u043d\u043e\u0439!)");
                    }
                    broadcastToTeam(server, task, "\u00a7a[\u2714 SUCESSO] Alian\u00e7a mantida com sucesso! +15 Pontos Cooperativos para ambos.");
                    break;
                }
                case "WAYPOINT": {
                    UUID winUid = (UUID)task.data.get("winner");
                    class_3222 winner = winUid != null ? getPlayerByUuid(server, winUid) : null;
                    if (initiator != null) awardCoopPoints(initiator, 15, "Marcar ponto de encontro (\u0421\u043c\u043e\u0442\u0440\u0438 \u0442\u0443\u0434\u0430!)");
                    if (winner != null && !winner.method_5667().equals(task.initiatorUuid)) {
                        awardCoopPoints(winner, 15, "Alcan\u00e7ar ponto marcado (\u0421\u043c\u043e\u0442\u0440\u0438 \u0442\u0443\u0434\u0430!)");
                    }
                    broadcastToTeam(server, task, "\u00a7a[\u2714 SUCESSO] Ponto de encontro alcan\u00e7ado! +15 Pontos Cooperativos.");
                    break;
                }
                case "REQUEST_ITEM": {
                    UUID winUid = (UUID)task.data.get("winner");
                    class_3222 winner = winUid != null ? getPlayerByUuid(server, winUid) : null;
                    String ptName = (String)task.data.getOrDefault("reqPtName", "item");
                    String ruName = (String)task.data.getOrDefault("reqRuName", ptName);
                    if (initiator != null) awardCoopPoints(initiator, 20, "Solicitar " + ptName + " (\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e: " + ruName + "!)");
                    if (winner != null && !winner.method_5667().equals(task.initiatorUuid)) {
                        awardCoopPoints(winner, 20, "Fornecer " + ptName + " para aliado (\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e: " + ruName + "!)");
                    }
                    broadcastToTeam(server, task, "\u00a7a[\u2714 SUCESSO] " + ptName + " compartilhado! +20 Pontos Cooperativos para ambos.");
                    break;
                }
                case "REQUEST_WOOD": {
                    UUID winUid = (UUID)task.data.get("winner");
                    class_3222 winner = winUid != null ? getPlayerByUuid(server, winUid) : null;
                    if (initiator != null) awardCoopPoints(initiator, 20, "Solicitar madeira (\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e \u0434\u0435\u0440\u0435\u0432\u043e!)");
                    if (winner != null && !winner.method_5667().equals(task.initiatorUuid)) {
                        awardCoopPoints(winner, 20, "Fornecer madeira para aliado (\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e \u0434\u0435\u0440\u0435\u0432\u043e!)");
                    }
                    broadcastToTeam(server, task, "\u00a7a[\u2714 SUCESSO] Madeira compartilhada! +20 Pontos Cooperativos para ambos.");
                    break;
                }
                case "REQUEST_FOOD": {
                    UUID winUid = (UUID)task.data.get("winner");
                    class_3222 winner = winUid != null ? getPlayerByUuid(server, winUid) : null;
                    if (initiator != null) awardCoopPoints(initiator, 20, "Solicitar comida (\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u0430 \u0435\u0434\u0430!)");
                    if (winner != null && !winner.method_5667().equals(task.initiatorUuid)) {
                        awardCoopPoints(winner, 20, "Compartilhar comida com aliado (\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u0430 \u0435\u0434\u0430!)");
                    }
                    broadcastToTeam(server, task, "\u00a7a[\u2714 SUCESSO] Alimento compartilhado! +20 Pontos Cooperativos para ambos.");
                    break;
                }
                case "HELP": {
                    UUID winUid = (UUID)task.data.get("winner");
                    class_3222 winner = winUid != null ? getPlayerByUuid(server, winUid) : null;
                    if (initiator != null) {
                        awardCoopPoints(initiator, 20, "Pedir ajuda ao aliado (\u041f\u043e\u043c\u043e\u0433\u0438 \u043c\u043d\u0435!)");
                        initiator.method_6092(new class_1293(class_1294.field_5908, 140, 0));
                    }
                    if (winner != null && !winner.method_5667().equals(task.initiatorUuid)) {
                        awardCoopPoints(winner, 20, "Prestar socorro ao aliado (\u041f\u043e\u043c\u043e\u0433\u0438 \u043c\u043d\u0435!)");
                        winner.method_6092(new class_1293(class_1294.field_5908, 140, 0));
                    }
                    broadcastToTeam(server, task, "\u00a7a[\u2714 SUCESSO] Socorro prestado! Regenera\u00e7\u00e3o e +20 Pontos Cooperativos concedidos.");
                    break;
                }
                case "LAVA": {
                    for (UUID uid : task.targetUuids) {
                        class_3222 p = getPlayerByUuid(server, uid);
                        if (p != null) {
                            int burns = (int)task.data.getOrDefault("burn_" + uid.toString(), 0);
                            if (burns < 10) {
                                awardCoopPoints(p, 25, "Sobreviver ao Ch\u00e3o \u00e9 Lava (\u041f\u043e\u043b - \u044d\u0442\u043e \u043b\u0430\u0432\u0430!)");
                                p.method_6092(new class_1293(class_1294.field_5904, 300, 1));
                                p.method_43496((class_2561)class_2561.method_43470("\u00a7a[\u2605 Vit\u00f3ria] Voc\u00ea sobreviveu ao Ch\u00e3o \u00e9 Lava! +25 Pontos Cooperativos e Velocidade II!"));
                            } else {
                                p.method_43496((class_2561)class_2561.method_43470("\u00a7c[\ud83d\udd25 Queimado] Voc\u00ea sofreu queimaduras excessivas na lava. Tente novamente no pr\u00f3ximo!"));
                            }
                        }
                    }
                    break;
                }
                case "PARKOUR": {
                    for (UUID uid : task.targetUuids) {
                        class_3222 p = getPlayerByUuid(server, uid);
                        if (p != null) {
                            awardCoopPoints(p, 20, "Completar desafio de movimento (\u041f\u0440\u044b\u0433\u0430\u0439 \u0432\u044b\u0448\u0435!)");
                            p.method_6092(new class_1293(class_1294.field_5915, 400, 1));
                        }
                    }
                    broadcastServer(server, "\u00a7a[\u2714 PARKOUR CONCLU\u00cdDO] Todos os participantes receberam +20 Pontos Cooperativos e Super Salto!");
                    break;
                }
                case "RALLY": {
                    for (UUID uid : task.targetUuids) {
                        class_3222 p = getPlayerByUuid(server, uid);
                        if (p != null) {
                            awardCoopPoints(p, 20, "Reuni\u00e3o de equipe conclu\u00edda (\u0414\u0435\u0440\u0436\u0438\u043c\u0441\u044f \u0432\u043c\u0435\u0441\u0442\u0435!)");
                            p.method_6092(new class_1293(class_1294.field_5908, 200, 0));
                        }
                    }
                    broadcastServer(server, "\u00a7a[\u2714 REUNI\u00c3O CONCLU\u00cdDA] A equipe est\u00e1 unida! +20 Pontos Cooperativos e Regenera\u00e7\u00e3o!");
                    break;
                }
                default:
                    break;
            }
        }

        private static void handleTaskTimeout(MinecraftServer server, ActiveTask task, class_3222 initiator) {
            if ("LAVA".equals(task.type)) {
                handleTaskSuccess(server, task, initiator);
                return;
            }
            if (initiator != null) {
                initiator.method_43496((class_2561)class_2561.method_43470(
                    "\u00a7c[COOP Tempo Esgotado] O tempo para completar o objetivo cooperativo expirou."
                ));
                initiator.method_5783(class_3417.field_14624.comp_349(), 0.8f, 0.8f);
            }
        }

        public static class_3222 getPlayerByUuid(MinecraftServer server, UUID uuid) {
            if (server == null || uuid == null) return null;
            for (class_3222 p : server.method_3760().method_14571()) {
                if (uuid.equals(p.method_5667())) return p;
            }
            return null;
        }

        public static void broadcastServer(MinecraftServer server, String message) {
            if (server == null || message == null) return;
            class_2561 txt = (class_2561)class_2561.method_43470(message);
            for (class_3222 p : server.method_3760().method_14571()) {
                p.method_43496(txt);
            }
        }

        public static void broadcastToTeam(MinecraftServer server, ActiveTask task, String message) {
            if (server == null || task == null) return;
            class_2561 txt = (class_2561)class_2561.method_43470(message);
            class_3222 init = getPlayerByUuid(server, task.initiatorUuid);
            if (init != null) init.method_43496(txt);
            for (UUID uid : task.targetUuids) {
                class_3222 t = getPlayerByUuid(server, uid);
                if (t != null && !t.method_5667().equals(task.initiatorUuid)) {
                    t.method_43496(txt);
                }
            }
        }

        public static void registerCommands(CommandDispatcher<class_2168> dispatcher) {
            dispatcher.register(
                class_2170.method_9247("sayit")
                    .then(class_2170.method_9247("coop")
                        .then(class_2170.method_9247("rank")
                            .executes(CoopManager::cmdRank))
                        .then(class_2170.method_9247("stats")
                            .executes(CoopManager::cmdStats))
                        .then(class_2170.method_9247("lava")
                            .executes(CoopManager::cmdLava))
                        .then(class_2170.method_9247("parkour")
                            .executes(CoopManager::cmdParkour))
                        .then(class_2170.method_9247("rally")
                            .executes(CoopManager::cmdRally))
                        .then(class_2170.method_9247("dialogue")
                            .executes(CoopManager::cmdDialogue))
                        .then(class_2170.method_9247("help")
                            .executes(CoopManager::cmdHelp))
                        .then(class_2170.method_9247("invoke")
                            .then(class_2170.method_9244("action", StringArgumentType.string())
                                .executes(ctx -> cmdInvoke(ctx, StringArgumentType.getString(ctx, "action")))))
                    )
                    .then(class_2170.method_9247("challenge")
                        .then(class_2170.method_9247("lava")
                            .executes(CoopManager::cmdLava))
                        .then(class_2170.method_9247("parkour")
                            .executes(CoopManager::cmdParkour))
                        .then(class_2170.method_9247("rally")
                            .executes(CoopManager::cmdRally))
                    )
            );
        }

        private static int cmdRank(CommandContext<class_2168> ctx) {
            class_2168 source = ctx.getSource();
            MinecraftServer server = source.method_9211();
            List<class_3222> players = new ArrayList<class_3222>(server.method_3760().method_14571());
            players.sort((p1, p2) -> {
                int s1 = ChallengeManager.state(p1).progress.coopScore;
                int s2 = ChallengeManager.state(p2).progress.coopScore;
                return Integer.compare(s2, s1);
            });
            StringBuilder sb = new StringBuilder("\u00a76=== [Leaderboard Cooperativo SayIt] ===\n");
            if (players.isEmpty()) {
                sb.append("\u00a77Nenhum jogador online no momento.");
            } else {
                int pos = 1;
                for (class_3222 p : players) {
                    Progress pr = ChallengeManager.state(p).progress;
                    sb.append("\u00a7e").append(pos++).append(". \u00a7f").append(p.method_5477().getString())
                      .append(" \u00a77- \u00a7a").append(pr.coopScore).append(" pts")
                      .append(" \u00a77- ").append(pr.coopRankTitle())
                      .append(" \u00a77(").append(pr.coopCompletedChallenges).append(" desafios)\n");
                }
            }
            source.method_9226(() -> (class_2561)class_2561.method_43470(sb.toString().trim()), false);
            return 1;
        }

        private static int cmdStats(CommandContext<class_2168> ctx) {
            class_2168 source = ctx.getSource();
            class_3222 player = null;
            try {
                player = source.method_9207();
            } catch (Exception ignored) {
            }
            if (player == null) {
                MinecraftServer server = source.method_9211();
                for (class_3222 p : server.method_3760().method_14571()) {
                    displayPlayerStats(source, p);
                }
                return 1;
            }
            displayPlayerStats(source, player);
            return 1;
        }

        private static void displayPlayerStats(class_2168 source, class_3222 p) {
            Progress pr = ChallengeManager.state(p).progress;
            StringBuilder sb = new StringBuilder();
            sb.append("\u00a76=== [Estat\u00edsticas Cooperativas: \u00a7e").append(p.method_5477().getString()).append("\u00a76] ===\n");
            sb.append("\u00a7ePontos Cooperativos: \u00a7f").append(pr.coopScore).append("\n");
            sb.append("\u00a7eDesafios Completados: \u00a7f").append(pr.coopCompletedChallenges).append("\n");
            sb.append("\u00a7ePatente Atual: ").append(pr.coopRankTitle()).append(" \u00a77(N\u00edvel ").append(pr.coopRankTier()).append("/5)\n");
            if (pr.coopRankTier() >= 5) {
                sb.append("\u00a7ePr\u00f3xima Patente: \u00a76\u041b\u0435\u0433\u0435\u043d\u0434\u0430 \u00a7a(N\u00edvel M\u00e1ximo alcan\u00e7ado!)");
            } else {
                sb.append("\u00a7ePr\u00f3xima Patente: ").append(pr.nextRankTitle())
                  .append(" \u00a77(").append(pr.pointsToNextRank()).append(" pts restantes)");
            }
            source.method_9226(() -> (class_2561)class_2561.method_43470(sb.toString()), false);
        }

        private static int cmdLava(CommandContext<class_2168> ctx) {
            class_2168 source = ctx.getSource();
            class_3222 player = null;
            try {
                player = source.method_9207();
            } catch (Exception ignored) {
            }
            startLavaChallenge(player, source.method_9211());
            return 1;
        }

        private static int cmdParkour(CommandContext<class_2168> ctx) {
            class_2168 source = ctx.getSource();
            class_3222 player = null;
            try {
                player = source.method_9207();
            } catch (Exception ignored) {
            }
            startParkourChallenge(player, source.method_9211());
            return 1;
        }

        private static int cmdRally(CommandContext<class_2168> ctx) {
            class_2168 source = ctx.getSource();
            class_3222 player = null;
            try {
                player = source.method_9207();
            } catch (Exception ignored) {
            }
            if (player == null) {
                source.method_9226(() -> (class_2561)class_2561.method_43470("\u00a7cComando deve ser executado por um jogador."), false);
                return 0;
            }
            startRallyChallenge(player, source.method_9211());
            return 1;
        }

        private static int cmdDialogue(CommandContext<class_2168> ctx) {
            class_2168 source = ctx.getSource();
            class_3222 player = null;
            try {
                player = source.method_9207();
            } catch (Exception ignored) {
            }
            MinecraftServer server = source.method_9211();
            long now = server.method_3780();
            if (player != null) {
                List<class_3222> mates = getNearbyTeammates(player, 32.0);
                if (mates.isEmpty()) {
                    source.method_9226(() -> (class_2561)class_2561.method_43470("\u00a7c[COOP] Nenhum aliado pr\u00f3ximo para iniciar um di\u00e1logo."), false);
                    return 0;
                }
                startDialogueBetween(player, mates.get(0), RANDOM.nextInt(DIALOGUES.length), now);
                return 1;
            }
            tryTriggerRandomDialogue(server, now);
            return 1;
        }

        private static int cmdInvoke(CommandContext<class_2168> ctx, String action) {
            class_2168 source = ctx.getSource();
            class_3222 player = null;
            try {
                player = source.method_9207();
            } catch (Exception ignored) {
            }
            if (player == null) {
                source.method_9226(() -> (class_2561)class_2561.method_43470("\u00a7cApenas jogadores podem invocar frases."), false);
                return 0;
            }
            startPhraseInvocation(player, action);
            return 1;
        }

        private static int cmdHelp(CommandContext<class_2168> ctx) {
            class_2168 source = ctx.getSource();
            StringBuilder sb = new StringBuilder();
            sb.append("\u00a76=== [Comandos Cooperativos SayIt] ===\n");
            sb.append("\u00a7e/sayit coop rank \u00a77- Exibe o placar de l\u00edderes cooperativo\n");
            sb.append("\u00a7e/sayit coop stats \u00a77- Exibe suas estat\u00edsticas e patente\n");
            sb.append("\u00a7e/sayit coop lava \u00a77- Inicia o desafio 'O Ch\u00e3o \u00e9 Lava'\n");
            sb.append("\u00a7e/sayit coop parkour \u00a77- Inicia o minidesafio de movimento\n");
            sb.append("\u00a7e/sayit coop rally \u00a77- Inicia o minidesafio de reuni\u00e3o de equipe\n");
            sb.append("\u00a7e/sayit coop dialogue \u00a77- Inicia um di\u00e1logo cooperativo em 2 turnos\n");
            sb.append("\u00a7e/sayit coop invoke <a\u00e7\u00e3o> \u00a77- Invoca frase (follow, waypoint, wood, food, help)");
            source.method_9226(() -> (class_2561)class_2561.method_43470(sb.toString()), false);
            return 1;
        }
    }
}
