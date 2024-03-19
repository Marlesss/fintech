package com.academy.fintech.origination.core;

import com.academy.fintech.application.ApplicationRequest;
import com.academy.fintech.application.ClientData;
import com.academy.fintech.origination.core.agreement.client.AgreementClientService;
import com.academy.fintech.origination.core.db.application.ApplicationEntity;
import com.academy.fintech.origination.core.db.application.ApplicationService;
import com.academy.fintech.origination.core.db.client.ClientEntity;
import com.academy.fintech.origination.core.db.client.ClientService;
import com.academy.fintech.origination.core.disbursement.client.DisbursementClientService;
import com.academy.fintech.origination.core.email.ResponseSender;
import com.academy.fintech.origination.core.scoring.client.ScoringClientService;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.NoSuchElementException;

/**
 * Service responsible for creating and handling applications.
 */
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ApplicationCreationService {
    private final ClientService clientService;
    private final ApplicationService applicationService;
    private final AgreementClientService agreementClientService;
    private final ScoringClientService scoringClientService;
    private final DisbursementClientService disbursementClientService;
    private final ResponseSender responseSender;

    /**
     * Создает заявку на кредит.
     * Перед созданием заявки выбирает подходящий под требования клиента продукт среди доступных в product-engine.
     *
     * @param applicationRequest запрос на создание заявки
     * @return applicationId созданной заявки
     * @throws NoSuchElementException   если не был найден продукт, удовлетворяющий условиям
     * @throws IllegalArgumentException если переданы неправильные персональные данные существующего клиента
     */
    public BigInteger create(ApplicationRequest applicationRequest) throws NoSuchElementException, IllegalArgumentException {
        ClientEntity client = clientService.getOrCreate(applicationRequest.getClientData());
        try {
            BigInteger agreementId = agreementClientService.createAgreement(client.getId(),
                    applicationRequest.getDisbursementAmount());
            ApplicationEntity application = applicationService.create(client, agreementId,
                    applicationRequest.getDisbursementAmount());
            return application.getId();
        } catch (NoSuchElementException e) {
            responseSender.sendRejected(client.to());
            throw e;
        }
    }

    /**
     * Отправляет на проверку в scoring все новые заявки
     */
    @Scheduled(fixedRate = 1000)
    public void checkScore() {
        applicationService.getNew().forEach(application -> {
            applicationService.scoring(application);
            int score;
            try {
                score = scoringClientService.score(
                        application.getClientEntity().getId(),
                        application.getAgreementId(),
                        application.getClientEntity().getSalary());
            } catch (StatusRuntimeException e) {
                log.error("Error occurred while scoring agreement", e);
                applicationService.reject(application);
                return;
            }
            if (applicationService.isCancelled(application)) {
                return;
            }
            ClientData clientData = application.getClientEntity().to();
            if (score < 0) {
                applicationService.reject(application);
                responseSender.sendRejected(clientData);

            } else {
                applicationService.accept(application);
                responseSender.sendAccepted(clientData);
                disbursementClientService.disbursement(application);
                applicationService.active(application);
            }
        });
    }

    /**
     * Отменяет заявку
     *
     * @param applicationId {@code applicationId} заявки, которую нужно отменить
     * @throws NoSuchElementException   если не была найдена заявка с переданным {@code applicationId}
     * @throws IllegalArgumentException если заявку невозможно отклонить
     */
    public void cancel(BigInteger applicationId) throws NoSuchElementException, IllegalArgumentException {
        ApplicationEntity application = applicationService.getById(applicationId);
        applicationService.cancel(application);
    }
}
