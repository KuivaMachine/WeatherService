package data;

/**
 * Класс температуры, представляет объект пары "время"-"величина"
 * @param time время
 * @param value величина температуры в градусах Цельсия
 */
public record Temperature(String time, double value) {
}

