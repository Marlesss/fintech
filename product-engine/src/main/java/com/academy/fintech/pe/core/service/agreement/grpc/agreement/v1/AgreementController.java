package com.academy.fintech.pe.core.service.agreement.grpc.agreement.v1;

import com.academy.fintech.agreement.ActivateAgreementRequest;
import com.academy.fintech.agreement.AgreementResponse;
import com.academy.fintech.agreement.AgreementServiceGrpc;
import com.academy.fintech.agreement.ApproximatedPaymentScheduleRequest;
import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.agreement.GetAgreementsRequest;
import com.academy.fintech.agreement.PaymentScheduleRequest;
import com.academy.fintech.agreement.Product;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.AgreementCreationService;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.math.BigInteger;
import java.util.NoSuchElementException;

/**
 * Implementation of {@link AgreementServiceGrpc} controller.
 *
 * @see com.academy.fintech.agreement.AgreementServiceV1
 * @see AgreementCreationService
 * @see ProductService
 */
@Slf4j
@GRpcService
@RequiredArgsConstructor
public class AgreementController extends AgreementServiceGrpc.AgreementServiceImplBase {
    private final AgreementCreationService agreementCreationService;
    private final ProductService productService;

    /**
     * Handles {@code create} gRPC request.
     * Creates agreement and sends back id of the created agreement.
     * <p>
     * Throws:
     * Status.INVALID_ARGUMENT in case of error while creating agreement (for example unsatisfied requirements of product)
     * Status.NOT_FOUND in case of unknown product code passed
     * </p>
     *
     * @param request          request containing information of the creating agreement
     * @param responseObserver observer to send the response to
     */
    @Override
    public void create(CreateAgreementRequest request, StreamObserver<AgreementResponse> responseObserver) {
        log.info("Got create request: {}", request);
        try {
            BigInteger agreementId = agreementCreationService.createAgreement(request);

            responseObserver.onNext(
                    AgreementResponse.newBuilder()
                            .setAgreementId(agreementId.toString())
                            .build()
            );
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.error("Error occurred while creating agreement", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).asException());
        } catch (NoSuchElementException e) {
            log.error("Tried to create agreement with unknown product", e);
            responseObserver.onError(Status.NOT_FOUND.withCause(e).asException());
        }
    }

    /**
     * Handles {@code getProducts} gRPC request.
     * Sends back a stream of available products.
     *
     * @param request          Empty request
     * @param responseObserver observer to send the response to
     * @see com.google.protobuf.Empty
     */
    @Override
    public void getProducts(com.google.protobuf.Empty request, StreamObserver<Product> responseObserver) {
        log.info("Got getProducts request");
        for (Product product : productService.getAvailableProducts()) {
            responseObserver.onNext(product);
        }
        responseObserver.onCompleted();
    }

    /**
     * Handles {@code activateAgreement} gRPC request.
     * Activates agreement and sends back a stream of scheduled payments, ordered by payment period.
     * <p>
     * Throws:
     * Status.INVALID_ARGUMENT in case of error while activating agreement (for example this agreement already was activated)
     * Status.NOT_FOUND in case of unknown agreement id passed
     * </p>
     *
     * @param request          request containing information of the activating agreement
     * @param responseObserver observer to send the response to
     */
    @Override
    public void activateAgreement(ActivateAgreementRequest request, StreamObserver<ScheduledPayment> responseObserver) {
        log.info("Got activateAgreement request: " + request);
        try {
            sendStreamScheduledPayment(agreementCreationService.activateAgreement(request), responseObserver);
        } catch (NoSuchElementException e) {
            log.error("Unknown agreement id passed", e);
            responseObserver.onError(Status.NOT_FOUND.withCause(e).asException());
        } catch (IllegalArgumentException e) {
            log.error("Tried to activate the activated agreement", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).asException());
        }
    }

    /**
     * Handles {@code getPaymentSchedule} gRPC request.
     * Sends back a stream of scheduled payments, ordered by payment period.
     * <p>
     * Throws:
     * Status.INVALID_ARGUMENT in case of error while getting payment schedule (for example this agreement has no payment schedule yet)
     * Status.NOT_FOUND in case of unknown agreement id passed
     * </p>
     *
     * @param request          request containing agreement id
     * @param responseObserver observer to send the response to
     */
    @Override
    public void getPaymentSchedule(PaymentScheduleRequest request, StreamObserver<ScheduledPayment> responseObserver) {
        log.info("Got getPaymentSchedule request: {}", request);
        try {
            sendStreamScheduledPayment(agreementCreationService.getPaymentSchedule(request), responseObserver);
        } catch (NoSuchElementException e) {
            log.error("Unknown agreement id passed", e);
            responseObserver.onError(Status.NOT_FOUND.withCause(e).asException());
        } catch (IllegalArgumentException e) {
            log.error("Tried to get payment schedule of inactive agreement", e);
            responseObserver.onError(Status.INVALID_ARGUMENT.withCause(e).asException());
        }
    }


    /**
     * Handles {@code getApproximatedPaymentSchedule} gRPC request.
     * Sends back a stream of scheduled payments, ordered by payment period.
     * <p>
     * Throws:
     * Status.NOT_FOUND in case of unknown agreement id passed
     * </p>
     *
     * @param request          request containing agreement id
     * @param responseObserver observer to send the response to
     */
    @Override
    public void getApproximatedPaymentSchedule(ApproximatedPaymentScheduleRequest request,
                                               StreamObserver<ScheduledPayment> responseObserver) {
        log.info("Got getApproximatedPaymentSchedule request: {}", request);
        try {
            BigInteger agreementId = new BigInteger(request.getAgreementId());
            sendStreamScheduledPayment(agreementCreationService.getApproximatedPaymentSchedule(agreementId), responseObserver);
        } catch (NoSuchElementException e) {
            log.error("Unknown agreement id passed", e);
            responseObserver.onError(Status.NOT_FOUND.withCause(e).asException());
        }
    }

    /**
     * Handles {@code getAgreements} gRPC request.
     * Sends back a stream of agreements ids, paired with client.
     *
     * @param request          request containing client id
     * @param responseObserver observer to send the response to
     */
    @Override
    public void getAgreements(GetAgreementsRequest request, StreamObserver<AgreementResponse> responseObserver) {
        log.info("Got getAgreements request: {}", request);
        BigInteger clientId = new BigInteger(request.getClientId());
        agreementCreationService.getAgreementsIds(clientId).forEach(agreementId ->
                responseObserver.onNext(AgreementResponse.newBuilder()
                        .setAgreementId(agreementId)
                        .build())
        );
        responseObserver.onCompleted();

    }

    private void sendStreamScheduledPayment(Iterable<ScheduledPayment> scheduledPayments, StreamObserver<ScheduledPayment> responseObserver) {
        for (ScheduledPayment scheduledPayment : scheduledPayments) {
            responseObserver.onNext(scheduledPayment);
        }
        responseObserver.onCompleted();
    }
}
