package com.academy.fintech.pg.core.grpc.disbursement.v1;

import com.academy.fintech.disbursement.DisbursementRequest;
import com.academy.fintech.disbursement.DisbursementResponse;
import com.academy.fintech.disbursement.DisbursementServiceGrpc;
import com.academy.fintech.pg.core.DisbursementService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@GRpcService
@RequiredArgsConstructor
public class DisbursementController extends DisbursementServiceGrpc.DisbursementServiceImplBase {
    private final DisbursementService disbursementService;
    @Override
    public void disbursement(DisbursementRequest request, StreamObserver<DisbursementResponse> responseObserver) {
        log.info("Got disbursement request: {}", request);

        try {
            disbursementService.disbursement(new BigInteger(request.getAgreementId()), request.getDisbursementAmount());
            responseObserver.onNext(DisbursementResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Exception on request: {}", request, e);
            responseObserver.onError(e);
        }

    }
}
