package ru.itmo.advancedjava.grpcdemo.service;

import static java.util.stream.Collectors.toUnmodifiableMap;

import io.grpc.BindableService;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.itmo.advancedjava.grpcdemo.domain.MyDude;
import ru.itmo.advancedjava.grpcdemo.proto.DemoServiceGrpc;
import ru.itmo.advancedjava.grpcdemo.proto.GetDudesRequest;
import ru.itmo.advancedjava.grpcdemo.proto.GetDudesResponse;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MinimalDemoService extends DemoServiceGrpc.DemoServiceImplBase {

    private final Map<String, MyDude> dudes;

    public static BindableService create(Set<MyDude> dudes) {
        return new MinimalDemoService(
            dudes.stream().collect(toUnmodifiableMap(
                MyDude::name,
                Function.identity()
            ))
        );
    }

    @Override
    public void getDudes(
        GetDudesRequest request,
        StreamObserver<GetDudesResponse> responseObserver
    ) {
        try {
            var response = doGetDudes(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusException e) {
            responseObserver.onError(e);
        } catch (Throwable x) {
            // TODO: OutOfMemoryError
            responseObserver.onError(Status.INTERNAL
                .withDescription("Unexpected exception caught")
                .withCause(x)
                .asException()
            );
        }
    }

    public GetDudesResponse doGetDudes(GetDudesRequest request) throws StatusException {
        if (request.getDebug()) {
            throw Status.INVALID_ARGUMENT
                .withDescription("Debugging is not supported")
                .asException();
        }

        var response = GetDudesResponse.newBuilder();
        for (var name : request.getNamesList()) {
            var dude = dudes.get(name);
            if (dude != null) {
                response.addDudes(dude.toProto());
            }
        }

        return response.build();
    }

    @Override
    public void serverStreaming(GetDudesRequest request, StreamObserver<GetDudesResponse> responseObserver) {
        for (var name : request.getNamesList()) {
            var dude = dudes.get(name);
            if (dude != null) {
                responseObserver.onNext(GetDudesResponse.newBuilder()
                    .addDudes(dude.toProto())
                    .build()
                );

                try {
                    Thread.sleep(Duration.ofSeconds(3));
                } catch (InterruptedException e) {
                    responseObserver.onError(Status.CANCELLED
                        .withCause(e)
                        .withDescription("Server cancelled")
                        .asException()
                    );
                    return;
                }
            }
        }

        responseObserver.onCompleted();
    }
}
