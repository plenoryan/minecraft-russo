package com.spicesfire.sayitenglish;

import com.spicesfire.sayitenglish.platform.ModPacket;
import com.spicesfire.sayitenglish.platform.ModPacketType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public final class Packets {
    private Packets() {
    }

    static <T extends ModPacket> ModPacketType<T> type(String name, BiConsumer<class_2540, T> writer, Function<class_2540, T> reader) {
        return new ModPacketType<>(SayIt.id(name), reader);
    }

    public record Sync(String language, List<String> words, List<Integer> scores, int sentences, int lost) implements ModPacket {
        public static final ModPacketType<Sync> TYPE = Packets.type("sync", (b, p) -> p.write((class_2540)b), Sync::read);

        @Override
        public void write(class_2540 b) {
            b.method_10814(this.language != null ? this.language : "");
            int n = this.words != null ? this.words.size() : 0;
            b.method_10804(n);
            for (int i = 0; i < n; ++i) {
                b.method_10814(this.words.get(i));
                b.method_10804(this.scores.get(i));
            }
            b.method_10804(this.sentences);
            b.method_10804(this.lost);
        }

        static Sync read(class_2540 b) {
            String lang = b.method_19772();
            int n = b.method_10816();
            ArrayList<String> w = new ArrayList<>(n);
            ArrayList<Integer> sc = new ArrayList<>(n);
            for (int i = 0; i < n; ++i) {
                w.add(b.method_19772());
                sc.add(b.method_10816());
            }
            return new Sync(lang, w, sc, b.method_10816(), b.method_10816());
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    public record Challenge(int id, String kind, String subject, String arg, int seconds, int passScore) implements ModPacket {
        public static final ModPacketType<Challenge> TYPE = Packets.type("challenge", (b, p) -> p.write((class_2540)b), Challenge::read);

        @Override
        public void write(class_2540 b) {
            b.method_10804(this.id);
            b.method_10814(this.kind != null ? this.kind : "");
            b.method_10814(this.subject != null ? this.subject : "");
            b.method_10814(this.arg != null ? this.arg : "");
            b.method_10804(this.seconds);
            b.method_10804(this.passScore);
        }

        static Challenge read(class_2540 b) {
            return new Challenge(b.method_10816(), b.method_19772(), b.method_19772(), b.method_19772(), b.method_10816(), b.method_10816());
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    public record Result(int id, int score, String heard) implements ModPacket {
        public static final ModPacketType<Result> TYPE = Packets.type("result", (b, p) -> p.write((class_2540)b), Result::read);

        @Override
        public void write(class_2540 b) {
            b.method_10804(this.id);
            b.method_10804(this.score);
            b.method_10788(this.heard != null ? this.heard : "", 256);
        }

        static Result read(class_2540 b) {
            return new Result(b.method_10816(), b.method_10816(), b.method_10800(256));
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    public record AskLanguage(int unused) implements ModPacket {
        public static final ModPacketType<AskLanguage> TYPE = Packets.type("ask_language", (b, p) -> p.write((class_2540)b), AskLanguage::read);

        @Override
        public void write(class_2540 b) {
            b.method_10804(this.unused);
        }

        static AskLanguage read(class_2540 b) {
            return new AskLanguage(b.method_10816());
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    public record ChooseLanguage(String code) implements ModPacket {
        public static final ModPacketType<ChooseLanguage> TYPE = Packets.type("choose_language", (b, p) -> p.write((class_2540)b), ChooseLanguage::read);

        @Override
        public void write(class_2540 b) {
            b.method_10814(this.code != null ? this.code : "");
        }

        static ChooseLanguage read(class_2540 b) {
            return new ChooseLanguage(b.method_19772());
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    public record Practice(int unused) implements ModPacket {
        public static final ModPacketType<Practice> TYPE = Packets.type("practice", (b, p) -> p.write((class_2540)b), Practice::read);

        @Override
        public void write(class_2540 b) {
            b.method_10804(this.unused);
        }

        static Practice read(class_2540 b) {
            return new Practice(b.method_10816());
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    // =========================================================================
    // MULTIPLAYER COOPERATIVE PACKETS (Requirement R1)
    // =========================================================================

    public record CoopInvoke(String action, String targetPlayer, String subject, String arg) implements ModPacket {
        public static final ModPacketType<CoopInvoke> TYPE = Packets.type("coop_invoke", (b, p) -> p.write((class_2540)b), CoopInvoke::read);

        public CoopInvoke(String action, String arg) {
            this(action, "", "", arg);
        }

        public String actionType() {
            return this.action;
        }

        public String targetPlayerId() {
            return this.targetPlayer;
        }

        @Override
        public void write(class_2540 b) {
            b.method_10814(this.action != null ? this.action : "");
            b.method_10814(this.targetPlayer != null ? this.targetPlayer : "");
            b.method_10814(this.subject != null ? this.subject : "");
            b.method_10814(this.arg != null ? this.arg : "");
        }

        static CoopInvoke read(class_2540 b) {
            return new CoopInvoke(b.method_19772(), b.method_19772(), b.method_19772(), b.method_19772());
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    public record CoopCue(
        String cueType,
        String initiatorName,
        String messageBilingual,
        double x,
        double y,
        double z,
        int durationTicks,
        String ruText,
        String phonetic,
        String ptText,
        String objective
    ) implements ModPacket {
        public static final ModPacketType<CoopCue> TYPE = Packets.type("coop_cue", (b, p) -> p.write((class_2540)b), CoopCue::read);

        public CoopCue(String cueType, String initiatorName, String messageBilingual, double x, double y, double z, int durationTicks) {
            this(cueType, initiatorName, messageBilingual, x, y, z, durationTicks, "", "", "", "");
        }

        public CoopCue(String sender, String action, String ruText, String phonetic, String ptText, String objective, double x, double y, double z, int seconds) {
            this(action, sender, ruText + " | " + ptText, x, y, z, seconds * 20, ruText, phonetic, ptText, objective);
        }

        public String sender() {
            return this.initiatorName;
        }

        public String action() {
            return this.cueType;
        }

        public int seconds() {
            return this.durationTicks > 0 ? this.durationTicks / 20 : 0;
        }

        @Override
        public void write(class_2540 b) {
            b.method_10814(this.cueType != null ? this.cueType : "");
            b.method_10814(this.initiatorName != null ? this.initiatorName : "");
            b.method_10814(this.messageBilingual != null ? this.messageBilingual : "");
            b.writeDouble(this.x);
            b.writeDouble(this.y);
            b.writeDouble(this.z);
            b.method_10804(this.durationTicks);
            b.method_10814(this.ruText != null ? this.ruText : "");
            b.method_10814(this.phonetic != null ? this.phonetic : "");
            b.method_10814(this.ptText != null ? this.ptText : "");
            b.method_10814(this.objective != null ? this.objective : "");
        }

        static CoopCue read(class_2540 b) {
            return new CoopCue(
                b.method_19772(),
                b.method_19772(),
                b.method_19772(),
                b.readDouble(),
                b.readDouble(),
                b.readDouble(),
                b.method_10816(),
                b.method_19772(),
                b.method_19772(),
                b.method_19772(),
                b.method_19772()
            );
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }

    public record CoopSync(
        int score,
        int completedCount,
        String rankTitle,
        String activeChallengeName,
        int timeRemaining,
        int rankTier
    ) implements ModPacket {
        public static final ModPacketType<CoopSync> TYPE = Packets.type("coop_sync", (b, p) -> p.write((class_2540)b), CoopSync::read);

        public CoopSync(int score, int completedCount, String rankTitle, String activeChallengeName, int timeRemaining) {
            this(score, completedCount, rankTitle, activeChallengeName, timeRemaining, calculateTier(score));
        }

        public CoopSync(int score, int rankTier, String rankTitle, int completed) {
            this(score, completed, rankTitle, "", 0, rankTier);
        }

        private static int calculateTier(int s) {
            if (s >= 1000) return 5;
            if (s >= 600) return 4;
            if (s >= 300) return 3;
            if (s >= 100) return 2;
            return 1;
        }

        public int completed() {
            return this.completedCount;
        }

        @Override
        public void write(class_2540 b) {
            b.method_10804(this.score);
            b.method_10804(this.completedCount);
            b.method_10814(this.rankTitle != null ? this.rankTitle : "");
            b.method_10814(this.activeChallengeName != null ? this.activeChallengeName : "");
            b.method_10804(this.timeRemaining);
            b.method_10804(this.rankTier);
        }

        static CoopSync read(class_2540 b) {
            return new CoopSync(
                b.method_10816(),
                b.method_10816(),
                b.method_19772(),
                b.method_19772(),
                b.method_10816(),
                b.method_10816()
            );
        }

        @Override
        public class_2960 channel() {
            return TYPE.channel();
        }
    }
}
