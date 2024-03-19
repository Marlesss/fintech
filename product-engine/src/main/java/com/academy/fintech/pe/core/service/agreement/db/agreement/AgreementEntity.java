package com.academy.fintech.pe.core.service.agreement.db.agreement;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Entity for {@code agreement} table in database
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Entity
@Table(name = "agreement")
public class AgreementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @JoinColumn(name = "product_code")
    private String productCode;

    @JoinColumn(name = "client_id")
    private BigInteger clientId;

    @JoinColumn(name = "term")
    private Integer term;

    @JoinColumn(name = "interest")
    private BigDecimal interest;

    @JoinColumn(name = "principal_amount")
    private Long principalAmount;

    @JoinColumn(name = "origination_amount")
    private Long originationAmount;

    @JoinColumn(name = "status")
    private String status;

    @JoinColumn(name = "disbursement_date")
    private Date disbursementDate;

    @JoinColumn(name = "next_payment_date")
    private Date nextPaymentDate;
}
