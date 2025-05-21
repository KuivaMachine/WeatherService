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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class WeatherView {
    private final WeatherService weatherService;

    public WeatherView(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public void setupRoutes() {
        Spark.get("/weather", ((request, response) -> getWeather(request, response)));
    }

    String getWeather(Request request, Response response) {

        //Получаем город из запроса
        String city = request.queryParams("city");

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
            }
        }
        return String.format("%.1f", closestTemp);
    }

    private String generateTemperatureChartSVG(WeatherData weatherData) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Temperature temperature : weatherData.temperature()) {
            dataset.addValue(temperature.value(), "Temperature", temperature.time().substring(0,5));
        }

        JFreeChart chart = ChartFactory.createLineChart(
                null,
                "Time",
                "Temperature  (°C)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );


        customizeChartDesign(chart);

        SVGGraphics2D g2 = new SVGGraphics2D(800, 400);
        chart.draw(g2, new Rectangle2D.Double(0, 0, 800, 400));
        return g2.getSVGElement();
    }

    private void customizeChartDesign(JFreeChart chart) {

        CategoryPlot plot = chart.getCategoryPlot();


        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180)); // SteelBlue
        renderer.setSeriesStroke(0, new BasicStroke(3f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));


        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));


        Font axisFont = new Font("Arial", Font.PLAIN, 6);
        plot.getDomainAxis().setTickLabelFont(axisFont);
        plot.getRangeAxis().setTickLabelFont(axisFont);
        plot.getRangeAxis().setLabelFont(axisFont.deriveFont(Font.BOLD));


        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(70, 130, 180, 100),
                0, 400, new Color(70, 130, 180, 20)
        );
        renderer.setSeriesFillPaint(0, gradient);

    }
}