package eu.moondev.cc;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

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

    public void addToMap(String mapName, String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(mapName, key, value);
        }
    }

    public Map<String, String> getMap(String mapName) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(mapName);
        }
    }

    public String getFromMap(String mapName, String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(mapName, key);
        }
    }

}
