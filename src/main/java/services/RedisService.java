package services;

import redis.clients.jedis.Jedis;
import com.google.gson.Gson;

public class RedisService {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    private final Jedis jedis;
    private final Gson gson;

    public RedisService () {
        this.jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        this.gson = new Gson();
    }

    /*public void cacheWeather(String city, WeatherData weatherData, int ttlSeconds) {
        String key = "weather:" + city.toLowerCase();
        String value = gson.toJson(weatherData);
        jedis.setex(key, ttlSeconds, value);
    }

    public WeatherData getWeather(String city) {
        String key = "weather:" + city.toLowerCase();
        String value = jedis.get(key);

        if (value == null) {
            return null;
        }

        return gson.fromJson(value, WeatherData.class);
    }*/
}