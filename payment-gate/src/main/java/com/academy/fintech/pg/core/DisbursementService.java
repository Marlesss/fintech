package com.academy.fintech.pg.core;

import com.academy.fintech.pg.core.agreement.client.AgreementClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;

import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class DisbursementService {
    private final AgreementClientService agreementClientService;

    public void disbursement(BigInteger agreementId, int disbursement) {
        // TODO :: ... disbursement logic
        try {
            sleep(4000);
        } catch (InterruptedException ignored) {
        }
        agreementClientService.activateAgreement(agreementId, Instant.now());
    }
}
