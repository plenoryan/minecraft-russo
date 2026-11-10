package com.spicesfire.sayitenglish.client;

import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.DictionaryItem;
import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.SayIt;
import com.spicesfire.sayitenglish.client.ChallengeHud;
import com.spicesfire.sayitenglish.client.ChallengeSession;
import com.spicesfire.sayitenglish.client.ClientCompat;
import com.spicesfire.sayitenglish.client.DictionaryScreen;
import com.spicesfire.sayitenglish.client.LanguageScreen;
import com.spicesfire.sayitenglish.client.RadialMenuScreen;
import com.spicesfire.sayitenglish.client.SettingsScreen;
import com.spicesfire.sayitenglish.client.SpeechEngine;
import com.spicesfire.sayitenglish.client.TypeAnswerScreen;
import com.spicesfire.sayitenglish.client.Words;
import com.spicesfire.sayitenglish.platform.ClientPlatform;
import com.spicesfire.sayitenglish.audio.AudioPlaybackManager;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;

public final class SayItClient {
    private static class_304 typeKey;
    private static class_304 settingsKey;
    private static class_304 radialKey;
    private static class_304 hearKey;
    private static String prepared;
    private static boolean irisReloadUnbound = false;

    private SayItClient() {
    }

    public static void init() {
        ClientPlatform c = ClientPlatform.I;
        typeKey = ClientCompat.key("key.sayitenglish.type", 89);
        c.registerKey(typeKey);
        settingsKey = ClientCompat.key("key.sayitenglish.settings", 85);
        c.registerKey(settingsKey);
        radialKey = ClientCompat.key("key.sayitenglish.radial", 86);
        c.registerKey(radialKey);
        hearKey = ClientCompat.key("key.sayitenglish.hear", 72);
        c.registerKey(hearKey);
        c.hudLayer(SayIt.id("challenge"), ChallengeHud::render);
        c.settingsScreen(SettingsScreen::new);
        c.receivePacket(Packets.Challenge.TYPE, ChallengeSession::start);
        c.receivePacket(Packets.Sync.TYPE, ChallengeSession::sync);
        c.receivePacket(Packets.AskLanguage.TYPE, p -> {
            ChallengeSession.askLanguage = true;
        });
        c.receivePacket(Packets.CoopCue.TYPE, ChallengeHud::onCoopCue);
        c.onWorldLeave(() -> {
            AudioPlaybackManager.stop();
            ChallengeSession.stop();
            ChallengeSession.banner = null;
            Words.forget();
            prepared = "";
            ChallengeSession.synced = false;
            ChallengeSession.askLanguage = false;
            ChallengeHud.activeCoopCue = null;
        });
        c.onClientTickEnd(SayItClient::tick);
        DictionaryItem.openScreen = () -> class_310.method_1551().execute(() -> ClientCompat.openScreen(class_310.method_1551(), new DictionaryScreen()));
    }

    private static void tick() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }

        if (!irisReloadUnbound) {
            unbindIrisReloadKey();
            irisReloadUnbound = true;
        }

        if (ChallengeSession.askLanguage) {
            if (ClientCompat.currentScreen(mc) == null) {
                ClientCompat.openScreen(mc, new LanguageScreen(null));
            }
        } else if (ChallengeSession.synced && !prepared.equals(ChallengeSession.language)) {
            prepared = ChallengeSession.language;
            Words.preload();
        }
        if (ChallengeSession.synced && !ChallengeSession.askLanguage) {
            SpeechEngine.forConfig(Config.get().engine, ChallengeSession.language).prepare(ChallengeSession.language);
        }
        while (typeKey.method_1436()) {
            if (ChallengeSession.current == null || ClientCompat.currentScreen(mc) != null) continue;
            ClientCompat.openScreen(mc, new TypeAnswerScreen());
        }
        while (settingsKey.method_1436()) {
            if (ClientCompat.currentScreen(mc) != null) continue;
            ClientCompat.openScreen(mc, new SettingsScreen(null));
        }
        while (radialKey.method_1436()) {
            if (ClientCompat.currentScreen(mc) != null) continue;
            ClientCompat.openScreen(mc, new RadialMenuScreen());
        }
        while (hearKey.method_1436()) {
            if (ChallengeSession.current == null || ClientCompat.currentScreen(mc) != null) continue;
            ChallengeSession.triggerHear();
        }
        ChallengeSession.tick();
    }

    private static void unbindIrisReloadKey() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1690 == null || mc.field_1690.field_1839 == null) return;
        boolean changed = false;
        for (class_304 key : mc.field_1690.field_1839) {
            if (key == null) continue;
            String name = key.method_1431();
            if ("iris.keybind.reload".equals(name) || "key.iris.reloadShaders".equals(name)) {
                if (!key.method_1429().equals(class_3675.field_16237)) {
                    key.method_1422(class_3675.field_16237);
                    changed = true;
                }
            }
        }
        if (changed) {
            class_304.method_1424();
            mc.field_1690.method_1640();
        }
    }

    public static boolean isRadialKey(int keyCode) {
        if (radialKey == null) return keyCode == 86;
        try {
            return radialKey.method_1429().method_1444() == keyCode || keyCode == 86;
        } catch (Throwable t) {
            return keyCode == 86;
        }
    }

    public static boolean isHearKey(int keyCode) {
        if (hearKey == null) return keyCode == 72;
        try {
            return hearKey.method_1429().method_1444() == keyCode || keyCode == 72;
        } catch (Throwable t) {
            return keyCode == 72;
        }
    }

    public static String typeKeyName() {
        return typeKey == null ? "Y" : typeKey.method_16007().getString();
    }

    public static String radialKeyName() {
        return radialKey == null ? "V" : radialKey.method_16007().getString();
    }

    public static String hearKeyName() {
        return hearKey == null ? "H" : hearKey.method_16007().getString();
    }

    static {
        prepared = "";
    }
}
