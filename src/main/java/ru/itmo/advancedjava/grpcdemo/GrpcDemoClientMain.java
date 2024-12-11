package ru.itmo.advancedjava.grpcdemo;

import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import ru.itmo.advancedjava.grpcdemo.domain.MyDude;
import ru.itmo.advancedjava.grpcdemo.proto.DemoServiceGrpc;
import ru.itmo.advancedjava.grpcdemo.proto.GetDudesRequest;
import ru.itmo.advancedjava.grpcdemo.proto.GetDudesResponse;
import ru.itmo.advancedjava.grpcdemo.service.MinimalDemoService;

public class GrpcDemoClientMain {
    public static void main(String[] args) {
        new GrpcDemoClientMain().run();
    }

    public void run() {
        var channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var stub = DemoServiceGrpc.newFutureStub(channel);

            var response = stub.getDudes(
                GetDudesRequest.newBuilder()
                    .addNames("Guy")
                    .addNames("Nonexistent")
                    .build()
            );

            var cf = new CompletableFuture<GetDudesResponse>();
            response.addListener(() -> {
                try {
                    cf.complete(response.get());
                } catch (InterruptedException | ExecutionException e) {
                    cf.completeExceptionally(e);
                }
            }, executor);

            System.out.println("Response: " + cf.join());
        } finally {
            channel.shutdown();
        }
    }
}