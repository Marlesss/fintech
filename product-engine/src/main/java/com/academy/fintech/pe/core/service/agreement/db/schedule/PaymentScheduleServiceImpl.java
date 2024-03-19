package com.academy.fintech.pe.core.service.agreement.db.schedule;

import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentEntity;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentRepository;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link PaymentScheduleService} interface
 *
 * @see ScheduledPaymentRepository
 */
@Service
@RequiredArgsConstructor
public class PaymentScheduleServiceImpl implements PaymentScheduleService {
    private final PaymentScheduleRepository paymentScheduleRepository;
    private final ScheduledPaymentService scheduledPaymentService;

    @Override
    public PaymentScheduleEntity createPaymentSchedule(AgreementEntity agreementEntity) {
        PaymentScheduleEntity paymentScheduleEntity = paymentScheduleRepository.insertPaymentSchedule(agreementEntity, 1);
        List<ScheduledPaymentEntity> scheduledPaymentEntities = scheduledPaymentService.createScheduledPayments(agreementEntity, agreementEntity.getDisbursementDate());
        scheduledPaymentService.saveScheduledPayments(scheduledPaymentEntities, paymentScheduleEntity);
        return paymentScheduleEntity;
    }

    @Override
    public Optional<PaymentScheduleEntity> getPaymentScheduleEntity(AgreementEntity agreement) {
        return paymentScheduleRepository.findTopByAgreementEntityOrderByVersionDesc(agreement);
    }

    @Override
    public Optional<List<ScheduledPayment>> getScheduledPayments(AgreementEntity agreementEntity) {
        return getPaymentScheduleEntity(agreementEntity).map(pse ->
                scheduledPaymentService.getScheduledPayments(pse).stream()
                        .map(ScheduledPaymentEntity::toSchedulePayment).toList());
    }
}
