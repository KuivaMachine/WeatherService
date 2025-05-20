package services;

import data.Coordinates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeolocationServiceTest {

    @Test
    public void getCoordinates_shouldReturnRightCoordinates_whenRightResponse() {
        // Arrange
        GeolocationService service = new GeolocationService();

        // Act
        Coordinates coordinates = service.getCoordinates("Moscow");

        // Assert
        assertNotNull(coordinates);
        assertEquals(55.75222, coordinates.latitude());
        assertEquals(37.61556, coordinates.longitude());
    }


    @Test
    public void getCoordinates_shouldReturnNull_whenWrongResponse() {
        //Arrange
        GeolocationService service = new GeolocationService();
        //Act
        Coordinates coordinates = service.getCoordinates("Москва");
        //Assert
        assertNull(coordinates);
    }
}
