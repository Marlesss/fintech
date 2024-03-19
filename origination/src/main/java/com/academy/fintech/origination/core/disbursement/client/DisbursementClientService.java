package com.academy.fintech.origination.core.disbursement.client;

import com.academy.fintech.disbursement.DisbursementRequest;
import com.academy.fintech.origination.core.db.application.ApplicationEntity;
import com.academy.fintech.origination.core.disbursement.client.grpc.DisbursementGrpcClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Сервис для обращения к {@link com.academy.fintech.disbursement.DisbursementServiceGrpc}
 */
@Service
@RequiredArgsConstructor
public class DisbursementClientService {
    private final DisbursementGrpcClient disbursementGrpcClient;

    public void disbursement(ApplicationEntity application) {
        disbursementGrpcClient.disbursement(DisbursementRequest.newBuilder()
                .setAgreementId(String.valueOf(application.getAgreementId()))
                .setDisbursementAmount(application.getDisbursementAmount())
                .build());
    }
}
