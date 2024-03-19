package com.academy.fintech.scoring.core;

import com.academy.fintech.agreement.PaymentStatus;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.scoring.core.agreement.client.AgreementClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для проверки договоров на одобрение.
 */
@Service
@RequiredArgsConstructor
public class ScoringService {
    private final AgreementClientService agreementClientService;

    /**
     * Рассчитывает баллы одобрения договора.
     *
     * @param clientId     id клиента
     * @param agreementId  id договора, для которого рассчитать баллы одобрения
     * @param clientSalary зарплата клиента
     * @return баллы одобрения
     */
    public int score(BigInteger clientId, BigInteger agreementId, int clientSalary) {
        int score = 0;
        score += checkPeriodPayment(agreementId, clientSalary);
        score += checkExistingAgreements(clientId);
        return score;
    }

    /**
     * Сравнивает ежемесячный платеж с зарплатой клиента.
     *
     * @param agreementId  id клиента
     * @param clientSalary зарплата клиента
     * @return баллы одобрения. Если ежемесячный платеж не больше 1/3 от зарплаты клиента, то это 1 балл, иначе 0.
     */
    private int checkPeriodPayment(BigInteger agreementId, int clientSalary) {
        BigDecimal approximatedPeriodPayment = agreementClientService.getApproximatedPeriodPayment(agreementId);
        if (approximatedPeriodPayment.doubleValue() * 3 <= clientSalary) {
            return 1;
        }
        return 0;
    }

    /**
     * Проверяет существующие кредиты.
     * Если у клиента есть существующий кредит и у него в нем есть просрочка более 7 дней, то -1 балл,
     * если просрочка до 7 дней, то 0 баллов, если есть кредит без просрочки или нет кредита, то 1 балл
     *
     * @param clientId id клиента
     * @return баллы одобрения
     */
    private int checkExistingAgreements(BigInteger clientId) {
        List<BigInteger> agreementsIds = agreementClientService.getAgreementsIds(clientId);
        return agreementsIds.stream()
                .map(agreementClientService::getPaymentSchedule)
                .flatMap(Optional::stream)
                .map(this::checkScheduledPaymentsForDebts)
                .min(Comparator.naturalOrder())
                .orElse(1);
    }

    /**
     * Проверяет график платежей на долги.
     * Если у кредита есть просрочка более 7 дней, то -1 балл,
     * если просрочка до 7 дней, то 0 баллов,
     * если кредит без просрочки, то 1 балл
     *
     * @param scheduledPayments график платежей
     * @return баллы одобрения
     */
    private int checkScheduledPaymentsForDebts(List<ScheduledPayment> scheduledPayments) {
        LocalDate today = LocalDate.now();
        return scheduledPayments.stream()
                .map(payment -> {
                    if (payment.getStatus() == PaymentStatus.OVERDUE) {
                        if (ChronoUnit.DAYS.between(dateFromTimestamp(payment.getPaymentDate().getSeconds()), today) > 7) {
                            return -1;
                        }
                        return 0;
                    }
                    return 1;
                }).min(Comparator.naturalOrder()).orElse(0);
    }

    private static LocalDate dateFromTimestamp(long seconds) {
        return LocalDate.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
    }
}
