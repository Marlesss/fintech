package com.academy.fintech.pe.core.service.agreement.db.schedule;

import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;

import java.util.List;
import java.util.Optional;

/**
 * Service to work with {@link PaymentScheduleRepository}
 */
public interface PaymentScheduleService {
    /**
     * Creates payment schedule for passed {@param agreementEntity}
     *
     * @param agreementEntity {@link AgreementEntity} to create payment schedule for
     * @return {@link PaymentScheduleEntity} the created payment schedule is associated with
     */
    PaymentScheduleEntity createPaymentSchedule(AgreementEntity agreementEntity);

    /**
     * Get the {@link PaymentScheduleEntity}, the relevant payment schedule for passed {@param agreementEntity} is associated with
     *
     * @param agreementEntity {@link AgreementEntity} the relevant payment schedule is searching for
     * @return {@link PaymentScheduleEntity} the relevant payment schedule is associated with
     */

    Optional<PaymentScheduleEntity> getPaymentScheduleEntity(AgreementEntity agreementEntity);

    /**
     * Get the relevant payment schedule for passed {@param agreementEntity}.
     *
     * @param agreementEntity {@link AgreementEntity} the relevant payment schedule is searching for
     * @return {@link List} of {@link ScheduledPayment} of the relevant payment schedule, ordered by payment period
     */
    Optional<List<ScheduledPayment>> getScheduledPayments(AgreementEntity agreementEntity);

}
