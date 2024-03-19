package com.academy.fintech.pe.core.service.agreement.db.scheduled_payment;

import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleEntity;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.Finance;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link ScheduledPaymentService} interface
 */
@Service
@RequiredArgsConstructor
public class ScheduledPaymentServiceImpl implements ScheduledPaymentService {
    private final ScheduledPaymentRepository repository;

    @Override
    public List<ScheduledPaymentEntity> createScheduledPayments(AgreementEntity agreementEntity, Date disbursementDate) {
        List<ScheduledPaymentEntity> scheduledPaymentEntities = new ArrayList<>();
        double interestPerMonth = agreementEntity.getInterest().divide(BigDecimal.valueOf(100), MathContext.DECIMAL128).divide(BigDecimal.valueOf(12), MathContext.DECIMAL128).doubleValue();
        double periodPayment = Finance.pmt(interestPerMonth, agreementEntity.getTerm(), agreementEntity.getPrincipalAmount());
        LocalDate paymentDate = disbursementDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        for (int periodNumber = 1; periodNumber <= agreementEntity.getTerm(); periodNumber++) {
            paymentDate = paymentDate.plusMonths(1);
            double interest_payment = Finance.ipmt(interestPerMonth, periodNumber, agreementEntity.getTerm(), agreementEntity.getPrincipalAmount());
            double principal_payment = Finance.ppmt(interestPerMonth, periodNumber, agreementEntity.getTerm(), agreementEntity.getPrincipalAmount());
            ScheduledPaymentEntity scheduledPaymentEntity = createScheduledPaymentEntity(
                    Date.from(paymentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    BigDecimal.valueOf(periodPayment),
                    BigDecimal.valueOf(interest_payment),
                    BigDecimal.valueOf(principal_payment),
                    periodNumber);
            scheduledPaymentEntities.add(scheduledPaymentEntity);
        }
        return scheduledPaymentEntities;
    }

    @Override
    public void saveScheduledPayments(List<ScheduledPaymentEntity> scheduledPayments, PaymentScheduleEntity paymentScheduleEntity) {
        scheduledPayments.forEach(sp -> {
            sp.setPaymentEntityPK(ScheduledPaymentEntity.ScheduledPaymentEntityPK.builder()
                    .paymentScheduleEntity(paymentScheduleEntity)
                    .periodNumber(sp.getPaymentEntityPK().getPeriodNumber())
                    .build());
            repository.save(sp);
        });
    }

    @Override
    public List<ScheduledPaymentEntity> getScheduledPayments(PaymentScheduleEntity paymentScheduleEntity) {
        return repository.findAllByPaymentEntityPK_PaymentScheduleEntityOrderByPaymentEntityPK_PeriodNumberAsc(paymentScheduleEntity);
    }

    private ScheduledPaymentEntity createScheduledPaymentEntity(Date paymentDate, BigDecimal periodPayment, BigDecimal interestPayment, BigDecimal principalPayment, int periodNumber) {
        String paymentStatus = Instant.now().isBefore(paymentDate.toInstant()) ? "FUTURE" : "OVERDUE";
        return ScheduledPaymentEntity.builder()
                .paymentEntityPK(ScheduledPaymentEntity.ScheduledPaymentEntityPK.builder()
                        .periodNumber(periodNumber)
                        .build())
                .status(paymentStatus)
                .paymentDate(paymentDate)
                .periodPayment(periodPayment)
                .interestPayment(interestPayment)
                .principalPayment(principalPayment)
                .build();
    }
}
