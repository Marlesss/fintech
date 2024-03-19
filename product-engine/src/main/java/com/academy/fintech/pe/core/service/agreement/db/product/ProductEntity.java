package com.academy.fintech.pe.core.service.agreement.db.product;

import com.academy.fintech.agreement.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity for {@code product} table in database
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Entity
@Table(name = "product")
public class ProductEntity {
    @Id
    private String code;

    @JoinColumn(name = "min_term")
    private int minTerm;

    @JoinColumn(name = "max_term")
    private int maxTerm;

    @JoinColumn(name = "min_principal_amount")
    private long minPrincipalAmount;

    @JoinColumn(name = "max_principal_amount")
    private long maxPrincipalAmount;

    @JoinColumn(name = "min_interest")
    private BigDecimal minInterest;

    @JoinColumn(name = "max_interest")
    private BigDecimal maxInterest;

    @JoinColumn(name = "min_origination_amount")
    private long minOriginationAmount;

    @JoinColumn(name = "max_origination_amount")
    private long maxOriginationAmount;

    public static ProductEntity from(Product product) {
        return ProductEntity.builder()
                .code(product.getCode())
                .minTerm(product.getMinLoanTerm())
                .maxTerm(product.getMaxLoanTerm())
                .minPrincipalAmount(product.getMinPrincipalAmount())
                .maxPrincipalAmount(product.getMaxPrincipalAmount())
                .minInterest(new BigDecimal(product.getMinInterest()))
                .maxInterest(new BigDecimal(product.getMaxInterest()))
                .minOriginationAmount(product.getMinOriginationAmount())
                .maxOriginationAmount(product.getMaxOriginationAmount())
                .build();
    }

    public Product toProduct() {
        return Product.newBuilder()
                .setCode(getCode())
                .setMinLoanTerm(getMinTerm())
                .setMaxLoanTerm(getMaxTerm())
                .setMinPrincipalAmount(getMinPrincipalAmount())
                .setMaxPrincipalAmount(getMaxPrincipalAmount())
                .setMinInterest(getMinInterest().toString())
                .setMaxInterest(getMaxInterest().toString())
                .setMinOriginationAmount(getMinOriginationAmount())
                .setMaxOriginationAmount(getMaxOriginationAmount())
                .build();
    }
}
