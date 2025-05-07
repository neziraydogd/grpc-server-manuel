package na.library.grpcweatherservice.service;

import io.grpc.stub.StreamObserver;
import na.library.grpcweather.proto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class WeatherServiceImpl extends WeatherServiceGrpc.WeatherServiceImplBase {

    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ConcurrentMap<String, List<WeatherAlert>> alertsByLocation = new ConcurrentHashMap<>();

    // Unary RPC Implementation
    @Override
    public void getCurrentWeather(LocationRequest request, StreamObserver<WeatherResponse> responseObserver) {
        System.out.println("Received request for current weather in " + request.getCity() + ", " + request.getCountry());
        if (request.getCity().isEmpty() || request.getCountry().isEmpty()) {
            throw new IllegalArgumentException("City and Country must not be empty");
        }
        // Create a simulated weather response
        WeatherResponse response = WeatherResponse.newBuilder()
                .setLocation(request.getCity() + ", " + request.getCountry())
                .setTemperature(15 + random.nextFloat() * 15)
                .setDescription(getRandomWeatherDescription())
                .setHumidity(50 + random.nextFloat() * 40)
                .setWindSpeed(5 + random.nextFloat() * 20)
                .setTimestamp(LocalDateTime.now().format(formatter))
                .build();

        // Send the response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // Server Streaming RPC Implementation
    @Override
    public void getWeatherForecast(LocationRequest request, StreamObserver<WeatherResponse> responseObserver) {
        System.out.println("Received request for weather forecast in " + request.getCity() + ", " + request.getCountry());

        // Simulate sending multiple forecast updates (one per day for next 5 days)
        try {
            for (int i = 0; i < 5; i++) {
                LocalDateTime forecastDate = LocalDateTime.now().plusDays(i);

                WeatherResponse response = WeatherResponse.newBuilder()
                        .setLocation(request.getCity() + ", " + request.getCountry())
                        .setTemperature(15 + random.nextFloat() * 15)
                        .setDescription(getRandomWeatherDescription())
                        .setHumidity(50 + random.nextFloat() * 40)
                        .setWindSpeed(5 + random.nextFloat() * 20)
                        .setTimestamp(forecastDate.format(formatter))
                        .build();

                responseObserver.onNext(response);

                // Simulate delay between forecast updates
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            responseObserver.onError(e);
            return;
        }

        responseObserver.onCompleted();
    }

    // Client Streaming RPC Implementation
    @Override
    public StreamObserver<WeatherData> submitWeatherData(StreamObserver<SubmitResponse> responseObserver) {
        return new StreamObserver<>() {
            private final List<WeatherData> receivedData = new ArrayList<>();

            @Override
            public void onNext(WeatherData weatherData) {
                System.out.println("Received weather data for: " + weatherData.getLocation());
                receivedData.add(weatherData);
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error during submitWeatherData: " + throwable.getMessage());
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed receiving weather data. Total records: " + receivedData.size());

                // Process all collected weather data
                SubmitResponse response = SubmitResponse.newBuilder()
                        .setSuccess(true)
                        .setRecordsProcessed(receivedData.size())
                        .setMessage("Successfully processed " + receivedData.size() + " weather data records")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    // Bidirectional Streaming RPC Implementation
    @Override
    public StreamObserver<LocationRequest> monitorWeather(StreamObserver<WeatherAlert> responseObserver) {
        return new StreamObserver<>() {
            private final List<String> monitoredLocations = new ArrayList<>();

            @Override
            public void onNext(LocationRequest request) {
                String location = request.getCity() + ", " + request.getCountry();
                System.out.println("Started monitoring weather for: " + location);
                monitoredLocations.add(location);

                // Simulate generating a random alert for this location
                if (random.nextBoolean()) {
                    WeatherAlert alert = generateRandomAlert(location);
                    responseObserver.onNext(alert);

                    // Store the alert
                    alertsByLocation.computeIfAbsent(location, k -> new ArrayList<>()).add(alert);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error during monitorWeather: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Client completed monitoring requests. Sending any remaining alerts...");

                // Send any final alerts
                for (String location : monitoredLocations) {
                    if (random.nextBoolean()) {
                        responseObserver.onNext(generateRandomAlert(location));
                    }
                }

                responseObserver.onCompleted();
            }
        };
    }

    // Helper methods
    private String getRandomWeatherDescription() {
        String[] descriptions = {"Sunny", "Partly Cloudy", "Cloudy", "Overcast", "Light Rain", "Heavy Rain", "Thunderstorm", "Snowy", "Foggy"};
        return descriptions[random.nextInt(descriptions.length)];
    }

    private WeatherAlert generateRandomAlert(String location) {
        String[] alertTypes = {"STORM", "HEAT_WAVE", "FLOOD", "TORNADO", "HURRICANE", "BLIZZARD", "HIGH_WINDS"};
        String[] severities = {"LOW", "MEDIUM", "HIGH", "SEVERE"};

        String alertType = alertTypes[random.nextInt(alertTypes.length)];
        String severity = severities[random.nextInt(severities.length)];
        String description = generateAlertDescription(alertType);

        return WeatherAlert.newBuilder()
                .setLocation(location)
                .setAlertType(alertType)
                .setDescription(description)
                .setSeverity(severity)
                .setTimestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    private String generateAlertDescription(String alertType) {
        return switch (alertType) {
            case "STORM" -> "Severe thunderstorm expected with potential for lightning and heavy rainfall";
            case "HEAT_WAVE" -> "Prolonged period of extremely high temperatures expected";
            case "FLOOD" -> "Risk of flooding due to heavy rainfall";
            case "TORNADO" -> "Conditions favorable for tornado formation";
            case "HURRICANE" -> "Hurricane approaching with high winds and heavy rainfall";
            case "BLIZZARD" -> "Heavy snowfall and strong winds expected";
            case "HIGH_WINDS" -> "Strong gusty winds expected";
            default -> "Weather alert";
        };
    }
}