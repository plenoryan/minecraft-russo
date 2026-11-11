package com.spicesfire.sayitenglish;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Scorer {
    private Scorer() {
    }

    public static int score(String heard, String target) {
        return Scorer.score(heard, target, true);
    }

    public static int score(String heard, String target, boolean english) {
        List<String> h = Scorer.words(heard);
        List<String> t = Scorer.words(target);
        if (h.isEmpty() || t.isEmpty()) {
            return 0;
        }
        String tj = String.join("", t);
        double best = 0.0;
        for (int len = Math.max(1, t.size() - 1); len <= Math.min(h.size(), t.size() + 2); ++len) {
            int i = 0;
            while (i + len <= h.size()) {
                String hj = String.join("", h.subList(i, i + len));
                best = Math.max(best, english ? Scorer.blended(hj, tj) : Scorer.similarity(hj, tj));
                ++i;
            }
        }
        if (t.size() >= 2) {
            best = Math.max(best, Scorer.wordCoverage(h, t, english));
        }

        // Soft-sign (\u044c, \u044a) relaxed whole-phrase comparison for Cyrillic/Russian
        if (!english) {
            String hNoSoft = String.join("", h).replace("\u044c", "").replace("\u044a", "");
            String tNoSoft = tj.replace("\u044c", "").replace("\u044a", "");
            if (!hNoSoft.isEmpty() && !tNoSoft.isEmpty()) {
                best = Math.max(best, Scorer.similarity(hNoSoft, tNoSoft));
            }
        }

        return (int)Math.round(Math.min(1.0, best) * 100.0);
    }

    private static double wordCoverage(List<String> h, List<String> t, boolean english) {
        double sum = 0.0;
        boolean[] used = new boolean[h.size()];
        for (String tw : t) {
            double b = 0.0;
            int at = -1;
            for (int i = 0; i < h.size(); ++i) {
                if (used[i]) continue;
                double v = english ? Scorer.blended(h.get(i), tw) : Scorer.similarity(h.get(i), tw);
                if (v > b) {
                    b = v;
                    at = i;
                }
            }
            if (at >= 0 && b >= 0.75) {
                used[at] = true;
            }
            sum += b >= 0.75 ? b : b * 0.5;
        }
        return sum / (double)t.size();
    }

    public static List<String> words(String s) {
        ArrayList<String> out = new ArrayList<>();
        if (s == null) {
            return out;
        }
        String n = s.replace("\u0131", "i").replace("\u0130", "i").replace("\u00df", "ss");
        n = Normalizer.normalize(n, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
        for (String w : n.split("[^\\p{L}\\p{N}]+")) {
            if (w.isEmpty()) continue;
            if (w.codePoints().anyMatch(Scorer::han)) {
                StringBuilder rest = new StringBuilder();
                for (int cp : w.codePoints().toArray()) {
                    if (Scorer.han(cp)) {
                        if (rest.length() > 0) {
                            out.add(rest.toString());
                            rest.setLength(0);
                        }
                        out.add(new String(Character.toChars(cp)));
                        continue;
                    }
                    rest.appendCodePoint(cp);
                }
                if (rest.length() <= 0) continue;
                out.add(rest.toString());
                continue;
            }
            out.add(w);
        }
        return out;
    }

    static double blended(String a, String b) {
        double letters = Scorer.similarity(a, b);
        return Math.max(letters, 0.85 * Scorer.similarity(Scorer.sound(a), Scorer.sound(b)) + 0.15 * letters);
    }

    static boolean han(int codePoint) {
        Character.UnicodeScript s = Character.UnicodeScript.of(codePoint);
        return s == Character.UnicodeScript.HAN || s == Character.UnicodeScript.HIRAGANA || s == Character.UnicodeScript.KATAKANA;
    }

    static double similarity(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 1.0;
        }
        int d = Scorer.levenshtein(a, b);
        return 1.0 - (double)d / (double)Math.max(a.length(), b.length());
    }

    static int levenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] cur = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); ++j) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); ++i) {
            cur[0] = i;
            for (int j = 1; j <= b.length(); ++j) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                cur[j] = Math.min(Math.min(cur[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] x = prev;
            prev = cur;
            cur = x;
        }
        return prev[b.length()];
    }

    static String sound(String w) {
        String s = w.replace("ph", "f").replace("ck", "k").replace("qu", "kw").replace("wh", "w").replace("tion", "xn").replace("th", "0").replace("sh", "x").replace("ch", "x").replace("gh", "");
        StringBuilder b = new StringBuilder();
        char last = '\0';
        for (int i = 0; i < s.length(); ++i) {
            char k;
            char c = s.charAt(i);
            switch (c) {
                case 'a':
                case 'e':
                case 'i':
                case 'o':
                case 'u':
                case 'y':
                    k = 'a';
                    break;
                case 'c':
                case 'g':
                case 'k':
                case 'q':
                    k = 'k';
                    break;
                case 's':
                case 'z':
                    k = 's';
                    break;
                case 'b':
                case 'p':
                    k = 'p';
                    break;
                case 'd':
                case 't':
                    k = 't';
                    break;
                case 'f':
                case 'v':
                case 'w':
                    k = 'f';
                    break;
                case 'm':
                case 'n':
                    k = 'n';
                    break;
                case 'h':
                    k = '\0';
                    break;
                default:
                    k = c;
            }
            if (k == '\0' || k == last) continue;
            b.append(k);
            last = k;
        }
        return b.toString();
    }
}
