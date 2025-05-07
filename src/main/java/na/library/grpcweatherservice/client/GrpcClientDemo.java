package na.library.grpcweatherservice.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import na.library.grpcweather.proto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcClientDemo {
    private static final Random random = new Random();

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        try {
            // Create stubs
            WeatherServiceGrpc.WeatherServiceBlockingStub blockingStub = WeatherServiceGrpc.newBlockingStub(channel);
            WeatherServiceGrpc.WeatherServiceStub asyncStub = WeatherServiceGrpc.newStub(channel);

            // 1. Unary RPC demo
            System.out.println("\n--- UNARY RPC DEMO ---");
            demoUnaryRpc(blockingStub);

            // 2. Server Streaming RPC demo
            System.out.println("\n--- SERVER STREAMING RPC DEMO ---");
            demoServerStreamingRpc(blockingStub);

            // 3. Client Streaming RPC demo
            System.out.println("\n--- CLIENT STREAMING RPC DEMO ---");
            demoClientStreamingRpc(asyncStub);

            // 4. Bidirectional Streaming RPC demo
            System.out.println("\n--- BIDIRECTIONAL STREAMING RPC DEMO ---");
            demoBidirectionalStreamingRpc(asyncStub);

        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static void demoUnaryRpc(WeatherServiceGrpc.WeatherServiceBlockingStub stub) {
        LocationRequest request = LocationRequest.newBuilder()
                .setCity("New York")
                .setCountry("USA")
                .build();

        System.out.println("Requesting current weather for: " + request.getCity() + ", " + request.getCountry());
        WeatherResponse response = stub.getCurrentWeather(request);
        System.out.println("Current weather: " + response.getDescription());
        System.out.println("Temperature: " + response.getTemperature() + "°C");
        System.out.println("Humidity: " + response.getHumidity() + "%");
        System.out.println("Wind Speed: " + response.getWindSpeed() + " km/h");
        System.out.println("Timestamp: " + response.getTimestamp());
    }

    private static void demoServerStreamingRpc(WeatherServiceGrpc.WeatherServiceBlockingStub stub) {
        LocationRequest request = LocationRequest.newBuilder()
                .setCity("London")
                .setCountry("UK")
                .build();

        System.out.println("Requesting 5-day forecast for: " + request.getCity() + ", " + request.getCountry());
        stub.getWeatherForecast(request).forEachRemaining(forecast -> {
            System.out.println("\nForecast for: " + forecast.getTimestamp());
            System.out.println("Weather: " + forecast.getDescription());
            System.out.println("Temperature: " + forecast.getTemperature() + "°C");
            System.out.println("Humidity: " + forecast.getHumidity() + "%");
            System.out.println("Wind Speed: " + forecast.getWindSpeed() + " km/h");
        });
    }

    private static void demoClientStreamingRpc(WeatherServiceGrpc.WeatherServiceStub asyncStub) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<SubmitResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(SubmitResponse response) {
                System.out.println("\nReceived response from server:");
                System.out.println("Success: " + response.getSuccess());
                System.out.println("Records processed: " + response.getRecordsProcessed());
                System.out.println("Message: " + response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error in client streaming: " + throwable.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed processing");
                finishLatch.countDown();
            }
        };

        StreamObserver<WeatherData> requestObserver = asyncStub.submitWeatherData(responseObserver);

        try {
            // Simulate sending weather data from multiple locations
            String[] locations = {"Tokyo, Japan", "Sydney, Australia", "Paris, France", "Berlin, Germany"};

            System.out.println("Sending weather data from multiple locations...");

            for (String location : locations) {
                WeatherData data = WeatherData.newBuilder()
                        .setLocation(location)
                        .setTemperature(15 + random.nextFloat() * 20)
                        .setHumidity(50 + random.nextFloat() * 40)
                        .setPressure(1000 + random.nextFloat() * 30)
                        .setWindSpeed(5 + random.nextFloat() * 20)
                        .setTimestamp(java.time.LocalDateTime.now().toString())
                        .build();

                requestObserver.onNext(data);
                System.out.println("Sent data for: " + location);
                Thread.sleep(100);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        // Mark the end of requests
        requestObserver.onCompleted();

        // Wait for the server to respond
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.err.println("Client streaming timed out!");
        }
    }

    private static void demoBidirectionalStreamingRpc(WeatherServiceGrpc.WeatherServiceStub asyncStub) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<WeatherAlert> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(WeatherAlert alert) {
                System.out.println("\nReceived weather alert:");
                System.out.println("Location: " + alert.getLocation());
                System.out.println("Alert Type: " + alert.getAlertType());
                System.out.println("Severity: " + alert.getSeverity());
                System.out.println("Description: " + alert.getDescription());
                System.out.println("Timestamp: " + alert.getTimestamp());
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error in bidirectional streaming: " + throwable.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending alerts");
                finishLatch.countDown();
            }
        };

        StreamObserver<LocationRequest> requestObserver = asyncStub.monitorWeather(responseObserver);

        try {
            // Monitor multiple locations
            List<LocationRequest> locations = new ArrayList<>();
            locations.add(LocationRequest.newBuilder().setCity("Miami").setCountry("USA").build());
            locations.add(LocationRequest.newBuilder().setCity("Tokyo").setCountry("Japan").build());
            locations.add(LocationRequest.newBuilder().setCity("Rio de Janeiro").setCountry("Brazil").build());

            System.out.println("Monitoring weather for multiple locations...");

            for (LocationRequest location : locations) {
                requestObserver.onNext(location);
                System.out.println("Started monitoring: " + location.getCity() + ", " + location.getCountry());
                Thread.sleep(500);
            }
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        // Mark the end of requests
        requestObserver.onCompleted();

        // Wait for server to finish sending alerts
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            System.err.println("Bidirectional streaming timed out!");
        }
    }
}
