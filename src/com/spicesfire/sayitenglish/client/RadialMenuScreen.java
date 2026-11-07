package com.spicesfire.sayitenglish.client;

import com.spicesfire.sayitenglish.ChallengeManager;
import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.client.ChallengeSession;
import com.spicesfire.sayitenglish.client.ClientCompat;
import com.spicesfire.sayitenglish.client.SayItClient;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class RadialMenuScreen extends class_437 {

    public static final class SectorDef {
        public final String action;
        public final String ruText;
        public final String ptText;
        public final String label;
        public final String iconId;
        private class_1799 cachedStack;

        public SectorDef(String action, String ruText, String ptText, String label, String iconId) {
            this.action = action;
            this.ruText = ruText;
            this.ptText = ptText;
            this.label = label;
            this.iconId = iconId;
        }

        public class_1799 getStack() {
            if ("item".equals(this.action)) {
                net.minecraft.class_310 mc = net.minecraft.class_310.method_1551();
                if (mc != null && mc.field_1724 != null) {
                    class_1799 held = mc.field_1724.method_6047();
                    if (held != null && !held.method_7960()) {
                        return held;
                    }
                }
            }
            if (this.cachedStack == null) {
                class_1792 item = ChallengeManager.item(this.iconId);
                this.cachedStack = item != null ? new class_1799((class_1935)item) : class_1799.field_8037;
            }
            return this.cachedStack;
        }
    }

    public static final SectorDef[] SECTORS = new SectorDef[] {
        new SectorDef("item", "\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e...", "Pedir qualquer item (Buscar)", "Pedir Item", "minecraft:chest"),
        new SectorDef("food", "\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u0430 \u0435\u0434\u0430!", "Preciso de comida!", "Comida", "minecraft:bread"),
        new SectorDef("help", "\u041f\u043e\u043c\u043e\u0433\u0438 \u043c\u043d\u0435!", "Me ajude!", "Ajuda", "minecraft:golden_apple"),
        new SectorDef("lava", "\u041f\u043e\u043b - \u044d\u0442\u043e \u043b\u0430\u0432\u0430!", "O ch\u00e3o \u00e9 lava!", "Lava", "minecraft:lava_bucket"),
        new SectorDef("follow", "\u0421\u043b\u0435\u0434\u0443\u0439 \u0437\u0430 \u043c\u043d\u043e\u0439!", "Siga-me!", "Seguir", "minecraft:compass"),
        new SectorDef("waypoint", "\u0421\u043c\u043e\u0442\u0440\u0438 \u0442\u0443\u0434\u0430!", "Olhe l\u00e1!", "Ponto", "minecraft:spyglass")
    };

    private final long openedAt;
    private int hoveredSector = -1;

    public RadialMenuScreen() {
        super((class_2561)class_2561.method_43470("Roda de A\u00e7\u00f5es Cooperativas"));
        this.openedAt = System.currentTimeMillis();
    }

    @Override
    public boolean method_25421() {
        return false; // Non-pausing screen
    }

    @Override
    public void method_25419() {
        ClientCompat.openScreen(this.field_22787, null);
    }

    public int getHoveredSector() {
        return this.hoveredSector;
    }

    public void selectSector(int index) {
        if (index >= 0 && index < SECTORS.length) {
            SectorDef s = SECTORS[index];
            this.method_25419();
            if ("item".equals(s.action)) {
                ClientCompat.openScreen(this.field_22787, new ItemSearchScreen());
                return;
            }
            ChallengeSession.startCoopChallenge(s.action, s.ruText, s.ptText, s.iconId, 15, Config.get().passScore);
        }
    }

    @Override
    public void method_25394(class_332 g, int mouseX, int mouseY, float partialTick) {
        this.method_25420(g);
        super.method_25394(g, mouseX, mouseY, partialTick);

        int cx = this.field_22789 / 2;
        int cy = this.field_22790 / 2;

        double dx = (double)mouseX - (double)cx;
        double dy = (double)mouseY - (double)cy;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist >= 22.0 && dist <= 150.0) {
            double angleDeg = Math.toDegrees(Math.atan2(dy, dx));
            double shifted = angleDeg + 120.0;
            while (shifted < 0.0) shifted += 360.0;
            while (shifted >= 360.0) shifted -= 360.0;
            int sec = (int)(shifted / 60.0);
            if (sec < 0) sec = 0;
            if (sec > 5) sec = 5;
            this.hoveredSector = sec;
        } else {
            this.hoveredSector = -1;
        }

        // Title and subtitle at top
        ClientCompat.centered(g, this.field_22793, "\u00a76\u00a7lRODA DE A\u00c7\u00d5ES COOPERATIVAS \u00a7e(V)", cx, cy - 118, -14006);
        ClientCompat.centered(g, this.field_22793, "\u00a77Selecione e pronuncie em russo para notificar seu aliado", cx, cy - 105, 0xAAAAAA);

        // Center hub
        int hr = 16;
        ClientCompat.fill(g, cx - hr, cy - hr, cx + hr, cy + hr, -1341124584);
        ClientCompat.fill(g, cx - hr, cy - hr, cx + hr, cy - hr + 1, -14006);
        ClientCompat.fill(g, cx - hr, cy + hr - 1, cx + hr, cy + hr, -14006);
        ClientCompat.fill(g, cx - hr, cy - hr, cx - hr + 1, cy + hr, -14006);
        ClientCompat.fill(g, cx + hr - 1, cy - hr, cx + hr, cy + hr, -14006);
        ClientCompat.centered(g, this.field_22793, "\u00a76\u2694", cx, cy - 4, -14006);

        // 6 Sector buttons
        int boxW = 58;
        int boxH = 34;
        for (int i = 0; i < SECTORS.length; ++i) {
            SectorDef sector = SECTORS[i];
            double angleRad = Math.toRadians(-90.0 + (double)i * 60.0);
            int sx = (int)((double)cx + 68.0 * Math.cos(angleRad));
            int sy = (int)((double)cy + 68.0 * Math.sin(angleRad));
            int x1 = sx - boxW / 2;
            int y1 = sy - boxH / 2;
            int x2 = x1 + boxW;
            int y2 = y1 + boxH;
            boolean isHovered = (i == this.hoveredSector);

            if (isHovered) {
                ClientCompat.fill(g, x1, y1, x2, y2, -803726810);
                ClientCompat.fill(g, x1, y1, x2, y1 + 2, -14006);
                ClientCompat.fill(g, x1, y2 - 2, x2, y2, -14006);
                ClientCompat.fill(g, x1, y1, x1 + 2, y2, -14006);
                ClientCompat.fill(g, x2 - 2, y1, x2, y2, -14006);
                ClientCompat.item(g, sector.getStack(), sx - 8, sy - 13);
                ClientCompat.centered(g, this.field_22793, "\u00a7e" + sector.label, sx, sy + 4, -14006);
            } else {
                ClientCompat.fill(g, x1, y1, x2, y2, -1610612736);
                ClientCompat.fill(g, x1, y1, x2, y1 + 1, 0x66444455);
                ClientCompat.fill(g, x1, y2 - 1, x2, y2, 0x66444455);
                ClientCompat.fill(g, x1, y1, x1 + 1, y2, 0x66444455);
                ClientCompat.fill(g, x2 - 1, y1, x2, y2, 0x66444455);
                ClientCompat.item(g, sector.getStack(), sx - 8, sy - 13);
                ClientCompat.centered(g, this.field_22793, "\u00a77" + sector.label, sx, sy + 4, 0xAA888899);
            }
        }

        // Detail card at bottom
        int cardW = 280;
        int cardH = 46;
        int cardX = cx - cardW / 2;
        int cardY = cy + 96;

        if (this.hoveredSector >= 0) {
            SectorDef sel = SECTORS[this.hoveredSector];
            ClientCompat.fill(g, cardX, cardY, cardX + cardW, cardY + cardH, -1341124584);
            ClientCompat.fill(g, cardX, cardY, cardX + cardW, cardY + 2, -14006);
            ClientCompat.fill(g, cardX, cardY + cardH - 1, cardX + cardW, cardY + cardH, 0x66AAAAAA);
            ClientCompat.fill(g, cardX, cardY, cardX + 1, cardY + cardH, 0x66AAAAAA);
            ClientCompat.fill(g, cardX + cardW - 1, cardY, cardX + cardW, cardY + cardH, 0x66AAAAAA);

            ClientCompat.item(g, sel.getStack(), cardX + 8, cardY + 9);
            ClientCompat.text(g, this.field_22793, "\u00a7e" + sel.ruText, cardX + 32, cardY + 7, -1);
            ClientCompat.text(g, this.field_22793, "\u00a77" + sel.ptText, cardX + 32, cardY + 19, -1);
            ClientCompat.text(g, this.field_22793, "\u00a78Solte V ou clique para iniciar o desafio", cardX + 32, cardY + 31, -1);
        } else {
            ClientCompat.fill(g, cardX, cardY + 10, cardX + cardW, cardY + 36, -1610612736);
            ClientCompat.centered(g, this.field_22793, "\u00a77Mova o cursor para escolher uma a\u00e7\u00e3o", cx, cardY + 19, -1);
        }
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (button == 0 && this.hoveredSector >= 0) {
            this.selectSector(this.hoveredSector);
            return true;
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.method_25419();
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean method_16803(int keyCode, int scanCode, int modifiers) {
        if (SayItClient.isRadialKey(keyCode)) {
            long holdDuration = System.currentTimeMillis() - this.openedAt;
            if (holdDuration > 180L) {
                if (this.hoveredSector >= 0) {
                    this.selectSector(this.hoveredSector);
                } else {
                    this.method_25419();
                }
                return true;
            }
        }
        return super.method_16803(keyCode, scanCode, modifiers);
    }
}
