package eu.moondev.ccc;

import java.io.IOException;
import java.net.*;

public class Main {

    private static RedisConnector redisConnector;
    private static String hostname;
    public static void main(String[] args) {
        hostname = args[0];
        System.out.println("Starting with " + hostname);
        redisConnector = new RedisConnector(args[1]);

        redisConnector.listen("ping", message -> {
            String ipAddress = message.split("#")[0].split("=")[1];
            String id = message.split("#")[1];
            try {
                redisConnector.addToMap(message.split("#")[1], "host", ipAddress);
                InetAddress inetAddress = InetAddress.getByName(ipAddress);
                long startTime = System.currentTimeMillis();

                if (inetAddress.isReachable(5000)) {
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;
                    redisConnector.addToMap(id, hostname, String.valueOf(Float.parseFloat(String.valueOf(responseTime)) / 1000));
                } else {
                    redisConnector.addToMap(id, hostname, "Time out");
                }
            } catch (UnknownHostException e) {
                redisConnector.addToMap(id, hostname, "Unknown host");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        redisConnector.listen("tcp", message -> {
            System.out.println(message);
            String host = message.split("#")[0].split("=")[1];
            String id = message.split("#")[1];
            int port = 80;
            if (host.contains(":")) {
                port = Integer.parseInt(host.split(":")[1]);
                host = host.split(":")[0];
            }
            redisConnector.addToMap(id, "host", host + ":" + port);

            try {
                long startTime = System.currentTimeMillis();
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 5000);
                long endTime = System.currentTimeMillis();
                long responseTime = endTime - startTime;
                redisConnector.addToMap(id, hostname, String.valueOf(Float.parseFloat(String.valueOf(responseTime)) / 1000));
                socket.close();
            } catch (UnknownHostException e) {
                redisConnector.addToMap(id, hostname, "Unknown host");
            } catch (IOException e) {
                redisConnector.addToMap(id, hostname, "Time out");
            }
        });


        redisConnector.listen("http", message -> {
            String id = message.split("#")[1];
            try {
                long startTime = System.currentTimeMillis();
                URL url = new URL(message.split("#")[0].split("=")[1]);
                redisConnector.addToMap(message.split("#")[1], "host", String.valueOf(url));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                try {
                    int responseCode = connection.getResponseCode();
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;
                    redisConnector.addToMap(id, hostname, Float.parseFloat(String.valueOf(responseTime)) / 1000 + ";" + responseCode);
                } catch (SocketTimeoutException e) {
                    redisConnector.addToMap(id, hostname, "Time out");
                } finally {
                    connection.disconnect();
                }
            } catch (UnknownHostException e) {
                redisConnector.addToMap(id, hostname, "Unknown host");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
