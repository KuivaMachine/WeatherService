import services.GeolocationService;
import services.RedisService;
import services.WeatherService;
import services.WeatherView;

import static spark.Spark.*;


public class MainClass {
    public static void main(String[] args) {

        //Указываем порт, на котором будет работать сервис
        port(9090);

        //Инициализируем сервисы
        GeolocationService geolocation = new GeolocationService();
        WeatherService weatherService = new WeatherService(geolocation);
        WeatherView weatherView = new WeatherView(weatherService);

        //Запускаем сервис
        weatherView.setupRoutes();
    }
}