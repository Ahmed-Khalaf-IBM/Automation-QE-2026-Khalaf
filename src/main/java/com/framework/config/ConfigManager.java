package com.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * DESIGN PATTERN: Singleton (thread-safe, initialization-on-demand holder).
 * One place that reads config/{env}.properties. Any key can be overridden with -Dkey=value.
 */
public final class ConfigManager {

    private final Properties props = new Properties();

    private ConfigManager() {
        String env = System.getProperty("env", "qa");
        if (env.isBlank() || env.startsWith("${")) env = "qa";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config/" + env + ".properties")) {
            if (in == null) throw new IllegalStateException("Missing config/" + env + ".properties");
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load config", e);
        }
    }

    private static final class Holder {
        private static final ConfigManager INSTANCE = new ConfigManager();
    }

    public static ConfigManager get() {
        return Holder.INSTANCE;
    }

    public String get(String key) {
        String v = System.getProperty(key, props.getProperty(key));
        if (v == null) throw new IllegalArgumentException("Config key not found: " + key);
        return v.trim();
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public boolean getBool(String key) {
        return Boolean.parseBoolean(get(key));
    }
}
