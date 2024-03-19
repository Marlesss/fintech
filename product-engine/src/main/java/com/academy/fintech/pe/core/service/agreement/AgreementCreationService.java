package com.academy.fintech.pe.core.service.agreement;

import com.academy.fintech.agreement.ActivateAgreementRequest;
import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.agreement.Mapper;
import com.academy.fintech.agreement.PaymentScheduleRequest;
import com.academy.fintech.agreement.ProductRequest;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementService;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductEntity;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductService;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleService;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentEntity;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Service responsible for creating, activating and working with agreements
 *
 * @see com.academy.fintech.pe.core.service.agreement.grpc.agreement.v1.AgreementController
 */
@Service
@RequiredArgsConstructor
public class AgreementCreationService {
    private final AgreementService agreementService;
    private final ProductService productService;
    private final PaymentScheduleService paymentScheduleService;
    private final ScheduledPaymentService scheduledPaymentService;

    /**
     * Creates agreement and saves it in database
     *
     * @param request request containing information on the creating agreement
     * @return id of the created agreement
     * @throws NoSuchElementException   if unknown product code {@code request.getProduct().getCode()} passed
     * @throws IllegalArgumentException if passed product's arguments doesn't satisfy the requirements of product.
     * @see CreateAgreementRequest
     * @see com.academy.fintech.agreement.Product
     */
    public BigInteger createAgreement(CreateAgreementRequest request) throws NoSuchElementException, IllegalArgumentException {
        if (!validateProductRequest(request.getProduct())) {
            throw new IllegalArgumentException(String.format("Wrong parameters of passed product request. Check the requirements of chosen product (%s).", request.getProduct().getCode()));
        }
        return agreementService.insertNewAgreement(request);
    }

    private boolean validateProductRequest(ProductRequest productRequest) throws NoSuchElementException {
        ProductEntity product = productService.getByCode(productRequest.getCode()).orElseThrow(() -> new NoSuchElementException(String.format("Unknown product code passed (%s)", productRequest.getCode())));

        long principalAmount = productRequest.getOriginationAmount() + productRequest.getDisbursementAmount();
        return (product.getMinTerm() <= productRequest.getLoanTerm() && productRequest.getLoanTerm() <= product.getMaxTerm()
                && product.getMinPrincipalAmount() <= principalAmount && principalAmount <= product.getMaxPrincipalAmount()
                && product.getMinInterest().compareTo(new BigDecimal(productRequest.getInterest())) <= 0 && new BigDecimal(productRequest.getInterest()).compareTo(product.getMaxInterest()) <= 0
                && product.getMinOriginationAmount() <= productRequest.getOriginationAmount() && productRequest.getOriginationAmount() <= product.getMaxOriginationAmount());
    }

    /**
     * Activates agreement and makes the first payment schedule
     *
     * @param request request containing information on the activating agreement
     * @return Payment schedule (ordered by period number)
     * @throws NoSuchElementException   if there is no agreement with passed id
     * @throws IllegalArgumentException if this agreement couldn't be activated (it's already activated or closed)
     * @see ScheduledPayment
     */
    public List<ScheduledPayment> activateAgreement(ActivateAgreementRequest request) throws NoSuchElementException, IllegalArgumentException {
        AgreementEntity agreementEntity = agreementService.getBy(new BigInteger(request.getAgreementId())).orElseThrow();
        if (!Objects.equals(agreementEntity.getStatus(), "NEW")) {
            throw new IllegalArgumentException(String.format("Agreement %d has already been activated", agreementEntity.getId()));
        }
        agreementEntity.setDisbursementDate(Mapper.getDateFrom(request.getDisbursementDate()));
        agreementEntity.setStatus("ACTIVE");
        agreementService.save(agreementEntity);
        paymentScheduleService.createPaymentSchedule(agreementEntity);
        List<ScheduledPayment> paymentSchedule = getPaymentScheduleByAgreementEntity(agreementEntity);
        agreementEntity.setNextPaymentDate(Mapper.getDateFrom(paymentSchedule.get(0).getPaymentDate()));
        agreementService.save(agreementEntity);
        return paymentSchedule;
    }

    /**
     * Gets payment schedule of active agreement
     *
     * @param request request containing information on the agreement
     * @return Payment schedule (ordered by period number)
     * @throws NoSuchElementException   if there is no agreement with passed id
     * @throws IllegalArgumentException if this agreement has no payment schedule
     * @see ScheduledPayment
     */
    public List<ScheduledPayment> getPaymentSchedule(PaymentScheduleRequest request) throws NoSuchElementException, IllegalArgumentException {
        return getPaymentScheduleByAgreementEntity(agreementService.getBy(new BigInteger(request.getAgreementId())).orElseThrow());
    }

    /**
     * Gets approximated payment schedule for agreement.
     * Does not saves it.
     *
     * @param agreementId id of agreement to make payment schedule for
     * @return Payment schedule (ordered by period number)
     * @throws NoSuchElementException if there is no agreement with passed id
     */
    public List<ScheduledPayment> getApproximatedPaymentSchedule(BigInteger agreementId) throws NoSuchElementException {
        AgreementEntity agreement = agreementService.getBy(agreementId).orElseThrow();
        return scheduledPaymentService.createScheduledPayments(agreement, Date.from(Instant.now()))
                .stream().map(ScheduledPaymentEntity::toSchedulePayment).toList();
    }

    /**
     * Gets client's agreements ids
     *
     * @param clientId id of the client to get agreements for
     */
    public List<String> getAgreementsIds(BigInteger clientId) {
        return agreementService.getAgreementsByClientId(clientId).stream().map(AgreementEntity::getId).map(BigInteger::toString).toList();
    }

    private List<ScheduledPayment> getPaymentScheduleByAgreementEntity(AgreementEntity agreementEntity) throws IllegalArgumentException {
        return paymentScheduleService.getScheduledPayments(agreementEntity).orElseThrow(() -> new IllegalArgumentException("This agreement has no scheduled payment yet"));
    }

}
