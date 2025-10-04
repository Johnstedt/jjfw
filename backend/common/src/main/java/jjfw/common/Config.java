package jjfw.common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {

    private static JSONObject config = null;

    public static String get(String s) {
        return config.getString(s);
    }

    public static int getNum(String s) {
        return config.getInt(s);
    }

    public static void setSitePath(String s) {
        try {
            config = parseJSONFile(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getLogLevel(String s) {
        return config.getJSONObject("log").getInt(s);
    }

    private static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        return new JSONObject(content);
    }

}