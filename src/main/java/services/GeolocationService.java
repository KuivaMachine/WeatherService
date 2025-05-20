package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import data.Coordinates;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GeolocationService {
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private final OkHttpClient okHttpClient;
    private final Gson gson;
    private final static Logger logger = LoggerFactory.getLogger(GeolocationService.class);

    public GeolocationService() {
        this.okHttpClient = new OkHttpClient();
        this.gson = new Gson();
    }

    /**
     * Получает координаты города по его названию
     * @param city название города (например: "Moscow")
     * @return объект Coordinates(double latitude, double longitude) с широтой и долготой или null при ошибке
     */
    public Coordinates getCoordinates(String city) {
        String url = String.format("%s?name=%s&count=1", GEOCODING_URL, city);
        Request request = new Request.Builder().url(url).build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                logger.error("При запросе произошла ошибка: {}", response.code());
                return null;
            }

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            JsonArray results = json.getAsJsonArray("results");
            if (results == null) {
                logger.error(String.format("При запросе координат города \"%s\" сервер вернул пустой результат", city));
                return null;
            }
            JsonObject firstResult = results.get(0).getAsJsonObject();

            String latitude = firstResult.get("latitude").getAsString().replace(",",".");
            String longitude = firstResult.get("longitude").getAsString().replace(",",".");
            return new Coordinates(latitude, longitude);

        } catch (Exception e) {
            logger.error(String.format("При запросе координат города \"%s\" произошла ошибка - %s", city, e.getMessage()));
        }
        return null;
    }
}