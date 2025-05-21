package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import data.Coordinates;
import data.Temperature;
import data.WeatherData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Сервис для получения прогноза погоды по названию города.
 * Обеспечивает:
 * - Получение данных через Open-Meteo API
 * - Кэширование результатов в Redis
 * - Преобразование данных из Json в объектную модель (WeatherData.class)
 */
public class WeatherService {
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";
    private static final int CACHE_TTL_SECONDS = 900; // 15 минут
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final RedisService redis;
    private final GeolocationService geolocation;
    private final OkHttpClient httpClient;
    private final Gson gson;

    public WeatherService(GeolocationService geolocation) {
        this.redis = new RedisService();
        this.geolocation = geolocation;
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    /**
     * Получает прогноз температуры на сутки для указанного города
     * @param city название города (например: "Moscow")
     * @return объект WeatherData с прогнозом температуры или null, если город не найден или произошла ошибка
     */
    WeatherData getWeather(String city) {

        // Запрашиваем данные из кэша. Возвращаем, если успешно.
        WeatherData savedData = redis.getSavedWeather(city);
        if (savedData != null) {
            return savedData;
        }

        // Запрос координат города
        Coordinates coordinates = geolocation.getCoordinates(city);
        if (coordinates == null) {
            logger.error("City \"{}\" was not found", city);
            return null;
        }

        // Запрос температуры на сутки по городу
        String url = String.format("%s?latitude=%s&longitude=%s&hourly=temperature_2m",
                WEATHER_URL, coordinates.latitude(), coordinates.longitude());
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()||response.body()==null) {
                logger.error("During temperature request an error has occurred: {}", response.code());
                return null;
            }

            //Строим данные из ответа json с сервера
            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonObject hourly = json.getAsJsonObject("hourly");

            JsonArray timeArray = hourly.getAsJsonArray("time");
            JsonArray tempArray = hourly.getAsJsonArray("temperature_2m");

            ArrayList<Temperature> hourlyTemps = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            //Записываем данные в класс WeatherData
            for (int i = 0; i < timeArray.size(); i++) {
                String timeStr = timeArray.get(i).getAsString();
                LocalDateTime time = LocalDateTime.parse(timeStr, formatter);
                double temp = tempArray.get(i).getAsDouble();

                hourlyTemps.add(new Temperature(time.format(timeFormatter), temp));
            }
            WeatherData weatherData =  new WeatherData(city, coordinates, hourlyTemps);

            //Сохраняем данные о температуре в кэш на 15 минут
            if(redis.saveWeather(weatherData, CACHE_TTL_SECONDS)){
                logger.info("Temperature data by city \"{}\" was saved", city);
            }else {
                logger.info("During saving temperature data by \"{}\" city an error has occurred", city);

            }
            return weatherData;

        } catch (Exception exception) {
            logger.error("During request an error has occurred: - {}", exception.getMessage());
        }
        return null;
    }

}