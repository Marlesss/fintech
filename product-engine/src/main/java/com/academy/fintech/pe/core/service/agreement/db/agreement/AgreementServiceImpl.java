package com.academy.fintech.pe.core.service.agreement.db.agreement;

import com.academy.fintech.agreement.CreateAgreementRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link AgreementService} interface
 */
@Service
@RequiredArgsConstructor
public class AgreementServiceImpl implements AgreementService {
    private final AgreementRepository agreementRepository;

    @Override
    public BigInteger insertNewAgreement(CreateAgreementRequest request) {
        return agreementRepository.insertNewAgreement(request);
    }

    @Override
    public Optional<AgreementEntity> getBy(BigInteger id) {
        return agreementRepository.findById(id);
    }

    @Override
    public List<AgreementEntity> getAgreementsByClientId(BigInteger clientId) {
        return agreementRepository.findAllByClientId(clientId);
    }

    @Override
    public void save(AgreementEntity agreementEntity) {
        agreementRepository.save(agreementEntity);
    }
}
