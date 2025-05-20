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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.Layer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";
    private static final int CACHE_TTL_SECONDS = 900; // 15 minutes
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

    public WeatherData getWeather(String city) {
        // Try to get from cache first
//        WeatherData cachedData = redisCache.getWeather(city);
//        if (cachedData != null) {
//            return cachedData;
//        }

        // Get coordinates
        Coordinates coordinates = geolocation.getCoordinates(city);
        if (coordinates == null) {
            logger.error("City not found");
        }

        // Get weather data
        String url = String.format("%s?latitude=%s&longitude=%s&hourly=temperature_2m",
                WEATHER_URL, coordinates.latitude(), coordinates.longitude());
        Request request = new Request.Builder().url(url).build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Weather API request failed");
            }

            String responseBody = response.body().string();
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            JsonObject hourly = json.getAsJsonObject("hourly");

            JsonArray timeArray = hourly.getAsJsonArray("time");
            JsonArray tempArray = hourly.getAsJsonArray("temperature_2m");

            ArrayList<Temperature> hourlyTemps = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            for (int i = 0; i < timeArray.size(); i++) {
                String timeStr = timeArray.get(i).getAsString();
                LocalDateTime time = LocalDateTime.parse(timeStr, formatter);
                double temp = tempArray.get(i).getAsDouble();

                hourlyTemps.add(new Temperature(time.format(timeFormatter), temp));
            }


//            redisCache.cacheWeather(city, weatherData, CACHE_TTL_SECONDS);
            return new WeatherData(city, coordinates, hourlyTemps);

        } catch (Exception exception) {
            logger.error("Error getting weather data", exception);
        }
        return null;
    }

}