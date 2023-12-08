package com.maximilian.discordbot;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SseClient {

    private final String url;
    private final Consumer<SseResponse> onMessage;
    private final Map<String, String> cookies;
    private boolean keepRunning = true;

    private static final int RECONNECT_DELAY = 5000;
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int READ_TIMEOUT = 10000;

    public SseClient(String url, Map<String, String> cookies, Consumer<SseResponse> onMessage) {
        this.url = url;
        this.cookies = cookies;
        this.onMessage = onMessage;
    }

    public void connect() {
        new Thread(() -> {
            while (keepRunning) {
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setConnectTimeout(CONNECTION_TIMEOUT);
                    connection.setReadTimeout(READ_TIMEOUT);
                    connection.setRequestProperty("Connection", "keep-alive");

                    if (cookies != null) {
                        connection.setRequestProperty("Cookie", formatCookies());
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        String line;
                        while (keepRunning && (line = reader.readLine()) != null) {
                            if (line.startsWith("data:")) {
                                String data = line.substring(5);
                                onMessage.accept(new Gson().fromJson(data, SseResponse.class));
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.err.println("Connection timed out. Attempting to reconnect...");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                // Attempt to reconnect after a delay
                if (keepRunning) {
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }).start();
    }

    public void stop() {
        keepRunning = false;
    }

    private String formatCookies() {
        return cookies.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue() + "; Path=/; HttpOnly")
                .collect(Collectors.joining("; "));
    }

    @Data
    @Builder
    @Getter
    public static class SseResponse {
        private TargetType target;
        private Action action;
        private Map<String, Object> data;
    }

    public enum TargetType {
        MUSIC,
        AUTO_CHANNEL,
    }

    public enum Action {
        TOGGLE_PAUSE,
        SKIP,
        RESUME,
        ADD_TO_QUEUE,
        REMOVE_FROM_QUEUE,
        FORCE_QUEUE_ITEM,
        CLEAR_QUEUE,
        SHUFFLE,
        CHANGE_VOLUME,
        CHANGE_CHANNEL,
        CHANGE_POSITION,
        STOP
    }
}