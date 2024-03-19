package com.academy.fintech.pe.core.service.agreement.db.schedule;

import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;

/**
 * Entity for {@code payment_schedule} table in database
 */
@Data
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Entity
@Table(name = "payment_schedule")
public class PaymentScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @JoinColumn(name = "agreement_number")
    @ManyToOne(fetch = FetchType.EAGER)
    private AgreementEntity agreementEntity;

    @JoinColumn(name = "version")
    private Integer version;
}
