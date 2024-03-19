package com.academy.fintech.scoring.core.agreement.client.grpc;

import com.academy.fintech.agreement.AgreementResponse;
import com.academy.fintech.agreement.AgreementServiceGrpc;
import com.academy.fintech.agreement.ApproximatedPaymentScheduleRequest;
import com.academy.fintech.agreement.GetAgreementsRequest;
import com.academy.fintech.agreement.PaymentScheduleRequest;
import com.academy.fintech.agreement.ScheduledPayment;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Slf4j
@Component
public class AgreementGrpcClient {
    private final AgreementServiceGrpc.AgreementServiceBlockingStub stub;

    public AgreementGrpcClient(AgreementGrpcClientProperty property) {
        Channel channel = ManagedChannelBuilder.forAddress(property.host(), property.port()).usePlaintext().build();
        this.stub = AgreementServiceGrpc.newBlockingStub(channel);
    }

    /**
     * @throws StatusRuntimeException Status.NOT_FOUND, если не найден договор с переданным id
     */
    public Iterator<ScheduledPayment> getApproximatedPaymentSchedule(ApproximatedPaymentScheduleRequest request) throws StatusRuntimeException {
        return stub.getApproximatedPaymentSchedule(request);
    }

    public Iterator<AgreementResponse> getAgreements(GetAgreementsRequest request) {
        return stub.getAgreements(request);
    }

    public Iterator<ScheduledPayment> getPaymentSchedule(PaymentScheduleRequest request) {
        return stub.getPaymentSchedule(request);
    }
}
