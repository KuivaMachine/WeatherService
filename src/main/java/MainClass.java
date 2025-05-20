import services.GeolocationService;
import services.RedisService;
import services.WeatherService;
import services.WeatherView;

import static spark.Spark.*;


public class MainClass {
    public static void main(String[] args) {
        port(9090);


        GeolocationService geolocation = new GeolocationService();
        WeatherService weatherService = new WeatherService(geolocation);
        WeatherView weatherView = new WeatherView(weatherService);

        weatherView.setupRoutes();


    }
}