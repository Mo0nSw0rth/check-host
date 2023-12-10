package eu.moondev.ccc;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

public class RedisConnector {

    private final JedisPool jedisPool;

    public RedisConnector(String redisUrl) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), redisUrl);
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public void listen(String channel, Consumer<String> messageConsumer) {
        new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSubAdapter(messageConsumer), channel);
            }
        }).start();
    }

    public void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public void delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public void addToList(String listName, String content) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.rpush(listName, content);
        }
    }

    public boolean isInList(String listName, String content) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.lrange(listName, 0, -1).contains(content);
        }
    }

    public long itemsInList(String listName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.llen(listName);
        }
    }

    public void removeFromList(String listName, String content) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lrem(listName, 0, content);
        }
    }

    public void addToSet(String setName, String content) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sadd(setName, content);
        }
    }

    public boolean isInSet(String setName, String content) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(setName).contains(content);
        }
    }

    public long itemsInSet(String setName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scard(setName);
        }
    }

    public void removeFromSet(String setName, String content) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.srem(setName, content);
        }
    }

    public void addToMap(String mapName, String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(mapName, key, value);
        }
    }

    public String getFromMap(String mapName, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(mapName, key);
        }
    }

    public void removeFromMap(String mapName, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(mapName, key);
        }
    }

    public boolean isInMap(String mapName, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists(mapName, key);
        }
    }

    public void handleException(Exception e, String s) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish("errors", e.getClass().getName() + ": " + e.getMessage() + " [" + s + "]");
            jedis.hset("errors", String.valueOf(System.currentTimeMillis()), e.getClass().getName() + ": " + e.getMessage() + " [" + s + "]");
        }
    }

    private static class JedisPubSubAdapter extends JedisPubSub {

        private final Consumer<String> messageConsumer;

        public JedisPubSubAdapter(Consumer<String> messageConsumer) {
            this.messageConsumer = messageConsumer;
        }

        @Override
        public void onMessage(String channel, String message) {
            messageConsumer.accept(message);
        }
    }
}
