package com.academy.fintech.origination.core.disbursement.client.grpc;

import com.academy.fintech.disbursement.DisbursementRequest;
import com.academy.fintech.disbursement.DisbursementResponse;
import com.academy.fintech.disbursement.DisbursementServiceGrpc;
import com.academy.fintech.scoring.ScoringServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * gRPC-клиент для доступа к {@link DisbursementServiceGrpc}
 */
@Slf4j
@Component
public class DisbursementGrpcClient {
    private final DisbursementServiceGrpc.DisbursementServiceBlockingStub stub;

    public DisbursementGrpcClient(DisbursementGrpcClientProperty property) {
        Channel channel = ManagedChannelBuilder.forAddress(property.host(), property.port()).usePlaintext().build();
        this.stub = DisbursementServiceGrpc.newBlockingStub(channel);
    }

    public DisbursementResponse disbursement(DisbursementRequest request) {
        return stub.disbursement(request);
    }
}
