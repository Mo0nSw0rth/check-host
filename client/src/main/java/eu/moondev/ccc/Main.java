package eu.moondev.ccc;

import java.io.IOException;
import java.net.*;

public class Main {

    private static RedisConnector redisConnector;
    private static String hostname;

    public static void main(String[] args) {
        // Nastavení hostname a inicializace RedisConnectoru
        hostname = args[0];
        System.out.println("Starting with " + hostname);
        redisConnector = new RedisConnector(args[1]);

        // Metoda pro ping check
        redisConnector.listen("ping", message -> {
            // Získání IP adresy a ID zprávy ze zprávy
            String ipAddress = message.split("#")[0].split("=")[1];
            String id = message.split("#")[1];

            try {
                // Přidání informací o hostovi do Redisu podle ID
                redisConnector.addToMap(message.split("#")[1], "host", ipAddress);
                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                long startTime = System.currentTimeMillis();

                if (inetAddress.isReachable(5000)) {
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;

                    // Přidání doby odezvy do Redisu podle ID
                    redisConnector.addToMap(id, hostname, String.valueOf(Float.parseFloat(String.valueOf(responseTime)) / 1000));
                } else {
                    // Přidání zprávy s errorem do Redisu podle ID
                    redisConnector.addToMap(id, hostname, "Time out");
                }
            } catch (UnknownHostException e) {
                // Přidání zprávy s errorem do Redisu podle ID
                redisConnector.addToMap(id, hostname, "Unknown host");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Metoda pro tcp check
        redisConnector.listen("tcp", message -> {
            System.out.println(message);
            // Získání hosta, ID a případně portu ze zprávy
            String host = message.split("#")[0].split("=")[1];
            String id = message.split("#")[1];
            int port = 80;

            // Pokud je port specifikován, získá se a odstraní z hosta
            if (host.contains(":")) {
                port = Integer.parseInt(host.split(":")[1]);
                host = host.split(":")[0];
            }

            // Přidání informací o hostovi do Redisu podle ID
            redisConnector.addToMap(id, "host", host + ":" + port);

            try {
                long startTime = System.currentTimeMillis();
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 5000);
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;

                // Přidání doby odezvy do Redisu podle ID
                redisConnector.addToMap(id, hostname, String.valueOf(Float.parseFloat(String.valueOf(responseTime)) / 1000));
                socket.close();
            } catch (UnknownHostException e) {
                // Přidání zprávy s errorem do Redisu podle ID
                redisConnector.addToMap(id, hostname, "Unknown host");
            } catch (IOException e) {
                // Přidání zprávy s errorem do Redisu podle ID
                redisConnector.addToMap(id, hostname, "Time out");
            }
        });

        // Metoda pro http check
        redisConnector.listen("http", message -> {
            String id = message.split("#")[1];

            try {
                long startTime = System.currentTimeMillis();
                URL url = new URL(message.split("#")[0].split("=")[1]);
                // Přidání URL Redisu podle ID
                redisConnector.addToMap(message.split("#")[1], "host", String.valueOf(url));
                // Vytvoření HTTP requestu
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                try {
                    int responseCode = connection.getResponseCode();
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;

                    // Přidání doby odezvy a HTTP kódu do Redisu podle ID
                    redisConnector.addToMap(id, hostname, Float.parseFloat(String.valueOf(responseTime)) / 1000 + ";" + responseCode);
                } catch (SocketTimeoutException e) {
                    // Přidání zprávy s errorem do Redisu podle ID
                    redisConnector.addToMap(id, hostname, "Time out");
                } finally {
                    connection.disconnect();
                }
            } catch (UnknownHostException e) {
                // Přidání zprávy s errorem do Redisu podle ID
                redisConnector.addToMap(id, hostname, "Unknown host");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
