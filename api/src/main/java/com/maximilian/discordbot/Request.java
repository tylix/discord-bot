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
import java.util.logging.Level;
import java.util.logging.Logger;

@Builder
@Getter
public class Request {
    private static final Logger LOGGER = Logger.getLogger(Request.class.getName());

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


    public String sendRequest() {
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();

        try {
            URL urlObj = new URL(this.url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setRequestMethod(this.method.name());
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", this.contentType);
            connection.setRequestProperty("Accept", this.accept);

            setRequestProperties(connection);

            writeRequestBody(connection);

            readResponse(connection, response);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Connection error: ", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response.toString();
    }

    private void setRequestProperties(HttpURLConnection connection) {
        if (headers != null) {
            headers.forEach(connection::setRequestProperty);
        }

        if (cookies != null) {
            StringBuilder cookieHeader = new StringBuilder();
            cookies.forEach((key, value) -> cookieHeader.append(key).append("=").append(value).append("; "));
            connection.setRequestProperty("Cookie", cookieHeader.toString());
        }
    }

    private void writeRequestBody(HttpURLConnection connection) throws IOException {
        if (body != null) {
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = this.body.getBytes(StandardCharsets.UTF_8);
                outputStream.write(input, 0, input.length);
            }
        }
    }

    private void readResponse(HttpURLConnection connection, StringBuilder response) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
    }

    public enum RequestMethod {
        POST, GET, PUT, DELETE
    }

}
