package com.spicesfire.sayitenglish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_5218;
import net.minecraft.server.MinecraftServer;

public final class Progress {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public String language;
    public Map<String, Map<String, Integer>> byLanguage = new LinkedHashMap<>();
    public Map<String, Map<String, Long>> timestamps = new LinkedHashMap<>();
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