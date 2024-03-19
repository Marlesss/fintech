package com.academy.fintech.origination.core.grpc.application.v1;

import com.academy.fintech.application.ApplicationRequest;
import com.academy.fintech.application.ApplicationResponse;
import com.academy.fintech.application.ApplicationServiceGrpc;
import com.academy.fintech.application.CancelRequest;
import com.academy.fintech.application.CancelResponse;
import com.academy.fintech.origination.core.ApplicationCreationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.math.BigInteger;
import java.util.NoSuchElementException;

/**
 * gRPC-сервис для обработки запросов к origination.
 */
@Slf4j
@GRpcService
@RequiredArgsConstructor
public class ApplicationController extends ApplicationServiceGrpc.ApplicationServiceImplBase {
    private final ApplicationCreationService applicationCreationService;

    @Override
    public void create(ApplicationRequest request, StreamObserver<ApplicationResponse> responseObserver) {
        log.info("Got create request: {}", request);

        try {
            BigInteger applicationId = applicationCreationService.create(request);
            responseObserver.onNext(
                    ApplicationResponse.newBuilder()
                            .setApplicationId(applicationId.toString())
                            .build()
            );
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND.withCause(e).asException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).asException());
        }
    }

    @Override
    public void cancel(CancelRequest request, StreamObserver<CancelResponse> responseObserver) {
        log.info("Got cancel request: {}", request);

        try {
            applicationCreationService.cancel(new BigInteger(request.getApplicationId()));
            responseObserver.onNext(CancelResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            responseObserver.onError(Status.NOT_FOUND.withCause(e).asException());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).asException());
        }
    }

}
