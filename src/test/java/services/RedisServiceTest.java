package services;

import data.Coordinates;
import data.Temperature;
import data.WeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RedisServiceTest {

    private RedisService redisService;
    private final List<Temperature> mockTemperature = List.of(new Temperature("12:00", 15.5), new Temperature("13:00", 16.0));
    private final WeatherData mockData = new WeatherData("Moscow", new Coordinates("55.75222", "37.61556"), mockTemperature);

    @BeforeEach
    public void setUpRedis() {
        redisService = new RedisService();
    }

    @Test
    public void saveWeather_shouldReturnTrue_WhenSavedSuccessfully() {
        //Arrange
        //Act
        boolean response = redisService.saveWeather(mockData, 2);
        //Assert
        assertTrue(response);
    }

    @Test
    public void saveWeather_shouldReturnFalse_ForNegativeTtl() {
        //Arrange
        //Act
        boolean response = redisService.saveWeather(mockData, -900);
        //Assert
        assertFalse(response);
    }

    @Test
    public void getSavedWeather_shouldReturnSavedWeatherData_IfDataExists() {
        //Arrange
        redisService.saveWeather(mockData, 2);
        //Act
        WeatherData weatherData = redisService.getSavedWeather("Moscow");
        //Assert
        assertNotNull(weatherData);
        assertEquals(mockData.city(), weatherData.city());
        assertEquals(mockData.coordinates(), weatherData.coordinates());
        assertEquals(mockData.temperature(), weatherData.temperature());
    }

    @Test
    public void getSavedWeather_shouldReturnNull_IfDataNotExists() {
        //Arrange
        redisService.saveWeather(mockData, 2);
        //Act
        WeatherData weatherData = redisService.getSavedWeather("Berlin");
        //Assert
        assertNull(weatherData);
    }

    @Test
    public void savedData_shouldReturnNull_WhenTimePassed() throws InterruptedException {
        // Arrange
        List<Temperature> mockTemperature = List.of(new Temperature("12:00", 15.5), new Temperature("13:00", 16.0));
        WeatherData mockData = new WeatherData("London", new Coordinates("55.75222", "37.61556"), mockTemperature);
        redisService.saveWeather(mockData, 1);
        // Act
        Thread.sleep(1500);
        // Assert
        assertNull(redisService.getSavedWeather("London"));
    }


}
