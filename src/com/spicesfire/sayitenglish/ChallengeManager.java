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
                pr.errors().remove(id);
            }
            pr.reps().put(id, 0);
            player.method_43496((class_2561)class_2561.method_43470("\u00a76\u26a0 [Curva do Esquecimento] Faz " + elapsedHours + "h sem praticar! Fixa\u00e7\u00e3o regrediu de " + lvl + "/5 para " + regressed + "/5 estrelas."));
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
                case 1: return 20L;  // 1s
                case 2: return 40L;  // 2s
                case 3: return 60L;  // 3s
                case 4: return 80L;  // 4s
                default: return 100L; // 5s
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
        int prevTotalStars = pr.totalStars();
        long now = ChallengeManager.server(player).method_3780();

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
                                    "\u00a7a\u2605 [Estrela 1/5 Desbloqueada!] Item Raro dominado na maioria das tentativas (" + successes + "/7 acertos)! Pr\u00f3ximos testes em ~" + delayMin + " min."
                                ));
                            } else if (newStars >= 5) {
                                player.method_43496((class_2561)class_2561.method_43470(
                                    "\u00a76\u2605\u2605\u2605\u2605\u2605 [DOM\u00cdNIO MESTRE 5/5] Item Raro dominado com perfei\u00e7\u00e3o na maioria das tentativas (" + successes + "/7 acertos)! Revis\u00e3o peri\u00f3dica em ~" + delayMin + " min."
                                ));
                            } else {
                                player.method_43496((class_2561)class_2561.method_43470(
                                    "\u00a7a\u2605 [Estrela " + newStars + "/5 Conquistada!] Item Raro superou o teste de reten\u00e7\u00e3o na maioria (" + successes + "/7 acertos)! Pr\u00f3xima etapa em ~" + delayMin + " min."
                                ));
                            }
                        } else {
                            long cdTicks = ChallengeManager.repCooldownTicks(currentStars, attemptsDone, true);
                            s.cooldown.put(c.subject(), now + cdTicks);
                            int nextSec = Math.max(1, (int)(cdTicks / 20L));
                            int remainingAttempts = 7 - attemptsDone;

                            if (successes >= 4) {
                                player.method_43496((class_2561)class_2561.method_43470(
                                    "\u00a7e\u2606 [Item Raro - Estrela " + targetStar + " - Tentativa " + attemptsDone + "/7] Correto (" + score + "%)! (" + successes + " acertos - maioria garantida!). Pr\u00f3ximo em " + nextSec + "s (" + remainingAttempts + " restante(s))."
                                ));
                            } else {
                                player.method_43496((class_2561)class_2561.method_43470(
                                    "\u00a7e\u2606 [Item Raro - Estrela " + targetStar + " - Tentativa " + attemptsDone + "/7] Correto (" + score + "%)! (" + successes + " acertos, " + errors + " erros). Pr\u00f3ximo em " + nextSec + "s (" + remainingAttempts + " restante(s))."
                                ));
                            }
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
                            ? "Progresso da palavra zerado!"
                            : "Regrediu para Estrela " + regressedStars + "/5.";

                        String actionMsg = "drop".equalsIgnoreCase(cfg.penalty)
                            ? "1 item foi dropado no ch\u00e3o!"
                            : "1 item foi exclu\u00eddo do invent\u00e1rio!";

                        player.method_43496((class_2561)class_2561.method_43470(
                            "\u00a7c\u2716 [Item Raro Perdido] Pron\u00fancia n\u00e3o reconhecida (" + score + "%). Voc\u00ea errou 4 tentativas (maioria de 7)! " + actionMsg + " " + regrMsg
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
                            "\u00a7a\u2605 [Estrela " + newStars + "/5 Conquistada!] 7 tentativas conclu\u00eddas com " + successes + " acertos e " + errors + " erros (maioria conquistada)! Pr\u00f3ximos testes em ~" + delayMin + " min."
                        ));
                    } else {
                        // Not lost yet, warning with strikes remaining
                        int remainingStrikes = 4 - errors;
                        int remainingAttempts = 7 - attemptsDone;
                        s.cooldown.put(c.subject(), now + 100L); // 5s grace interval

                        player.method_43496((class_2561)class_2561.method_43470(
                            "\u00a76\u26a0 [Item Raro - Estrela " + targetStar + " - Tentativa " + attemptsDone + "/7] Pron\u00fancia incorreta (" + score + "%). Erro " + errors + "/4 (se errar mais " + remainingStrikes + ", perde o item!). Pr\u00f3xima em 5s (" + remainingAttempts + " restante(s))."
                        ));
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
                                "\u00a7a\u2605 [Estrela 1/5 Desbloqueada!] Palavra descoberta com sucesso! Pr\u00f3ximos testes de fixa\u00e7\u00e3o em ~" + delayMin + " min."
                            ));
                        } else if (newStars >= 5) {
                            player.method_43496((class_2561)class_2561.method_43470(
                                "\u00a76\u2605\u2605\u2605\u2605\u2605 [DOM\u00cdNIO MESTRE 5/5] Voc\u00ea memorizou completamente esta palavra! Revis\u00e3o peri\u00f3dica em ~" + delayMin + " min."
                            ));
                        } else {
                            player.method_43496((class_2561)class_2561.method_43470(
                                "\u00a7a\u2605 [Estrela " + newStars + "/5 Conquistada!] N\u00edvel avan\u00e7ado! Descanse; pr\u00f3xima etapa em ~" + delayMin + " min."
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
                        int remaining = neededReps - nextReps;
                        int targetStar = currentStars + 1;

                        if (currentStars == 0) {
                            player.method_43496((class_2561)class_2561.method_43470(
                                "\u00a7e\u2606 [Descoberta " + nextReps + "/" + neededReps + "] Correto (" + score + "%)! Pr\u00f3ximo teste em " + nextSec + "s (" + remaining + " para Estrela 1 \u2605)."
                            ));
                        } else {
                            player.method_43496((class_2561)class_2561.method_43470(
                                "\u00a7e\u2605 [Estrela " + currentStars + "/5 - Teste " + nextReps + "/" + neededReps + "] Correto (" + score + "%)! Pr\u00f3ximo em " + nextSec + "s (" + remaining + " para Estrela " + targetStar + ")."
                            ));
                        }
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
                    ? "1 item foi dropado no ch\u00e3o!"
                    : "1 item foi exclu\u00eddo do invent\u00e1rio!";

                player.method_43496((class_2561)class_2561.method_43470(
                    "\u00a7c\u2716 [\u2605 " + regressedStars + "/5 - Repeti\u00e7\u00e3o " + regressedReps + "] Pron\u00fancia n\u00e3o reconhecida (" + score + "%). " + actionMsg
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
                if (nextStars >= 5) {
                    player.method_43496((class_2561)class_2561.method_43470("\u00a7a\u2714 [MESTRE 5/5] Dominado com Maestria na pr\u00e1tica!"));
                } else {
                    player.method_43496((class_2561)class_2561.method_43470("\u00a7e\u2605 [Estrela " + nextStars + "/5] Treino conclu\u00eddo com sucesso!"));
                }
                break;
            }
        }
        int newTotalStars = pr.totalStars();
        if (newTotalStars > prevTotalStars && "ru".equals(pr.language)) {
            int oldLvl = Alphabet.calculateLevel(prevTotalStars);
            int newLvl = Alphabet.calculateLevel(newTotalStars);
            if (newLvl > oldLvl && newLvl <= 32) {
                String letterInfo = Alphabet.LEVEL_NAME[newLvl];
                String mnem = Alphabet.LEVEL_MNEM[newLvl];
                player.method_43496((class_2561)class_2561.method_43470(
                    "\u00a76\u2605\u2605 [ALFABETO CIR\u00cdLICO] Parab\u00e9ns! N\u00edvel " + newLvl + "/32 Desbloqueado! \u2605\u2605\n" +
                    "\u00a7aNova letra ativa: \u00a7e" + letterInfo + " \u00a7a\u2022 \u00a77" + mnem
                ));

                try {
                    MinecraftServer srv = ChallengeManager.server(player);
                    if (srv != null) {
                        String pName = player.method_5477().getString();
                        if (newLvl >= 6 && oldLvl < 6) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 01_alfabeto_gemeas " + pName);
                        }
                        if (newLvl >= 12 && oldLvl < 12) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 02_alfabeto_falsos_amigos " + pName);
                        }
                        if (newLvl >= 19 && oldLvl < 19) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 03_alfabeto_construtores " + pName);
                        }
                        if (newLvl >= 26 && oldLvl < 26) {
                            srv.method_3734().method_44252(srv.method_3739(), "heracles complete 04_alfabeto_chiantes " + pName);
                        }
                        if (newLvl >= 32 && oldLvl < 32) {
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

        private State() {
        }
    }

    private record Event(String stem, String arg) {
    }
}
