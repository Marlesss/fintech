package com.academy.fintech.pe.core.service.agreement.db.agreement;

import com.academy.fintech.agreement.CreateAgreementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Repository for {@code agreement} table
 */
@Repository
public interface AgreementRepository extends JpaRepository<AgreementEntity, BigInteger> {

    /**
     * Inserts new inactivated {@link AgreementEntity} in the table.
     *
     * @param request request to form an entity
     * @return id of the inserted {@link AgreementEntity}
     */
    default BigInteger insertNewAgreement(CreateAgreementRequest request) {
        return save(AgreementEntity.builder()
                .productCode(request.getProduct().getCode())
                .clientId(new BigInteger(request.getClientId()))
                .term(request.getProduct().getLoanTerm())
                .interest(new BigDecimal(request.getProduct().getInterest()))
                .principalAmount(request.getProduct().getOriginationAmount() + request.getProduct().getDisbursementAmount())
                .originationAmount(request.getProduct().getOriginationAmount())
                .status("NEW")
                .build()).getId();
    }

    List<AgreementEntity> findAllByClientId(BigInteger clientId);
}
