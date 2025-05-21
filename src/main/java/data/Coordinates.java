package data;

import java.util.Objects;

/**
 * Класс координат, хранит долготу и широту города
 * @param latitude широта
 * @param longitude долгота
 */
public record Coordinates(String latitude, String longitude) {

}
