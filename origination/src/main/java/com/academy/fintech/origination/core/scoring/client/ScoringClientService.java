package com.academy.fintech.origination.core.scoring.client;

import com.academy.fintech.origination.core.scoring.client.grpc.ScoringGrpcClient;
import com.academy.fintech.scoring.ScoringRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

/**
 * Сервис для обращения к {@link com.academy.fintech.scoring.ScoringServiceGrpc}
 */
@Service
@RequiredArgsConstructor
public class ScoringClientService {
    private final ScoringGrpcClient scoringGrpcClient;

    /**
     * Запрашивает проверку созданного договора на одобрение
     *
     * @param clientId    id клиента
     * @param agreementId id договора, который нужно проверить
     * @param salary      заработная плата клиента
     * @return значение проверки
     */
    public int score(BigInteger clientId, BigInteger agreementId, int salary) {
        return scoringGrpcClient.score(ScoringRequest.newBuilder()
                .setClientId(clientId.toString())
                .setAgreementId(agreementId.toString())
                .setClientSalary(salary)
                .build()).getScore();
    }
}
