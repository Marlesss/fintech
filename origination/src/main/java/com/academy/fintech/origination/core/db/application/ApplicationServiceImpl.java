package com.academy.fintech.origination.core.db.application;

import com.academy.fintech.origination.core.db.client.ClientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private static final List<String> CANCELLABLE = List.of("NEW", "SCORING");
    private final ApplicationRepository applicationRepository;

    @Override
    public ApplicationEntity create(ClientEntity clientEntity, BigInteger agreementId, int disbursementAmount) {
        return applicationRepository.save(ApplicationEntity.builder()
                .clientEntity(clientEntity)
                .agreementId(agreementId)
                .disbursementAmount(disbursementAmount)
                .status("NEW")
                .build());
    }

    @Override
    public ApplicationEntity getById(BigInteger applicationId) throws NoSuchElementException {
        try {
            return applicationRepository.getReferenceById(applicationId);
        } catch (RuntimeException e) {
            throw new NoSuchElementException("No application with such id", e);
        }
    }

    @Override
    public Iterable<ApplicationEntity> getNew() {
        return applicationRepository.findAllByStatus("NEW");
    }

    @Override
    public void reject(ApplicationEntity application) {
        application.setStatus("REJECTED");
        applicationRepository.save(application);
    }

    @Override
    public void accept(ApplicationEntity application) {
        application.setStatus("ACCEPTED");
        applicationRepository.save(application);
    }

    @Override
    public void scoring(ApplicationEntity application) {
        application.setStatus("SCORING");
        applicationRepository.save(application);
    }

    @Override
    public void active(ApplicationEntity application) {
        application.setStatus("ACTIVE");
        applicationRepository.save(application);
    }

    @Override
    public void cancel(ApplicationEntity application) throws IllegalArgumentException {
        if (CANCELLABLE.contains(application.getStatus())) {
            application.setStatus("CANCELLED");
            applicationRepository.save(application);
        } else {
            throw new IllegalArgumentException("This application couldn't be cancelled");
        }
    }

    @Override
    public boolean isCancelled(ApplicationEntity application) {
        return application.getStatus().equals("CANCELLED");
    }
}
