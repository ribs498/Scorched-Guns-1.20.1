package top.ribs.scguns.entity.config;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigLoader {

    // Generic method to load any type of config
    public static <T> T loadConfig(InputStream inputStream, Class<T> configClass) throws IOException {
        Gson gson = new Gson();
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            return gson.fromJson(reader, configClass);
        }
    }
    public static CogMinionConfig loadCogMinionConfig(InputStream inputStream) throws IOException {
        return loadConfig(inputStream, CogMinionConfig.class);
    }
}



