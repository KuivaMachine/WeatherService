package data;

import java.util.ArrayList;
import java.util.List;

/**
 * Объектная модель данных о температуре
 * @param city город (String, например "Mexico")
 * @param coordinates координаты города (долгота и широта) Coordinates.class
 * @param temperature температура на сутки
 */
public record WeatherData (String city, Coordinates coordinates, List<Temperature> temperature){
}
