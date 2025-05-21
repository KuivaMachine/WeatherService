
<div align="center">
  <h1>🌦️ Weather Service</h1>
</div>
Сервис предоставляет прогноз погоды на 24 часа для указанного города, используя Open-Meteo API. 
Данные кэшируются в Redis, а также визуализируются в виде графика изменения температуры по часам.



📌 Features


    Получение текущей погоды по названию города
    Кэширование данных в Redis (TTL: 15 минут)
    Визуализация температуры в виде графика (график строится на основе данных за 24 часа)
    Автоматическое обновление данных при истечении срока кэша.

⚙️ Требования

    Java 17+
    Docker
    Gradle (опционально, можно использовать gradlew)

<div align="left">
  <h2>🛠️ Инструкция по запуску</h2>
</div>

Запустите Redis в Docker:

    docker run --name weather_redis -p 6379:6379 -d redis
Скачайте репозиторий:

    git clone https://github.com/ваш-репозиторий/weather-service.git

Перейдите в директорию с программой:
    
    cd weather-service

Соберите и запустите приложение:

    gradle build
    gradle run

Либо, если Gradle не установлен глобально:

    ./gradlew build 
    ./gradlew run

Сервис запущен!

    Доступен по адресу: http://localhost:9090/weather?city={CityName}


Остановка сервиса:

    Нажмите Ctrl+C в терминале, где работает приложение.

<div align="center">
 <picture>
  <img src="https://github.com/KuivaMachine/WeatherService/blob/master/src/main/resources/image.png"alt="Описание изображения">
</picture>
</div>
