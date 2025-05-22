package services;

import data.Temperature;
import data.WeatherData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.svg.SVGGraphics2D;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class WeatherViewer {
    private final WeatherService weatherService;

    public WeatherViewer(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public void setupRoutes() {
        Spark.get("/weather", ((request, response) -> getWeather(request, response)));
    }

    /**
     * Обрабатывает HTTP-запрос для получения информации о погоде в указанном городе.
     *
     * <p>Метод выполняет следующие действия:
     * <ol>
     *   <li>Извлекает название города из параметров запроса</li>
     *   <li>Получает данные о погоде через WeatherService</li>
     *   <li>Генерирует SVG-график температуры</li>
     *   <li>Загружает HTML-шаблон страницы</li>
     *   <li>Заполняет шаблон данными о погоде</li>
     *   <li>Возвращает сформированную HTML-страницу</li>
     * </ol>
     *
     * @param request HTTP-запрос, должен содержать параметр "city"
     * @param response HTTP-ответ, в который будет установлен тип содержимого
     * @return HTML-страница с информацией о погоде или null в случае ошибки
     */
    String getWeather(Request request, Response response) {
        System.out.println(request.body());
        //Получаем город из запроса и проверяем его
        String city = request.queryParams("city");
        if (city == null || city.isEmpty()) {
            response.status(404);
            return null;
        }

        //Запрашиваем погоду по городу
        WeatherData weatherData;
        try {
            weatherData = weatherService.getWeather(city);
        } catch (Exception e) {
            response.status(500);
            return null;
        }

        //Строим SVG график
        String svg = generateTemperatureChartSVG(weatherData);

        //Читаем HTML страницу из resources
        String htmlTemplate;
        try {
            htmlTemplate = Files.readString(
                    Paths.get("src/main/resources/weather_page.html"),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            return null;
        }

        //Вставляем в templates название города и график
        String html = htmlTemplate
                .replace("{{city}}", city)
                .replace("{{temperature}}", getCurrentTemperature(weatherData.temperature()))
                .replace("{{chart}}", svg);

        //Возвращаем ответ в виде HTML страницы
        response.type("text/html");
        return html;
    }

    /**
     * Принимает на вход список значений температуры ко времени и находит ближайшее по времени значение
     * @param temperatureList лист значений
     * @return температуру на текущий час, String
     */
    private String getCurrentTemperature(List<Temperature> temperatureList) {
        LocalTime timeNow = LocalDateTime.now().toLocalTime();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        double closestTemp = 0;
        long minDiff = Long.MAX_VALUE;
        for (Temperature temperature : temperatureList) {
            // Парсим время из температуры
            LocalTime tempTime = LocalTime.parse(temperature.time(), timeFormatter);
            // Вычисляем разницу во минутах между текущим временем и временем в данных
            long diff = Math.abs(ChronoUnit.MINUTES.between(timeNow, tempTime));
            // Если нашли более близкое по времени значение
            if (diff < minDiff) {
                minDiff = diff;
                closestTemp = temperature.value();
                System.out.println(String.format("NOW - %s, TIME - %s, TEMP - %s", timeNow, tempTime, closestTemp));
            }
        }
        return String.format("%.1f", closestTemp);
    }

    /**
     * Генерирует SVG-график температуры на основе данных о погоде
     * @param weatherData объект с данными о температуре
     * @return SVG-изображение графика в виде строки XML
     */
    private String generateTemperatureChartSVG(WeatherData weatherData) {
        // Создаем набор данных для графика
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // Заполняем данными о температуре
        for (Temperature temperature : weatherData.temperature()) {
            dataset.addValue(temperature.value(), "Temperature", temperature.time().substring(0,5));
        }

        // - Без заголовка
        // - Ось X: "Hours" (Часы)
        // - Ось Y: "Temperature (C)"
        // - Ориентация: вертикальная
        // - Без легенды, подсказок и URL
        JFreeChart chart = ChartFactory.createLineChart(
                null,
                "Hours",
                "Temperature (C)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        //Применяем кастомный дизайн к графику
        customizeChartDesign(chart);

        //Рисуем график в SVG
        SVGGraphics2D g2 = new SVGGraphics2D(950, 500);
        chart.draw(g2, new Rectangle2D.Double(0, 0, 950, 500));
        return g2.getSVGElement();
    }

    /**
     * Настраивает дизайн графика
     * @param chart объект графика для настройки
     */
    private void customizeChartDesign(JFreeChart chart) {

        // Получаем область рисования графика
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();

        // Устанавливаем зеленый цвет линии
        renderer.setSeriesPaint(0, new Color(8, 113, 0));
        // Толщина линии графика - 3 пикселя
        renderer.setSeriesStroke(0, new BasicStroke(2f));
        // Включаем отображение точек на графике
        renderer.setSeriesShapesVisible(0, true);
        // Форма точек - круги диаметром 6 пикселей
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
        // Белый фон области графика
        plot.setBackgroundPaint(new Color(255, 255, 255));
        // Цвет сетки по оси Y (полупрозрачный зеленый)
        plot.setRangeGridlinePaint(new Color(0, 30, 1, 161));

        // Настраиваем шрифт меток:
        Font axisFont = new Font("Ubuntu", Font.BOLD, 10);
        plot.getDomainAxis().setTickLabelFont(axisFont);
    }
}