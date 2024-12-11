package ru.itmo.advancedjava.grpcdemo;

import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import java.io.IOException;
import java.util.Set;
import ru.itmo.advancedjava.grpcdemo.domain.MyDude;
import ru.itmo.advancedjava.grpcdemo.service.MinimalDemoService;

public class GrpcDemoServerMain {
    public static void main(String[] args) {
        new GrpcDemoServerMain().run();
    }

    public void run() {
        var service = MinimalDemoService.create(Set.of(
            new MyDude("Guy", 12),
            new MyDude("Tom", 34)
        ));

        // ManagedChannelBuilder.forAddress("localhost", 8080)
        var server = ServerBuilder.forPort(8080)
            .addService(service)
            .addService(ProtoReflectionServiceV1.newInstance())
            .build();

        try {
            server.start().awaitTermination();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}