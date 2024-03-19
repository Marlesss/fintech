package com.academy.fintech.pe.core.service.agreement.db.scheduled_payment;

import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@code payment_schedule_payment} table
 */
public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPaymentEntity, ScheduledPaymentEntity.ScheduledPaymentEntityPK> {
    List<ScheduledPaymentEntity> findAllByPaymentEntityPK_PaymentScheduleEntityOrderByPaymentEntityPK_PeriodNumberAsc(PaymentScheduleEntity paymentScheduleEntity);
}
