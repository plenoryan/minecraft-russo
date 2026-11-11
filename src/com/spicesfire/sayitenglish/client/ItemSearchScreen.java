package com.spicesfire.sayitenglish.client;

import com.spicesfire.sayitenglish.Alphabet;
import com.spicesfire.sayitenglish.ChallengeManager;
import com.spicesfire.sayitenglish.Config;
import com.spicesfire.sayitenglish.Packets;
import com.spicesfire.sayitenglish.audio.AudioPlaybackManager;
import com.spicesfire.sayitenglish.client.ChallengeSession;
import com.spicesfire.sayitenglish.client.ClientCompat;
import com.spicesfire.sayitenglish.client.Words;
import com.spicesfire.sayitenglish.platform.ClientPlatform;
import com.spicesfire.sayitenglish.platform.ModPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_7923;

public class ItemSearchScreen extends class_437 {

    public static final class SearchEntry {
        public final String id;
        public final class_1799 stack;
        public final String ptName;
        public final String ruName;

        public SearchEntry(String id, class_1799 stack, String ptName, String ruName) {
            this.id = id;
            this.stack = stack;
            this.ptName = ptName;
            this.ruName = ruName;
        }
    }

    private static final String[] QUICK_ITEM_IDS = new String[] {
        "minecraft:oak_log",
        "minecraft:iron_ingot",
        "minecraft:coal",
        "minecraft:cooked_beef",
        "minecraft:torch",
        "minecraft:diamond",
        "minecraft:raw_copper",
        "farmersrespite:kettle"
    };

    private static final List<SearchEntry> ALL_ITEMS = new ArrayList<>();
    private static boolean catalogBuilt = false;

    private class_342 searchField;
    private final List<SearchEntry> filtered = new ArrayList<>();
    private SearchEntry heldEntry = null;
    private SearchEntry selectedEntry = null;
    private int scrollOffset = 0;
    private int hoveredIndex = -1;
    private boolean transitioningToCoop = false;

    public void setSelectedEntry(SearchEntry entry) {
        this.selectedEntry = entry;
    }

    public SearchEntry getSelectedEntry() {
        if (this.selectedEntry != null) return this.selectedEntry;
        if (this.hoveredIndex >= 0 && this.hoveredIndex < this.filtered.size()) {
            return this.filtered.get(this.hoveredIndex);
        }
        if (this.heldEntry != null) return this.heldEntry;
        if (!this.filtered.isEmpty()) return this.filtered.get(0);
        return null;
    }

    public ItemSearchScreen() {
        super((class_2561)class_2561.method_43470("Pedir Qualquer Item"));
    }

    @Override
    public boolean method_25421() {
        return false; // Non-pausing
    }

    @Override
    public void method_25419() {
        if (!this.transitioningToCoop && ChallengeSession.pendingCoopAction == null) {
            ClientPlatform.I.sendToServer((ModPacket)new Packets.CoopInvoke("coop_busy:false", ""));
            ChallengeSession.pause(false);
        }
        ClientCompat.openScreen(this.field_22787, null);
    }

    private static synchronized void ensureCatalogBuilt() {
        if (catalogBuilt && !ALL_ITEMS.isEmpty()) return;
        ALL_ITEMS.clear();
        for (class_1792 it : class_7923.field_41178) {
            String id = ChallengeManager.itemId(it);
            if (id.equals("minecraft:air")) continue;
            class_1799 stack = new class_1799((class_1935)it);
            String pt = stack.method_7964().getString();
            String ru = Alphabet.getRuWord(id);
            if (ru == null || ru.isEmpty() || ru.equals(id)) {
                ru = pt;
            }
            ALL_ITEMS.add(new SearchEntry(id, stack, pt, ru));
        }
        catalogBuilt = true;
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        ClientPlatform.I.sendToServer((ModPacket)new Packets.CoopInvoke("coop_busy:true", ""));
        ChallengeSession.pause(true);
        ensureCatalogBuilt();

        // Check held item in main hand
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null) {
            class_1799 held = mc.field_1724.method_6047();
            if (held != null && !held.method_7960()) {
                String heldId = ChallengeManager.itemId(held.method_7909());
                String pt = held.method_7964().getString();
                String ru = Alphabet.getRuWord(heldId);
                if (ru == null || ru.isEmpty() || ru.equals(heldId)) ru = pt;
                this.heldEntry = new SearchEntry(heldId, held, pt, ru);
            }
        }

        int cx = this.field_22789 / 2;
        int searchY = 46;

        this.searchField = new class_342(this.field_22793, cx - 130, searchY, 260, 20, class_2561.method_43470("Buscar item..."));
        this.searchField.method_1863(this::onSearchChanged);
        this.searchField.method_1852("");
        this.method_37063(this.searchField);
        this.method_48265(this.searchField);

        // Cancel button at bottom
        int cancelY = this.field_22790 - 28;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Cancelar"), b -> this.method_25419())
            .method_46434(cx - 50, cancelY, 100, 20).method_46431());

        this.updateFilter("");
    }

    private void onSearchChanged(String text) {
        this.scrollOffset = 0;
        this.updateFilter(text);
    }

    private void updateFilter(String query) {
        this.filtered.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) {
            // Default quick items when query is empty
            for (String qId : QUICK_ITEM_IDS) {
                class_1792 it = ChallengeManager.item(qId);
                if (it != null) {
                    class_1799 st = new class_1799((class_1935)it);
                    String pt = st.method_7964().getString();
                    String ru = Alphabet.getRuWord(qId);
                    if (ru == null || ru.isEmpty() || ru.equals(qId)) ru = pt;
                    this.filtered.add(new SearchEntry(qId, st, pt, ru));
                }
            }
            return;
        }

        int count = 0;
        for (SearchEntry e : ALL_ITEMS) {
            if (e.ptName.toLowerCase(Locale.ROOT).contains(q)
                || e.ruName.toLowerCase(Locale.ROOT).contains(q)
                || e.id.toLowerCase(Locale.ROOT).contains(q)) {
                this.filtered.add(e);
                count++;
                if (count >= 50) break; // Limit search results for performance
            }
        }
    }

    public void selectEntry(SearchEntry entry) {
        if (entry == null) return;
        this.transitioningToCoop = true;
        this.method_25419();

        String action = "item:" + entry.id;
        String ruPhrase = "\u041c\u043d\u0435 \u043d\u0443\u0436\u043d\u043e: " + entry.ruName + "!";
        String ptPhrase = "Preciso de " + entry.ptName + "!";
        ChallengeSession.startCoopChallenge(action, ruPhrase, ptPhrase, entry.id, 15, Config.get().passScore);
    }

    private int maxVisibleRows() {
        return Math.max(3, (this.field_22790 - 120) / 24);
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double amount) {
        int max = Math.max(0, this.filtered.size() - this.maxVisibleRows());
        this.scrollOffset = Math.max(0, Math.min(max, this.scrollOffset - (int)Math.signum(amount)));
        return true;
    }

    @Override
    public void method_25394(class_332 g, int mouseX, int mouseY, float partialTick) {
        this.method_25420(g);
        super.method_25394(g, mouseX, mouseY, partialTick);

        int cx = this.field_22789 / 2;

        // Title and instructions
        ClientCompat.centered(g, this.field_22793, "\u00a76\u00a7lPEDIR QUALQUER ITEM AO ALIADO", cx, 12, -14006);
        ClientCompat.centered(g, this.field_22793, "\u00a77Pesquise ou selecione um item \u2022 \u00a7b[H] Ouvir pron\u00fancia", cx, 26, 0xAAAAAA);

        int listX = cx - 130;
        int listY = 72;
        int listW = 260;
        int visibleRows = this.maxVisibleRows();
        int listH = visibleRows * 24;

        // List background
        ClientCompat.fill(g, listX - 2, listY - 2, listX + listW + 2, listY + listH + 2, -1341124584);
        ClientCompat.fill(g, listX - 2, listY - 2, listX + listW + 2, listY - 1, -14006);
        ClientCompat.fill(g, listX - 2, listY + listH + 1, listX + listW + 2, listY + listH + 2, -14006);
        ClientCompat.fill(g, listX - 2, listY - 2, listX - 1, listY + listH + 2, -14006);
        ClientCompat.fill(g, listX + listW + 1, listY - 2, listX + listW + 2, listY + listH + 2, -14006);

        this.hoveredIndex = -1;

        if (this.filtered.isEmpty()) {
            ClientCompat.centered(g, this.field_22793, "\u00a77Nenhum item encontrado", cx, listY + listH / 2 - 4, 0x888888);
        } else {
            for (int i = 0; i < visibleRows; ++i) {
                int itemIdx = this.scrollOffset + i;
                if (itemIdx >= this.filtered.size()) break;
                SearchEntry entry = this.filtered.get(itemIdx);
                int rowY = listY + i * 24;

                boolean isHover = mouseX >= listX && mouseX <= listX + listW && mouseY >= rowY && mouseY < rowY + 24;
                if (isHover) {
                    this.hoveredIndex = itemIdx;
                    ClientCompat.fill(g, listX, rowY, listX + listW, rowY + 24, -803726810);
                    ClientCompat.text(g, this.field_22793, "\u00a7b[H] Ouvir", listX + listW - 55, rowY + 8, -14006);
                }

                // Render item stack
                ClientCompat.item(g, entry.stack, listX + 4, rowY + 4);

                // Render names
                ClientCompat.text(g, this.field_22793, "\u00a7f" + entry.ptName, listX + 26, rowY + 3, -1);
                ClientCompat.text(g, this.field_22793, "\u00a76" + entry.ruName, listX + 26, rowY + 13, -14006);
            }
        }

        // Held item quick button if available
        if (this.heldEntry != null) {
            int hx = cx - 130;
            int hy = listY + listH + 6;
            boolean isHeldHover = mouseX >= hx && mouseX <= hx + listW && mouseY >= hy && mouseY <= hy + 18;
            int bg = isHeldHover ? -803726810 : -1610612736;
            ClientCompat.fill(g, hx, hy, hx + listW, hy + 18, bg);
            ClientCompat.fill(g, hx, hy, hx + listW, hy + 1, -14006);
            ClientCompat.fill(g, hx, hy + 17, hx + listW, hy + 18, -14006);
            ClientCompat.item(g, this.heldEntry.stack, hx + 3, hy + 1);
            ClientCompat.text(g, this.field_22793, "\u00a7e[Pedir item na m\u00e3o: \u00a7f" + this.heldEntry.ptName + "\u00a7e]", hx + 22, hy + 5, -1);
        }
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Check held item button click
            if (this.heldEntry != null) {
                int cx = this.field_22789 / 2;
                int listX = cx - 130;
                int listY = 72;
                int listW = 260;
                int listH = this.maxVisibleRows() * 24;
                int hy = listY + listH + 6;
                if (mouseX >= listX && mouseX <= listX + listW && mouseY >= hy && mouseY <= hy + 18) {
                    this.selectEntry(this.heldEntry);
                    return true;
                }
            }

            // Check list item click
            if (this.hoveredIndex >= 0 && this.hoveredIndex < this.filtered.size()) {
                this.selectEntry(this.filtered.get(this.hoveredIndex));
                return true;
            }
        } else if (button == 1) { // Right click to preview Russian pronunciation
            if (this.hoveredIndex >= 0 && this.hoveredIndex < this.filtered.size()) {
                SearchEntry entry = this.filtered.get(this.hoveredIndex);
                if (entry != null && entry.ruName != null && !entry.ruName.isEmpty()) {
                    AudioPlaybackManager.play(entry.ruName);
                    return true;
                }
            }
            if (this.heldEntry != null) {
                int cx = this.field_22789 / 2;
                int listX = cx - 130;
                int listY = 72;
                int listW = 260;
                int listH = this.maxVisibleRows() * 24;
                int hy = listY + listH + 6;
                if (mouseX >= listX && mouseX <= listX + listW && mouseY >= hy && mouseY <= hy + 18) {
                    AudioPlaybackManager.play(this.heldEntry.ruName);
                    return true;
                }
            }
        }
        return super.method_25402(mouseX, mouseY, button);
    }

    @Override
    public boolean method_25404(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.method_25419();
            return true;
        }
        if (SayItClient.isHearKey(keyCode)) {
            SearchEntry entry = getSelectedEntry();
            if (entry != null && entry.ruName != null && !entry.ruName.isEmpty()) {
                AudioPlaybackManager.play(entry.ruName);
                return true;
            }
        }
        if (this.searchField != null && this.searchField.method_25404(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.method_25404(keyCode, scanCode, modifiers);
    }
}
