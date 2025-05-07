package na.library.grpcweatherservice.controller;

import na.library.grpcweather.proto.*;
import na.library.grpcweatherservice.client.WeatherClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherClient weatherClient;

    public WeatherController(WeatherClient weatherClient) {
        this.weatherClient = weatherClient;
    }

    // Demonstrate Unary RPC
    @GetMapping("/current")
    public WeatherResponse getCurrentWeather(@RequestParam String city, @RequestParam String country) {
        return weatherClient.getCurrentWeather(city, country);
    }

    // Demonstrate Server Streaming RPC
    @GetMapping("/forecast")
    public List<WeatherResponse> getWeatherForecast(@RequestParam String city, @RequestParam String country) {
        return weatherClient.getWeatherForecast(city, country);
    }

    // Demonstrate Client Streaming RPC
    @PostMapping("/submit")
    public SubmitResponse submitWeatherData(@RequestBody List<Map<String, Object>> dataList) throws InterruptedException {
        List<WeatherData> weatherDataList = new ArrayList<>();

        for (Map<String, Object> data : dataList) {
            String location = (String) data.get("location");
            float temperature = ((Number) data.get("temperature")).floatValue();
            float humidity = ((Number) data.get("humidity")).floatValue();
            float pressure = ((Number) data.get("pressure")).floatValue();
            float windSpeed = ((Number) data.get("windSpeed")).floatValue();

            weatherDataList.add(weatherClient.createWeatherData(location, temperature, humidity, pressure, windSpeed));
        }

        return weatherClient.submitWeatherData(weatherDataList);
    }

    // Demonstrate Bidirectional Streaming RPC
    @PostMapping("/monitor")
    public List<WeatherAlert> monitorWeather(@RequestBody List<Map<String, String>> locations) throws InterruptedException {
        List<LocationRequest> locationRequests = new ArrayList<>();

        for (Map<String, String> location : locations) {
            LocationRequest request = LocationRequest.newBuilder()
                    .setCity(location.get("city"))
                    .setCountry(location.get("country"))
                    .build();

            locationRequests.add(request);
        }

        return weatherClient.monitorWeather(locationRequests);
    }
}