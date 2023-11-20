package com.academy.fintech.origination.grpc.application.v1;

import com.academy.fintech.application.ApplicationRequest;
import com.academy.fintech.application.ApplicationResponse;
import com.academy.fintech.application.ApplicationServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

@Slf4j
@GRpcService
public class ApplicationController extends ApplicationServiceGrpc.ApplicationServiceImplBase {

    @Override
    public void create(ApplicationRequest request, StreamObserver<ApplicationResponse> responseObserver) {
        log.info("Got request: {}", request);
        // Add logic here...

        responseObserver.onNext(
                ApplicationResponse.newBuilder()
                        .setApplicationId("test-application-id")
                        .build()
        );
        responseObserver.onCompleted();
    }

}
