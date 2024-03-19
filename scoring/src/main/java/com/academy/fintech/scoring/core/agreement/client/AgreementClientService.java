package com.academy.fintech.scoring.core.agreement.client;

import com.academy.fintech.agreement.ApproximatedPaymentScheduleRequest;
import com.academy.fintech.agreement.GetAgreementsRequest;
import com.academy.fintech.agreement.PaymentScheduleRequest;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.scoring.core.agreement.client.grpc.AgreementGrpcClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgreementClientService {
    private final AgreementGrpcClient agreementGrpcClient;

    /**
     * Получает примерный ежемесячный платеж по договору
     *
     * @param agreementId id договора, для которого рассчитать ежемесячный платеж
     * @return ежемесячный платеж
     */
    public BigDecimal getApproximatedPeriodPayment(BigInteger agreementId) {
        List<ScheduledPayment> scheduledPayments = new ArrayList<>();
        agreementGrpcClient.getApproximatedPaymentSchedule(ApproximatedPaymentScheduleRequest
                .newBuilder()
                .setAgreementId(agreementId.toString())
                .build()
        ).forEachRemaining(scheduledPayments::add);
        return scheduledPayments.stream().map(ScheduledPayment::getPeriodPayment).map(BigDecimal::new).max(Comparator.naturalOrder()).orElseThrow();
    }

    /**
     * Получает id всех договоров, оформленных на клиента
     *
     * @param clientId id клиента
     * @return список id договоров
     */
    public List<BigInteger> getAgreementsIds(BigInteger clientId) {
        List<BigInteger> agreementIds = new ArrayList<>();
        agreementGrpcClient.getAgreements(GetAgreementsRequest.newBuilder().setClientId(clientId.toString()).build())
                .forEachRemaining(response -> agreementIds.add(new BigInteger(response.getAgreementId())));
        return agreementIds;
    }

    /**
     * Получает график платежей по id договора
     *
     * @param agreementId id договора
     * @return {@link Optional#empty()}, если договор еще не был активирован, иначе - список платежей.
     */
    public Optional<List<ScheduledPayment>> getPaymentSchedule(BigInteger agreementId) {
        List<ScheduledPayment> scheduledPayments = new ArrayList<>();
        try {
            agreementGrpcClient.getPaymentSchedule(PaymentScheduleRequest.newBuilder().setAgreementId(agreementId.toString()).build())
                    .forEachRemaining(scheduledPayments::add);
            return Optional.of(scheduledPayments);
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.INVALID_ARGUMENT) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
