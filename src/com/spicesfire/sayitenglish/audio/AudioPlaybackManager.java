package com.spicesfire.sayitenglish.audio;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class AudioPlaybackManager {

    public interface WinMM extends Library {
        WinMM INSTANCE = init();

        static WinMM init() {
            try {
                return Native.load("winmm", WinMM.class, W32APIOptions.UNICODE_OPTIONS);
            } catch (Throwable t) {
                System.err.println("[SayIt-Audio] Failed to load winmm.dll via JNA: " + t.getMessage());
                return null;
            }
        }

        long mciSendStringW(String lpstrCommand, char[] lpstrReturnString, int uReturnLength, Pointer hwndCallback);
    }

    private static final String ALIAS = "sayit_audio_voice";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "sayit-audio-playback");
        t.setDaemon(true);
        return t;
    });

    private static volatile boolean isPlaying = false;
    private static volatile long cooldownUntilMs = 0L;
    private static volatile Future<?> currentTask = null;

    private AudioPlaybackManager() {
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static boolean isListeningLocked() {
        return isPlaying || System.currentTimeMillis() < cooldownUntilMs;
    }

    public static synchronized void stop() {
        isPlaying = false;
        cooldownUntilMs = System.currentTimeMillis() + 400L;
        if (WinMM.INSTANCE != null) {
            try {
                WinMM.INSTANCE.mciSendStringW("stop " + ALIAS, null, 0, null);
                WinMM.INSTANCE.mciSendStringW("close " + ALIAS, null, 0, null);
            } catch (Throwable ignored) {
            }
        }
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
    }

    public static void play(String text) {
        play(text, null);
    }

    public static synchronized void play(String text, Runnable onComplete) {
        if (text == null || text.trim().isEmpty()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        final String targetText = text.trim();
        stop();

        currentTask = EXECUTOR.submit(() -> {
            isPlaying = true;
            try {
                File audioFile = resolveAudioFile(targetText);
                if (audioFile == null || !audioFile.exists() || audioFile.length() == 0) {
                    String fallbackText = extractFallback(targetText);
                    if (fallbackText != null && !fallbackText.equals(targetText)) {
                        audioFile = resolveAudioFile(fallbackText);
                    }
                }

                if (audioFile == null || !audioFile.exists() || audioFile.length() == 0) {
                    System.out.println("[SayIt-Audio] Audio file unavailable for '" + targetText + "' (offline & uncached)");
                    return;
                }

                if (WinMM.INSTANCE == null) {
                    System.err.println("[SayIt-Audio] WinMM library unavailable, skipping audio playback.");
                    return;
                }

                String absPath = audioFile.getAbsolutePath();
                WinMM.INSTANCE.mciSendStringW("close " + ALIAS, null, 0, null);
                long openRes = WinMM.INSTANCE.mciSendStringW("open \"" + absPath + "\" type mpegvideo alias " + ALIAS, null, 0, null);
                if (openRes != 0) {
                    System.err.println("[SayIt-Audio] mciSendStringW open error " + openRes + " for " + absPath);
                    return;
                }

                WinMM.INSTANCE.mciSendStringW("play " + ALIAS + " wait", null, 0, null);
                WinMM.INSTANCE.mciSendStringW("close " + ALIAS, null, 0, null);
            } catch (Throwable t) {
                System.err.println("[SayIt-Audio] Playback exception: " + t.getMessage());
            } finally {
                isPlaying = false;
                cooldownUntilMs = System.currentTimeMillis() + 750L;
                if (onComplete != null) {
                    try {
                        onComplete.run();
                    } catch (Throwable t) {
                        System.err.println("[SayIt-Audio] onComplete callback exception: " + t.getMessage());
                    }
                }
            }
        });
    }

    public static File resolveAudioFile(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String clean = text.trim();

        File cached = findInCache(clean);
        if (cached != null && cached.exists() && cached.length() > 0) {
            return cached;
        }

        File fetched = fetchFromGoogleTTS(clean);
        if (fetched != null && fetched.exists() && fetched.length() > 0) {
            return fetched;
        }

        return null;
    }

    public static File findInCache(String text) {
        String sha = sha256(text);
        String md5 = md5(text);
        List<File> searchDirs = getCandidateCacheDirs();

        for (File dir : searchDirs) {
            if (!dir.exists() || !dir.isDirectory()) continue;

            File fSha = new File(dir, sha + ".mp3");
            if (fSha.exists() && fSha.length() > 0) return fSha;

            File fMd5 = new File(dir, md5 + ".mp3");
            if (fMd5.exists() && fMd5.length() > 0) return fMd5;
        }
        return null;
    }

    public static List<File> getCandidateCacheDirs() {
        List<File> list = new ArrayList<>();

        try {
            if (com.spicesfire.sayitenglish.platform.Platform.I != null) {
                Path cfg = com.spicesfire.sayitenglish.platform.Platform.I.configDir();
                if (cfg != null) {
                    list.add(cfg.resolve("sayitenglish").resolve("audio_cache").toFile());
                    list.add(cfg.resolve("sayit_audio_cache").toFile());
                    if (cfg.getParent() != null) {
                        list.add(cfg.getParent().resolve("sayit_audio_cache").toFile());
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        list.add(new File("Instancia_Cliente/sayit_audio_cache"));
        list.add(new File("Instancia_Cliente/config/sayitenglish/audio_cache"));
        list.add(new File("config/sayitenglish/audio_cache"));
        list.add(new File("sayit_audio_cache"));
        try {
            String userDir = System.getProperty("user.dir");
            if (userDir != null && !userDir.isEmpty()) {
                File base = new File(userDir);
                list.add(new File(base, "Instancia_Cliente/sayit_audio_cache"));
                list.add(new File(base, "Instancia_Cliente/config/sayitenglish/audio_cache"));
                list.add(new File(base, "sayit_audio_cache"));
                list.add(new File(base, "config/sayitenglish/audio_cache"));
            }
        } catch (Throwable ignored) {
        }

        List<File> existing = new ArrayList<>();
        for (File d : list) {
            if (d != null && d.exists() && d.isDirectory() && !existing.contains(d)) {
                existing.add(d);
            }
        }
        return existing;
    }

    public static File getPrimaryCacheDir() {
        List<File> candidates = getCandidateCacheDirs();
        for (File d : candidates) {
            if (d.canWrite()) return d;
        }
        File def = new File("Instancia_Cliente/sayit_audio_cache");
        if (!def.exists()) def.mkdirs();
        return def;
    }

    private static File fetchFromGoogleTTS(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translate.google.com/translate_tts?ie=UTF-8&q=" + encoded + "&tl=ru&client=tw-ob";

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(2))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .GET()
                .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200 && response.body() != null && response.body().length > 0) {
                File dir = getPrimaryCacheDir();
                if (!dir.exists()) dir.mkdirs();

                String sha = sha256(text);
                String md5 = md5(text);
                File outFile = new File(dir, sha + ".mp3");
                Files.write(outFile.toPath(), response.body());

                File md5File = new File(dir, md5 + ".mp3");
                if (!md5File.exists()) {
                    Files.write(md5File.toPath(), response.body());
                }
                return outFile;
            }
        } catch (Throwable t) {
            System.out.println("[SayIt-Audio] Google TTS request failed for '" + text + "': " + t.getMessage());
        }
        return null;
    }

    private static String extractFallback(String targetText) {
        if (targetText == null) return null;
        if (targetText.startsWith("Мне нужно: ")) {
            String sub = targetText.substring("Мне нужно: ".length()).trim();
            if (sub.endsWith("!")) {
                sub = sub.substring(0, sub.length() - 1).trim();
            }
            return sub;
        }
        return null;
    }

    public static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(text.hashCode());
        }
    }

    public static String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(text.hashCode());
        }
    }
}
