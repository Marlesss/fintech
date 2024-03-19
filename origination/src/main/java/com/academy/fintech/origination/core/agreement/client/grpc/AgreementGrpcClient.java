package com.academy.fintech.origination.core.agreement.client.grpc;

import com.academy.fintech.agreement.AgreementResponse;
import com.academy.fintech.agreement.AgreementServiceGrpc;
import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.agreement.Product;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * gRPC-клиент для доступа к {@link AgreementServiceGrpc}
 */
@Slf4j
@Component
public class AgreementGrpcClient {
    private final AgreementServiceGrpc.AgreementServiceBlockingStub stub;

    public AgreementGrpcClient(AgreementGrpcClientProperty property) {
        Channel channel = ManagedChannelBuilder.forAddress(property.host(), property.port()).usePlaintext().build();
        this.stub = AgreementServiceGrpc.newBlockingStub(channel);
    }

    public AgreementResponse createAgreement(CreateAgreementRequest createAgreementRequest) {
        return stub.create(createAgreementRequest);
    }

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        stub.getProducts(Empty.getDefaultInstance()).forEachRemaining(products::add);
        return products;
    }
}
