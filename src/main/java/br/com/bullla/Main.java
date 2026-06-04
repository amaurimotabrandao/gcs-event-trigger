package br.com.bullla;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Main {

    private static String webhookUrl;
    private static int port;

    public static void main(String[] args) throws Exception {
        loadConfig();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/event", Main::handleEvent);
        server.start();

        System.out.println("Server started on port " + port);
    }

    private static void loadConfig() throws Exception {
        Properties props = new Properties();
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(is);
        }
        webhookUrl = props.getProperty("webhook.url");
        port = Integer.parseInt(props.getProperty("server.port"));
    }

    private static void handleEvent(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("Event received: " + body);

        // responde 200 por enquanto para ver o payload
        exchange.sendResponseHeaders(200, -1);
        exchange.close();
    }

    private static void sendPost(String bucket, String file) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("bucket", bucket);
        payload.addProperty("file", file);

        HttpURLConnection conn = (HttpURLConnection) new URL(webhookUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("POST sent, response: " + conn.getResponseCode());
        conn.disconnect();
    }
}