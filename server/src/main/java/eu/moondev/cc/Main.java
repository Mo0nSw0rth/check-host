package eu.moondev.cc;

import com.google.gson.*;
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;

public class Main {

    private static RedisConnector redisConnector;

    public static void main(String[] args) throws Exception {
        // Inicializace RedisConnectoru
        redisConnector = new RedisConnector(args[0]);
        // Nastavení portu pro HTTP server
        int port = 80;

        // Vytvoření a konfigurace HTTP serveru
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/check-ping", new PingHandler());
        server.createContext("/check-http", new WebHandler());
        server.createContext("/check-tcp", new TcpHandler());
        server.createContext("/check-result", new ResultHandler());
        server.createContext("/nodes", new NodesHandler());
        server.setExecutor(null);

        server.start();
        System.out.println("Server started on port " + port);
    }

    // HTTP handler pro zpracování výsledků
    static class ResultHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Získání ID, typu a hosta
            String id = exchange.getRequestURI().getPath().split("/")[2];
            String type = redisConnector.getFromMap(id, "type");
            String host = redisConnector.getFromMap(id, "host");

            // Zpracování výsledků podle typu
            switch (type) {
                case "http":
                    JsonObject httpObject = new JsonObject();
                    httpObject.addProperty("type", type);
                    httpObject.addProperty("host", host);
                    JsonObject httpNodes = new JsonObject();
                    redisConnector.getMap(id).forEach((key, value) -> {
                        if (!key.equals("type") && !key.equals("host")) {
                            JsonObject node = new JsonObject();
                            if (value.split(";").length == 2) {
                                node.addProperty("response", value.split(";")[0]);
                                node.addProperty("code", value.split(";")[1]);
                            } else {
                                node.addProperty("response", value);
                            }
                            httpNodes.add(key, node);
                        }
                    });
                    httpObject.add("nodes", httpNodes);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    returnResponse(exchange, gson.toJson(httpObject));
                    break;

                case "tcp":
                    JsonObject tcpObject = new JsonObject();
                    tcpObject.addProperty("type", type);
                    tcpObject.addProperty("host", host);
                    JsonObject tcpNodes = new JsonObject();
                    redisConnector.getMap(id).forEach((key, value) -> {
                        if (!key.equals("type") && !key.equals("host")) {
                            JsonObject node = new JsonObject();
                            node.addProperty("response", value);
                            tcpNodes.add(key, node);
                        }
                    });
                    tcpObject.add("nodes", tcpNodes);
                    Gson tcpGson = new GsonBuilder().setPrettyPrinting().create();
                    returnResponse(exchange, tcpGson.toJson(tcpObject));
                    break;

                case "ping":
                    JsonObject pingObject = new JsonObject();
                    pingObject.addProperty("type", type);
                    pingObject.addProperty("host", host);
                    JsonObject pingNodes = new JsonObject();
                    for (Map.Entry<String, String> entry : redisConnector.getMap(id).entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (!key.equals("type") && !key.equals("host")) {
                            JsonObject node = new JsonObject();
                            node.addProperty("response", value);
                            pingNodes.add(key, node);
                        }
                    }
                    pingObject.add("nodes", pingNodes);
                    Gson pingGson = new GsonBuilder().setPrettyPrinting().create();
                    returnResponse(exchange, pingGson.toJson(pingObject));
                    break;
            }
        }
    }

    // HTTP handler pro odeslání Pingu na checkovací servery
    static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = exchange.getRequestURI().getQuery();
            String id = generateRandomString();
            redisConnector.addToMap(id, "type", "ping");
            redisConnector.publish("ping", response + "#" + id);
            JsonObject object = new JsonObject();
            object.addProperty("id", id);
            object.addProperty("url", "https://cc.moondev.eu/check-result/" + id);
            returnResponse(exchange, new GsonBuilder().setPrettyPrinting().create().toJson(object));
        }
    }

    // HTTP handler pro odeslání HTTP na checkovací servery
    static class WebHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = exchange.getRequestURI().getQuery();
            String id = generateRandomString();
            redisConnector.addToMap(id, "type", "http");
            redisConnector.publish("http", response + "#" + id);
            JsonObject object = new JsonObject();
            object.addProperty("id", id);
            object.addProperty("url", "https://cc.moondev.eu/check-result/" + id);
            returnResponse(exchange, new GsonBuilder().setPrettyPrinting().create().toJson(object));
        }
    }

    // HTTP handler pro odeslání TCP na checkovací servery
    static class TcpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = exchange.getRequestURI().getQuery();
            String id = generateRandomString();
            redisConnector.addToMap(id, "type", "tcp");
            redisConnector.publish("tcp", response + "#" + id);
            JsonObject object = new JsonObject();
            object.addProperty("id", id);
            object.addProperty("url", "https://cc.moondev.eu/check-result/" + id);
            returnResponse(exchange, new GsonBuilder().setPrettyPrinting().create().toJson(object));
        }
    }

    // HTTP handler pro získání lokací o checkovacích serverech
    static class NodesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"nodes\":{\"in.node.moondev.eu\":{\"ip\":\"194.195.119.201\",\"location\":[\"India\",\"Mumbai\"]},\"jp.node.moondev.eu\":{\"ip\":\"172.233.85.124\",\"location\":[\"Japan\",\"Osaka\"]},\"se.node.moondev.eu\":{\"ip\":\"172.232.159.199\",\"location\":[\"Sweden\",\"Stockholm\"]},\"tx.node.moondev.eu\":{\"ip\":\"45.79.51.1239\",\"location\":[\"United States\",\"Dallas\"]}}}\n";
            returnResponse(exchange, response);
        }
    }

    // Metoda pro odeslání odpovědi
    private static void returnResponse(HttpExchange exchange, String response) throws IOException {
        // Nastavení JSON a CORS headers odpovědi a odeslání
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    // Generování náhodného UUID pro přidání do Redisu
    private static String generateRandomString() {
        String randomUUID = UUID.randomUUID().toString();
        return randomUUID.replaceAll("-", "").substring(0, 10);
    }

}
