package na.library.grpcweatherservice.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import na.library.grpcweatherservice.exception.GlobalExceptionInterceptor;
import na.library.grpcweatherservice.service.WeatherServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
 Manual Server
*/
@Component
public class GrpcServer {

    @Value("${grpc.server.port:8090}")
    private int port;

    private Server server;

    private final WeatherServiceImpl weatherService;

    public GrpcServer(WeatherServiceImpl weatherService) {
        this.weatherService = weatherService;
    }

    @PostConstruct
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(weatherService)
                .addService(ServerInterceptors.intercept(weatherService, new GlobalExceptionInterceptor()))
                .build()
                .start();

        System.out.println("gRPC Server started, listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            GrpcServer.this.stop();
        }));
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}