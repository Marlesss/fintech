package com.academy.fintech.origination.core.agreement.client;

import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.agreement.ProductRequest;
import com.academy.fintech.origination.core.agreement.client.grpc.AgreementGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Сервис для обращения к {@link com.academy.fintech.agreement.AgreementServiceGrpc}
 */
@Service
@RequiredArgsConstructor
public class AgreementClientService {
    private final AgreementGrpcClient agreementGrpcClient;

    /**
     * @param clientId clientId клиента, который запрашивает кредит
     * @param disbursementAmount запрошенный размер кредита
     * @return agreementId созданного договора
     * @throws NoSuchElementException если не был найден продукт, удовлетворяющий условиям
     */
    public BigInteger createAgreement(BigInteger clientId, int disbursementAmount) throws NoSuchElementException {
        ProductRequest productRequest = generateProductRequest(disbursementAmount)
                .orElseThrow(() -> new NoSuchElementException("No satisfying product was found"));
        return new BigInteger(agreementGrpcClient.createAgreement(CreateAgreementRequest.newBuilder()
                        .setClientId(String.valueOf(clientId))
                        .setProduct(productRequest)
                        .build())
                .getAgreementId());
    }


    private Optional<ProductRequest> generateProductRequest(int disbursementAmount) {
        return agreementGrpcClient.getProducts().stream().filter(p -> {
                    long minPrincipalAmount = p.getMinOriginationAmount() + disbursementAmount;
                    return p.getMinPrincipalAmount() <= minPrincipalAmount && minPrincipalAmount <= p.getMaxPrincipalAmount();
                })
                .findAny()
                .map(p -> ProductRequest.newBuilder()
                        .setCode(p.getCode())
                        .setLoanTerm(p.getMaxLoanTerm())
                        .setDisbursementAmount(disbursementAmount)
                        .setOriginationAmount(p.getMinOriginationAmount())
                        .setInterest(p.getMinInterest())
                        .build());
    }
}
