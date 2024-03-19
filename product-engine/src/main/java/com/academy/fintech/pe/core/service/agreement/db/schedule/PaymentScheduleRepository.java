package com.academy.fintech.pe.core.service.agreement.db.schedule;

import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Repository for {@code payment_schedule} table
 */
@Repository
public interface PaymentScheduleRepository extends JpaRepository<PaymentScheduleEntity, BigInteger> {
    /**
     * Inserts {@link PaymentScheduleEntity} with {@param version} version, associated with passed {@param agreementEntity}
     *
     * @param agreementEntity {@link AgreementEntity} to associate {@link PaymentScheduleEntity} with
     * @param version         version of {@link PaymentScheduleEntity}
     * @return inserted {@link PaymentScheduleEntity}
     */
    default PaymentScheduleEntity insertPaymentSchedule(AgreementEntity agreementEntity, int version) {
        return save(PaymentScheduleEntity.builder()
                .agreementEntity(agreementEntity)
                .version(version)
                .build());
    }

    /**
     * Get {@link PaymentScheduleEntity}, associated with passed {@param agreementEntity}, with the maximum version
     *
     * @param agreementEntity {@link AgreementEntity} to associate {@link PaymentScheduleEntity} with
     * @return {@code Optional.of(PaymentScheduleEntity)} if the payment schedule entity was found, else {@code Optional.empty()}
     */
    Optional<PaymentScheduleEntity> findTopByAgreementEntityOrderByVersionDesc(AgreementEntity agreementEntity);

}
