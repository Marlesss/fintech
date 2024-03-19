package com.academy.fintech.origination.core.scoring.client.grpc;

import com.academy.fintech.scoring.ScoringRequest;
import com.academy.fintech.scoring.ScoringResponse;
import com.academy.fintech.scoring.ScoringServiceGrpc;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * gRPC-клиент для доступа к {@link ScoringServiceGrpc}
 */
@Slf4j
@Component
public class ScoringGrpcClient {

    private final ScoringServiceGrpc.ScoringServiceBlockingStub stub;

    public ScoringGrpcClient(ScoringGrpcClientProperty property) {
        Channel channel = ManagedChannelBuilder.forAddress(property.host(), property.port()).usePlaintext().build();
        this.stub = ScoringServiceGrpc.newBlockingStub(channel);
    }

    public ScoringResponse score(ScoringRequest request) {
        return stub.score(request);
    }
}
