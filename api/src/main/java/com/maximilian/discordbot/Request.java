package com.maximilian.discordbot;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class Request {
    private String url;
    private String body;
    private Map<String, String> headers;
    private Map<String, String> cookies;

    @Default
    private RequestMethod method = RequestMethod.POST;

    @Default
    private String contentType = "application/json";

    @Default
    private String accept = "application/json";

    
    public String sendRequest() throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(this.method.name());
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", this.contentType);
        connection.setRequestProperty("Accept", this.accept);

        if (headers != null)
            headers.forEach(connection::setRequestProperty);

        if (cookies != null) {
            StringBuilder cookieHeader = new StringBuilder();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                cookieHeader.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            connection.setRequestProperty("Cookie", cookieHeader.toString());
        }

        if (body != null)
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = this.body.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }

        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String responseLine = null;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        Map<String, List<String>> responseHeaders = connection.getHeaderFields();
        if (responseHeaders.containsKey("Set-Cookie")) {
            List<String> cookies = responseHeaders.get("Set-Cookie");
            for (String cookie : cookies) {
                String[] parts = cookie.split(";\\s*");
                for (String part : parts) {
                    String[] keyValue = part.split("=", 2);
                    if (keyValue.length == 2) {
                        this.cookies.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }

        connection.disconnect();
        return response.toString();
    }

    public enum RequestMethod {
        POST,
        GET,
        PUT,
        DELETE
    }

}
