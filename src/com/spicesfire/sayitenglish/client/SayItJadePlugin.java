package com.spicesfire.sayitenglish.client;

import com.spicesfire.sayitenglish.Alphabet;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2248;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import snownee.jade.api.Accessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("sayitenglish")
public class SayItJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addTooltipCollectedCallback((tooltip, accessor) -> {
            try {
                class_2960 id = null;
                class_1799 picked = accessor.getPickedResult();
                if (picked != null && !picked.method_7960()) {
                    class_1792 item = picked.method_7909();
                    id = class_7923.field_41178.method_10221(item);
                }
                if (id == null) {
                    Object target = accessor.getTarget();
                    if (target instanceof class_2680) {
                        class_2680 state = (class_2680) target;
                        class_2248 block = state.method_26204();
                        id = class_7923.field_41175.method_10221(block);
                    } else if (target instanceof class_1297) {
                        class_1297 entity = (class_1297) target;
                        class_1299<?> type = entity.method_5864();
                        id = class_7923.field_41177.method_10221(type);
                    }
                }
                if (id == null) return;

                String itemId = id.toString();
                String ru = Alphabet.getRuWord(itemId);
                if (ru == null || ru.isEmpty() || ru.equals(itemId)) return;

                boolean hasCyr = false;
                for (int i = 0; i < ru.length(); i++) {
                    char c = ru.charAt(i);
                    if (c >= '\u0400' && c <= '\u04ff') {
                        hasCyr = true;
                        break;
                    }
                }
                if (!hasCyr) return;

                String phon = Romanizer.of(ru, "ru");
                String line = "\u00a76\u2605 Russo: \u00a7f" + ru + (phon != null && !phon.isEmpty() ? " \u00a77[" + phon + "]" : "");
                tooltip.add(class_2561.method_43470(line));
            } catch (Throwable ignored) {
            }
        });
    }
}
