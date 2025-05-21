package services;

import com.google.gson.Gson;
import data.WeatherData;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisService {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private final Jedis jedis;
    private final Gson gson;

    public RedisService () {
        this.jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        this.gson = new Gson();
    }

    /**
     * Сохраняет данные о погоде (город, температуру) на указанный период
     * @param weatherData данные температуры WeatherData.class
     * @param ttlSeconds  период кэширования (int, секунды)
     * @return True, если успешно, иначе False
     */
    boolean saveWeather(WeatherData weatherData, int ttlSeconds) {
        String key = "weather:" + weatherData.city();
        String value = gson.toJson(weatherData);
        try {
            jedis.setex(key, ttlSeconds, value);
            return true;
        } catch (JedisDataException e) {
            return false;
        }
    }

    /**
     * Возвращает данные о погоде по указанному городу
     *
     * @param city название города (String, например, "Berlin")
     * @return данные температуры WeatherData.class, если успешно, иначе null
     */
    WeatherData getSavedWeather(String city) {
        String key = "weather:" + city;
        String value = jedis.get(key);
        if (value == null) {
            return null;
        }
        return gson.fromJson(value, WeatherData.class);
    }
}