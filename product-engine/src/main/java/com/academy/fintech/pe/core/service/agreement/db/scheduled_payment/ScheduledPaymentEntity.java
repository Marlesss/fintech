package com.academy.fintech.pe.core.service.agreement.db.scheduled_payment;

import com.academy.fintech.agreement.PaymentStatus;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleEntity;
import com.google.protobuf.Timestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for {@code payment_schedule_payment} table in database
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Entity
@Table(name = "payment_schedule_payment")
public class ScheduledPaymentEntity {
    @EmbeddedId
    private ScheduledPaymentEntityPK paymentEntityPK;
    @JoinColumn(name = "status")
    private String status;
    @JoinColumn(name = "payment_date")
    private Date paymentDate;
    @JoinColumn(name = "period_payment")
    private BigDecimal periodPayment;
    @JoinColumn(name = "interest_payment")
    private BigDecimal interestPayment;
    @JoinColumn(name = "principal_payment")
    private BigDecimal principalPayment;

    public ScheduledPayment toSchedulePayment() {
        return ScheduledPayment.newBuilder()
                .setStatus(PaymentStatus.valueOf(getStatus()))
                .setPaymentDate(Timestamp.newBuilder()
                        .setSeconds(getPaymentDate().toInstant().getEpochSecond())
                        .setNanos(0))
                .setPeriodPayment(getPeriodPayment().toString())
                .setInterestPayment(getInterestPayment().toString())
                .setPrincipalPayment(getPrincipalPayment().toString())
                .setPeriodNumber(getPaymentEntityPK().getPeriodNumber())
                .build();
    }

    @Data
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    @Builder
    @Embeddable
    public static class ScheduledPaymentEntityPK implements Serializable {
        @Column(name = "period_number")
        private Integer periodNumber;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "payment_schedule_id")
        private PaymentScheduleEntity paymentScheduleEntity;
    }
}
