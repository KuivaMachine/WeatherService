package services;

import data.Coordinates;
import data.Temperature;
import data.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class WeatherServiceTest {
    private final GeolocationService mockGeolocationService = new GeolocationService();
    private final RedisService mockRedisService = new RedisService();
    private WeatherService weatherService;

    @BeforeEach
    public void setUp() {
        weatherService = new WeatherService(mockGeolocationService);
    }

    @Test
    public void getWeather_ShouldReturnWeather_WhenDataNotYetExists() {
        //Arrange
        WeatherData mockData = new WeatherData("Paris", new Coordinates("48.85341","2.3488"), new ArrayList<Temperature>());
        //Act
        WeatherData weatherData = weatherService.getWeather("Paris");
        //Assert
        assertNotNull(weatherData);
        assertEquals(mockData.city(), weatherData.city());
        assertEquals(mockData.coordinates(), weatherData.coordinates());
    }

    @Test
    public void getWeather_ShouldReturnWeatherFromCash_WhenDataExists() {
        //Arrange
        List<Temperature> mockTemperature = List.of(new Temperature("12:00", 15.5), new Temperature("13:00", 16.0));
        WeatherData mockData = new WeatherData("Moscow", new Coordinates("55.75222", "37.61556"), mockTemperature);
        mockRedisService.saveWeather(mockData,2);
        //Act
        WeatherData weatherData = weatherService.getWeather("Moscow");
        //Assert
        assertEquals(mockData.city(), weatherData.city());
        assertEquals(mockData.coordinates(), weatherData.coordinates());
        assertEquals(mockData.temperature(), weatherData.temperature());
    }

    @Test
    public void getWeather_ShouldReturnNull_WhenWrongCity() {
        //Arrange
        //Act
        WeatherData weatherData = weatherService.getWeather("");
        //Assert
        assertNull(weatherData);
    }
}
