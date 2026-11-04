package com.spicesfire.sayitenglish;

import java.util.HashMap;
import java.util.Map;

public final class Alphabet {
    public static final Map<Character, Integer> LETTER_LEVEL = new HashMap<>();
    public static final Map<Character, String> LATIN_FALLBACK = new HashMap<>();
    public static final String[] LEVEL_NAME = new String[33];
    public static final String[] LEVEL_MNEM = new String[33];

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

    public static int calculateLevel(int totalStars) {
        if (totalStars <= 0) {
            return 0;
        }
        return Math.min(32, (totalStars + 1) / 2);
    }

    public static String toHybrid(String ru, int playerLvl, boolean formatCodes) {
        if (ru == null || ru.isEmpty()) {
            return "";
        }
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
                if (formatCodes) {
                    sb.append("\u00a76");
                }
                sb.append(ch);
                if (formatCodes) {
                    sb.append("\u00a7f");
                }
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
