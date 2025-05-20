package data;

import java.util.ArrayList;

public record WeatherData (String city, Coordinates coordinates, ArrayList<Temperature> temperature){
}
