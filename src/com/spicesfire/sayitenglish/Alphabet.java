package com.spicesfire.sayitenglish;

import com.google.gson.Gson;
import com.spicesfire.sayitenglish.platform.Platform;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class Alphabet {
    public static final Map<Character, Integer> LETTER_LEVEL = new HashMap<>();
    public static final Map<Character, String> LATIN_FALLBACK = new HashMap<>();
    public static final String[] LEVEL_NAME = new String[33];
    public static final String[] LEVEL_MNEM = new String[33];

    // Real-time client exposure tracking (Heatmap)
    public static final Map<Character, Integer> CLIENT_HEAT = new ConcurrentHashMap<>();

    private static Map<String, String> RU_DICT = null;

    static {
        // Fase 1: Gemeas Transparentes (Niveis 1 a 6)
        reg(1, '\u0430', '\u0410', "a", "A", "\u0410, \u0430 [A]", "Identica ao portugues");
        reg(2, '\u043e', '\u041e', "o", "O", "\u041e, \u043e [O]", "Identica ao portugues");
        reg(3, '\u0442', '\u0422', "t", "T", "\u0422, \u0442 [T]", "Identica ao portugues");
        reg(4, '\u043a', '\u041a', "k", "K", "\u041a, \u043a [K]", "Identica ao K/C duro");
        reg(5, '\u043c', '\u041c', "m", "M", "\u041c, \u043c [M]", "Identica ao portugues");
        reg(6, '\u0435', '\u0415', "e", "E", "\u0415, \u0435 [Ye/E]", "Som de Ye/E (como em Erva)");

        // Fase 2: Falsos Amigos Cardinais (Niveis 7 a 12)
        reg(7, '\u0432', '\u0412', "v", "V", "\u0412, \u0432 [V]", "Parece B, mas tem som de V de Vitoria!");
        reg(8, '\u0440', '\u0420', "r", "R", "\u0420, \u0440 [R]", "Parece P, mas tem som de R de Roda!");
        reg(9, '\u0441', '\u0421', "s", "S", "\u0421, \u0441 [S]", "Parece C, mas tem sempre som de S de Sol!");
        reg(10, '\u043d', '\u041d', "n", "N", "\u041d, \u043d [N]", "Parece H, mas tem som de N de Navio!");
        reg(11, '\u0443', '\u0423', "u", "U", "\u0423, \u0443 [U]", "Parece Y, mas tem som de U de Urso!");
        reg(12, '\u0445', '\u0425', "kh", "Kh", "\u0425, \u0445 [Kh]", "Parece X, mas tem som de Kh (aspirado forte)!");

        // Fase 3: Construtores de Ferramentas e Letras Gregas (Niveis 13 a 19)
        reg(13, '\u0434', '\u0414', "d", "D", "\u0414, \u0434 [D]", "Domo ou cabana de madeira = D");
        reg(14, '\u043f', '\u041f', "p", "P", "\u041f, \u043f [P]", "Porta ou portico grego = P");
        reg(15, '\u0431', '\u0411', "b", "B", "\u0411, \u0431 [B]", "B com bone superior = B");
        reg(16, '\u0433', '\u0413', "g", "G", "\u0413, \u0433 [G]", "Gancho invertido = G gato");
        reg(17, '\u043b', '\u041b', "l", "L", "\u041b, \u043b [L]", "Tenda de acampamento / Lambda = L");
        reg(18, '\u0438', '\u0418', "i", "I", "\u0418, \u0438 [I]", "N espelhado = I puro");
        reg(19, '\u0439', '\u0419', "y", "Y", "\u0419, \u0439 [Y]", "Semivogal breve I-kratkoye = Y");

        // Fase 4: Sons Eslavos e Chiantes (Niveis 20 a 26)
        reg(20, '\u0436', '\u0416', "zh", "Zh", "\u0416, \u0436 [Zh]", "Besouro com patinhas = J frances");
        reg(21, '\u0437', '\u0417', "z", "Z", "\u0417, \u0437 [Z]", "Parece o numero 3 = Z de zebra");
        reg(22, '\u0447', '\u0427', "ch", "Ch", "\u0427, \u0447 [Ch]", "Numero 4 invertido = Tche");
        reg(23, '\u0448', '\u0428', "sh", "Sh", "\u0428, \u0448 [Sh]", "Garfo de 3 dentes retos = Sh/Ch");
        reg(24, '\u0449', '\u0429', "shch", "Shch", "\u0429, \u0449 [Shch]", "Garfo com cauda = Shch longo");
        reg(25, '\u0446', '\u0426', "ts", "Ts", "\u0426, \u0446 [Ts]", "Letra com pe = PiTSa");
        reg(26, '\u0444', '\u0424', "f", "F", "\u0424, \u0444 [F]", "Circulo cortado = F");

        // Fase 5: Iodizadas e Modificadores (Niveis 27 a 32)
        reg(27, '\u044f', '\u042f', "ya", "Ya", "\u042f, \u044f [Ya]", "R espelhado = EU / Ya");
        reg(28, '\u044e', '\u042e', "yu", "Yu", "\u042e, \u044e [Yu]", "I e O de maos dadas = Yu");
        reg(29, '\u0451', '\u0401', "yo", "Yo", "\u0401, \u0451 [Yo]", "E com olhos = Yo sempre tonica");
        reg(30, '\u044b', '\u042b', "y", "Y", "\u042b, \u044b [Y]", "Vogal gutural profunda");
        reg(31, '\u044d', '\u042d', "e", "E", "\u042d, \u044d [E]", "C invertido com lingua = E aberto");
        reg(32, '\u044c', '\u042c', "'", "'", "\u042c, \u044c [Sinal Brando]", "Mudo: amolece e palatiza a consoante");
        reg(32, '\u044a', '\u042a', "-", "-", "\u042a, \u044a [Sinal Duro]", "Mudo: cria pausa seca antes da raiz");
    }

    private static void reg(int lvl, char lo, char up, String latLo, String latUp, String name, String mnem) {
        LETTER_LEVEL.put(lo, lvl);
        LETTER_LEVEL.put(up, lvl);
        LATIN_FALLBACK.put(lo, latLo);
        LATIN_FALLBACK.put(up, latUp);
        if (LEVEL_NAME[lvl] == null) {
            LEVEL_NAME[lvl] = name;
            LEVEL_MNEM[lvl] = mnem;
        }
    }

    public static int getHeatThreshold(char ch) {
        Integer lvl = LETTER_LEVEL.get(Character.toLowerCase(ch));
        if (lvl == null) return 10;
        if (lvl <= 6) return 3;    // Gemeas: 3 aparicoes
        if (lvl <= 12) return 5;   // Falsos Amigos: 5 aparicoes
        if (lvl <= 19) return 7;   // Construtores: 7 aparicoes
        if (lvl <= 26) return 9;   // Chiantes: 9 aparicoes
        return 12;                 // Iodizadas / Sinais: 12 aparicoes
    }

    public static List<Character> getLettersOrderedByDifficulty(String ru) {
        if (ru == null || ru.isEmpty()) return Collections.emptyList();
        String clean = ru.toLowerCase().replaceAll("[\u0300-\u036f]", "");
        Set<Character> unique = new LinkedHashSet<>();
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (LETTER_LEVEL.containsKey(c)) {
                unique.add(c);
            }
        }
        List<Character> list = new ArrayList<>(unique);
        list.sort(Comparator.comparingInt(c -> LETTER_LEVEL.getOrDefault(c, 99)));
        return list;
    }

    public static void recordExposure(String ru, int amount) {
        if (ru == null || ru.isEmpty()) return;
        String clean = ru.toLowerCase().replaceAll("[\u0300-\u036f]", "");
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (LETTER_LEVEL.containsKey(c)) {
                CLIENT_HEAT.merge(c, amount, Integer::sum);
            }
        }
    }

    public static int getHeat(char c) {
        return CLIENT_HEAT.getOrDefault(Character.toLowerCase(c), 0);
    }

    public static synchronized String getRuWord(String itemIdOrWord) {
        if (itemIdOrWord == null || itemIdOrWord.isEmpty()) return "";
        for (int i = 0; i < itemIdOrWord.length(); i++) {
            char c = itemIdOrWord.charAt(i);
            if (c >= '\u0400' && c <= '\u04ff') return itemIdOrWord;
        }

        if (RU_DICT == null) {
            RU_DICT = new HashMap<>();
            Path p = null;
            try {
                p = Platform.I.configDir().resolve("sayit/lang/ru_ru.json");
            } catch (Throwable t) {
                p = java.nio.file.Paths.get("config/sayit/lang/ru_ru.json");
                if (!Files.exists(p)) {
                    p = java.nio.file.Paths.get("Instancia_Cliente/config/sayit/lang/ru_ru.json");
                }
                if (!Files.exists(p)) {
                    p = java.nio.file.Paths.get("Servidor_Dedicado/config/sayit/lang/ru_ru.json");
                }
            }
            if (p != null && Files.exists(p)) {
                try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                    Map<?, ?> map = new Gson().fromJson(r, Map.class);
                    for (Map.Entry<?, ?> e : map.entrySet()) {
                        RU_DICT.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                    }
                } catch (Exception ignored) {
                }
            }
        }

        String val = RU_DICT.get(itemIdOrWord);
        if (val != null) return val;
        String cleanId = itemIdOrWord.startsWith("minecraft:") ? itemIdOrWord.substring(10) : itemIdOrWord.replace(':', '.');
        val = RU_DICT.get("block.minecraft." + cleanId);
        if (val != null) return val;
        val = RU_DICT.get("item.minecraft." + cleanId);
        if (val != null) return val;
        val = RU_DICT.get("block." + cleanId);
        if (val != null) return val;
        val = RU_DICT.get("item." + cleanId);
        if (val != null) return val;
        return itemIdOrWord;
    }

    public static Set<Character> computeUnlockedLetters(Map<String, Integer> wordStars, Map<?, ?> heatMap) {
        Set<Character> unlocked = new HashSet<>();

        // 1. Heatmap: letters seen often enough unlock automatically
        if (heatMap != null) {
            for (Map.Entry<?, ?> entry : heatMap.entrySet()) {
                char ch;
                Object key = entry.getKey();
                if (key instanceof Character) {
                    ch = Character.toLowerCase((Character) key);
                } else {
                    String s = String.valueOf(key);
                    if (s.isEmpty()) continue;
                    ch = Character.toLowerCase(s.charAt(0));
                }
                int count = 0;
                Object val = entry.getValue();
                if (val instanceof Number) {
                    count = ((Number) val).intValue();
                }
                if (count >= getHeatThreshold(ch)) {
                    unlocked.add(ch);
                    unlocked.add(Character.toUpperCase(ch));
                }
            }
        }

        // 2. Individual word progress: stars on words unlock letters contained in those words
        if (wordStars != null) {
            for (Map.Entry<String, Integer> entry : wordStars.entrySet()) {
                String wordOrId = entry.getKey();
                int stars = entry.getValue();
                if (stars <= 0) continue;

                String ru = getRuWord(wordOrId);
                List<Character> wordLetters = getLettersOrderedByDifficulty(ru);
                int unlockCount = (stars >= 5) ? wordLetters.size() : Math.min(stars, wordLetters.size());
                for (int i = 0; i < unlockCount; i++) {
                    char c = wordLetters.get(i);
                    unlocked.add(c);
                    unlocked.add(Character.toUpperCase(c));
                }
            }
        }

        return unlocked;
    }

    public static String toSmartHybrid(String ru, Set<Character> unlocked, boolean formatCodes) {
        if (ru == null || ru.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String clean = ru.replaceAll("[\u0300-\u036f]", "");
        for (int i = 0; i < clean.length(); i++) {
            char ch = clean.charAt(i);
            if (!LETTER_LEVEL.containsKey(Character.toLowerCase(ch))) {
                sb.append(ch);
                continue;
            }
            if (unlocked != null && unlocked.contains(ch)) {
                if (formatCodes) sb.append("\u00a76");
                sb.append(ch);
                if (formatCodes) sb.append("\u00a7f");
            } else {
                String lat = LATIN_FALLBACK.get(ch);
                sb.append(lat != null ? lat : ch);
            }
        }
        return sb.toString();
    }

    public static Character getNextFocusLetter(String ru, Set<Character> unlocked) {
        List<Character> list = getLettersOrderedByDifficulty(ru);
        for (char c : list) {
            if (unlocked == null || !unlocked.contains(c)) {
                return c;
            }
        }
        return null;
    }

    public static int getLetterCount(Set<Character> unlocked) {
        if (unlocked == null || unlocked.isEmpty()) return 0;
        int count = 0;
        for (char c : unlocked) {
            if (Character.isLowerCase(c)) count++;
        }
        return count;
    }

    // Legacy fallback compatibility
    public static int calculateLevel(int totalStars) {
        if (totalStars <= 0) return 0;
        return Math.min(32, (totalStars + 1) / 2);
    }

    public static String toHybrid(String ru, int playerLvl, boolean formatCodes) {
        if (ru == null || ru.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        String clean = ru.replaceAll("[\u0300-\u036f]", "");
        for (int i = 0; i < clean.length(); i++) {
            char ch = clean.charAt(i);
            Integer lvl = LETTER_LEVEL.get(ch);
            if (lvl == null) {
                sb.append(ch);
                continue;
            }
            if (lvl <= playerLvl) {
                if (formatCodes) sb.append("\u00a76");
                sb.append(ch);
                if (formatCodes) sb.append("\u00a7f");
            } else {
                String lat = LATIN_FALLBACK.get(ch);
                sb.append(lat != null ? lat : ch);
            }
        }
        return sb.toString();
    }

    private Alphabet() {
    }
}
