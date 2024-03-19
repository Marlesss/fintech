package com.academy.fintech.pe.core.service.agreement.db.agreement;

import com.academy.fintech.agreement.CreateAgreementRequest;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Service to work with {@link AgreementRepository}
 */
public interface AgreementService {
    /**
     * Inserts new inactivated {@link AgreementEntity} in the table.
     *
     * @param request request to form an entity
     * @return id of the inserted {@link AgreementEntity}
     */
    BigInteger insertNewAgreement(CreateAgreementRequest request);

    /**
     * Gets the {@link AgreementEntity} from the table by this id.
     *
     * @param id id of the {@link AgreementEntity} to get
     * @return {@code Optional.of(AgreementEntity)} if the agreement was found, else {@code Optional.empty()}
     */
    Optional<AgreementEntity> getBy(BigInteger id);

    /**
     * Gets the {@link List} of {@link AgreementEntity} associated with client
     *
     * @param clientId id of the client
     * @return {@link List} of {@link AgreementEntity} associated with client
     */
    List<AgreementEntity> getAgreementsByClientId(BigInteger clientId);

    /**
     * Saves passed {@link AgreementEntity} in the table
     *
     * @param agreementEntity to save
     */
    void save(AgreementEntity agreementEntity);
}
