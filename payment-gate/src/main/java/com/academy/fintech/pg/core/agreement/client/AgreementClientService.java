package com.academy.fintech.pg.core.agreement.client;

import com.academy.fintech.agreement.ActivateAgreementRequest;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pg.core.agreement.client.grpc.AgreementGrpcClient;
import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgreementClientService {
    private final AgreementGrpcClient agreementGrpcClient;

    public List<ScheduledPayment> activateAgreement(BigInteger agreementId, Instant disbursementDate) {
        Iterator<ScheduledPayment> iterator = agreementGrpcClient.activateAgreement(ActivateAgreementRequest.newBuilder()
                .setAgreementId(agreementId.toString())
                .setDisbursementDate(Timestamp.newBuilder()
                        .setSeconds(disbursementDate.getEpochSecond())
                        .setNanos(0)
                        .build())
                .build());
        List<ScheduledPayment> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }
}
