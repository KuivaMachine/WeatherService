import services.GeolocationService;
import services.WeatherService;
import services.WeatherViewer;

import static spark.Spark.*;


public class MainClass {
    public static void main(String[] args) {

        //Указываем порт, на котором будет работать сервис
        port(9090);

        //ициализируем сервисы
        GeolocationService geolocation = new GeolocationService();
        WeatherService weatherService = new WeatherService(geolocation);
        WeatherViewer weatherViewer = new WeatherViewer(weatherService);

        //Запускаем сервис
        weatherViewer.setupRoutes();
    }
}