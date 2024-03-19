package com.academy.fintech.pe.core.service.agreement.db.scheduled_payment;

import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleEntity;

import java.util.Date;
import java.util.List;

/**
 * Service to work with {@link ScheduledPaymentRepository}
 */
public interface ScheduledPaymentService {

    /**
     * Creates scheduled payments for passed {@param agreementEntity} since {@param disbursementDate}
     * without pairing it with {@link PaymentScheduleEntity} and saving.
     *
     * @param agreementEntity  {@link AgreementEntity} to create scheduled payments for
     * @param disbursementDate date of disbursement
     * @return {@link List} of {@link ScheduledPaymentEntity}
     */
    List<ScheduledPaymentEntity> createScheduledPayments(AgreementEntity agreementEntity, Date disbursementDate);

    /**
     * Pairs scheduled payments with {@param paymentScheduleEntity} and saves it.
     *
     * @param scheduledPayments     scheduled payments to pair and save
     * @param paymentScheduleEntity payment schedule entity to pair with
     */
    void saveScheduledPayments(List<ScheduledPaymentEntity> scheduledPayments, PaymentScheduleEntity paymentScheduleEntity);

    /**
     * Gets scheduled payments paired with {@param paymentScheduleEntity}
     *
     * @param paymentScheduleEntity payment schedule paired with desired scheduled payments
     * @return {@link List} of {@link ScheduledPayment} paired with {@param paymentScheduleEntity}, ordered by payment period
     */
    List<ScheduledPaymentEntity> getScheduledPayments(PaymentScheduleEntity paymentScheduleEntity);
}
