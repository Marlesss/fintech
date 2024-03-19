package com.academy.fintech.pg.core.agreement.client.grpc;

import com.academy.fintech.agreement.ActivateAgreementRequest;
import com.academy.fintech.agreement.AgreementServiceGrpc;
import com.academy.fintech.agreement.ScheduledPayment;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class AgreementGrpcClient {
    private final AgreementServiceGrpc.AgreementServiceBlockingStub stub;

    public AgreementGrpcClient(AgreementGrpcClientProperty property) {
        Channel channel = ManagedChannelBuilder.forAddress(property.host(), property.port()).usePlaintext().build();
        this.stub = AgreementServiceGrpc.newBlockingStub(channel);
    }

    public Iterator<ScheduledPayment> activateAgreement(ActivateAgreementRequest request) {
        return stub.activateAgreement(request);
    }
}
