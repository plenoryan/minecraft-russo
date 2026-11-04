package com.spicesfire.sayitenglish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_5218;
import net.minecraft.server.MinecraftServer;

public final class Progress {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public String language;
    public Map<String, Map<String, Integer>> byLanguage = new LinkedHashMap<>();
    public Map<String, Map<String, Long>> timestamps = new LinkedHashMap<>();
    public Map<String, Map<String, Integer>> reps = new LinkedHashMap<>();
    public Map<String, Map<String, Integer>> errors = new LinkedHashMap<>();
    public Map<String, Integer> letterHeat = new LinkedHashMap<>();
    public Map<String, Integer> words;
    public int sentences;
    public int lost;
    public boolean gotDictionary;
    public boolean starterDictionary;

    public Progress() {
    }

    public Map<String, Integer> words() {
        String lang = (this.language == null) ? "en" : this.language;
        return this.byLanguage.computeIfAbsent(lang, k -> new LinkedHashMap<>());
    }

    public int totalStars() {
        int sum = 0;
        for (int stars : this.words().values()) {
            sum += Math.max(0, stars);
        }
        return sum;
    }

    public Map<String, Integer> letterHeat() {
        if (this.letterHeat == null) {
            this.letterHeat = new LinkedHashMap<>();
        }
        return this.letterHeat;
    }

    public void addHeat(String word, int amount) {
        if (word == null || word.isEmpty() || amount <= 0) return;
        Map<String, Integer> heat = letterHeat();
        String clean = word.toLowerCase().replaceAll("[\u0300-\u036f]", "");
        Set<Character> seen = new HashSet<>();
        for (int i = 0; i < clean.length(); i++) {
            char ch = clean.charAt(i);
            if (Alphabet.LETTER_LEVEL.containsKey(ch) && seen.add(ch)) {
                String k = String.valueOf(ch);
                heat.put(k, heat.getOrDefault(k, 0) + amount);
            }
        }
    }

    public Map<String, Integer> reps() {
        if (this.reps == null) {
            this.reps = new LinkedHashMap<>();
        }
        String lang = (this.language == null) ? "en" : this.language;
        return this.reps.computeIfAbsent(lang, k -> new LinkedHashMap<>());
    }

    public Map<String, Integer> errors() {
        if (this.errors == null) {
            this.errors = new LinkedHashMap<>();
        }
        String lang = (this.language == null) ? "en" : this.language;
        return this.errors.computeIfAbsent(lang, k -> new LinkedHashMap<>());
    }

    public Map<String, Long> timestamps() {
        if (this.timestamps == null) {
            this.timestamps = new LinkedHashMap<>();
        }
        String lang = (this.language == null) ? "en" : this.language;
        return this.timestamps.computeIfAbsent(lang, k -> new LinkedHashMap<>());
    }

    private static Path file(MinecraftServer server, UUID uuid) {
        return server.method_27050(class_5218.field_24188).resolve("sayitenglish").resolve(uuid.toString() + ".json");
    }

    public static Progress load(MinecraftServer server, UUID uuid) {
        Path p = file(server, uuid);
        Progress pr = null;
        if (Files.exists(p)) {
            try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                pr = GSON.fromJson(r, Progress.class);
            } catch (Exception e) {
                SayIt.LOGGER.warn("Could not read progress {}: {}", p, e.getMessage());
            }
        }
        if (pr == null) {
            pr = new Progress();
        }
        if (pr.byLanguage == null) {
            pr.byLanguage = new LinkedHashMap<>();
        }
        if (pr.timestamps == null) {
            pr.timestamps = new LinkedHashMap<>();
        }
        if (pr.reps == null) {
            pr.reps = new LinkedHashMap<>();
        }
        if (pr.errors == null) {
            pr.errors = new LinkedHashMap<>();
        }
        if (pr.words != null && !pr.words.isEmpty()) {
            pr.byLanguage.computeIfAbsent("en", k -> new LinkedHashMap<>()).putAll(pr.words);
            if (pr.language == null) {
                pr.language = "en";
            }
            pr.words = null;
        }
        if (pr.language != null && !Languages.known(pr.language)) {
            pr.language = null;
        }
        return pr;
    }

    public void save(MinecraftServer server, UUID uuid) {
        Path p = file(server, uuid);
        try {
            Files.createDirectories(p.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
                GSON.toJson(this, w);
            }
        } catch (IOException e) {
            SayIt.LOGGER.warn("Could not save progress {}: {}", p, e.getMessage());
        }
    }
}