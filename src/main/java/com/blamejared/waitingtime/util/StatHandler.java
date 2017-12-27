package com.blamejared.waitingtime.util;

import com.blamejared.waitingtime.WaitingTime;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

public class StatHandler {

    private static Map<String, String> map = Maps.newHashMap();
    private static final File saveFile = new File("config" + File.separator +  "waitingtimestats.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Charset charset = StandardCharsets.UTF_8;
    private static final Logger log = LogManager.getLogger(WaitingTime.class);

    public static <E> E get(Stat<E> stat) {
        return stat.fromString.apply(map.computeIfAbsent(stat.key, s -> String.valueOf(stat.defaultVal)));
    }

    public static <E> void set(Stat<E> stat, E val) {
        map.put(stat.key, stat.toString.apply(val));
    }

    public static void loadStats() {
        if (saveFile.exists()) {
            try {
                String json = Files.toString(saveFile, charset);
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                map = gson.fromJson(json, type);
            } catch (JsonSyntaxException e) {
                log.error("Could not fromString stat file as valid json, deleting file", e);
                saveFile.delete();
            } catch (IOException e) {
                log.error("Failed to read stat file from disk, deleting file", e);
                saveFile.delete();
            } finally {
                // Can sometimes occur when the json file is malformed
                if (map == null) {
                    map = Maps.newHashMap();
                }
            }
        }
    }

    public static void saveStats() {
        new SaveThread(gson.toJson(map)).start();
    }

    private static class SaveThread extends Thread {
        private final String data;

        public SaveThread(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                Files.write(data, saveFile, charset);
            } catch (IOException e) {
                log.error("Failed to save stats to file!", e);
            }
        }
    }

    public static class Stat<E> {

        private String key;
        private Function<String, E> fromString;
        private Function<E, String> toString;
        private E defaultVal;

        public Stat(String key, Function<String, E> fromString, Function<E, String> toString, E defaultVal) {
            this.key = key;
            this.fromString = fromString;
            this.toString = toString;
            this.defaultVal = defaultVal;
        }

        public Stat(String key, Function<String, E> fromString, E defaultVal) {
            this(key, fromString, String::valueOf, defaultVal);
        }

        public Stat<E> saveDefault() {
            set(this, defaultVal);
            return this;
        }
    }
}
