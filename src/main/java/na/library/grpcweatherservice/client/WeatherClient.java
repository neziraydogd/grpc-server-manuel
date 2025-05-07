package na.library.grpcweatherservice.client;

import jakarta.annotation.PostConstruct;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;
import na.library.grpcweather.proto.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class WeatherClient {

    private ManagedChannel channel;
    private WeatherServiceGrpc.WeatherServiceBlockingStub blockingStub;
    private WeatherServiceGrpc.WeatherServiceStub asyncStub;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    public void init() {
        // Create a channel to the server
        channel = ManagedChannelBuilder.forAddress("localhost", 8090)
                .usePlaintext()
                .build();

        // Create stubs
        blockingStub = WeatherServiceGrpc.newBlockingStub(channel);
        asyncStub = WeatherServiceGrpc.newStub(channel);
    }

    // Unary RPC demonstration
    public WeatherResponse getCurrentWeather(String city, String country) {
        LocationRequest request = LocationRequest.newBuilder()
                .setCity(city)
                .setCountry(country)
                .build();

        return blockingStub.getCurrentWeather(request);
    }

    // Server Streaming RPC demonstration
    public List<WeatherResponse> getWeatherForecast(String city, String country) {
        LocationRequest request = LocationRequest.newBuilder()
                .setCity(city)
                .setCountry(country)
                .build();

        List<WeatherResponse> responses = new ArrayList<>();
        blockingStub.getWeatherForecast(request).forEachRemaining(responses::add);

        return responses;
    }

    // Client Streaming RPC demonstration
    public SubmitResponse submitWeatherData(List<WeatherData> weatherDataList) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final SubmitResponse[] response = new SubmitResponse[1];

        StreamObserver<SubmitResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(SubmitResponse submitResponse) {
                response[0] = submitResponse;
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error during submitWeatherData: " + throwable.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<WeatherData> requestObserver = asyncStub.submitWeatherData(responseObserver);

        try {
            for (WeatherData data : weatherDataList) {
                requestObserver.onNext(data);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        // Wait for the server to respond
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            throw new RuntimeException("submitWeatherData timed out");
        }

        return response[0];
    }

    // Bidirectional Streaming RPC demonstration
    public List<WeatherAlert> monitorWeather(List<LocationRequest> locations) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final List<WeatherAlert> alerts = new ArrayList<>();

        StreamObserver<WeatherAlert> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(WeatherAlert alert) {
                System.out.println("Received alert: " + alert.getAlertType() + " for " + alert.getLocation());
                alerts.add(alert);
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error during monitorWeather: " + throwable.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Completed receiving alerts");
                finishLatch.countDown();
            }
        };

        StreamObserver<LocationRequest> requestObserver = asyncStub.monitorWeather(responseObserver);

        try {
            // Send all location requests
            for (LocationRequest location : locations) {
                requestObserver.onNext(location);
                Thread.sleep(500); // Add a delay between location requests
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        requestObserver.onCompleted();

        // Wait for all alerts
        finishLatch.await(1, TimeUnit.MINUTES);

        return alerts;
    }

    // Helper method to create a weather data object
    public WeatherData createWeatherData(String location, float temperature, float humidity, float pressure, float windSpeed) {
        return WeatherData.newBuilder()
                .setLocation(location)
                .setTemperature(temperature)
                .setHumidity(humidity)
                .setPressure(pressure)
                .setWindSpeed(windSpeed)
                .setTimestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}