package com.academy.fintech.scoring;

import com.academy.fintech.agreement.AgreementResponse;
import com.academy.fintech.agreement.PaymentScheduleRequest;
import com.academy.fintech.agreement.PaymentStatus;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.scoring.core.agreement.client.grpc.AgreementGrpcClient;
import com.google.protobuf.Timestamp;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {
    private static final ScheduledPayment PAID = ScheduledPayment.getDefaultInstance().toBuilder().setStatus(PaymentStatus.PAID).build(),
            FUTURE = ScheduledPayment.getDefaultInstance().toBuilder().setStatus(PaymentStatus.FUTURE).build(),
            OVERDUE_SMALL = ScheduledPayment.getDefaultInstance().toBuilder().setStatus(PaymentStatus.OVERDUE)
                    .setPaymentDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() - 5 * 24 * 60 * 60).build()).build(),
            OVERDUE_BIG = ScheduledPayment.getDefaultInstance().toBuilder().setStatus(PaymentStatus.OVERDUE)
                    .setPaymentDate(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond() - 10 * 24 * 60 * 60).build()).build();

    private static final ScoringRequest DEFAULT_SCORING_REQUEST = ScoringRequest.newBuilder()
            .setAgreementId("1")
            .setClientId("1")
            .setClientSalary(100).build();
    @MockBean(reset = MockReset.BEFORE)
    private AgreementGrpcClient agreementGrpcClient;
    @Value("${grpc.port}")
    private int scoringServiceGrpcPort;
    ScoringServiceGrpc.ScoringServiceBlockingStub stub;

    @BeforeEach
    void initGrpcStub() {
        Channel channel = ManagedChannelBuilder.forAddress("localhost", scoringServiceGrpcPort).usePlaintext().build();
        stub = ScoringServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void cleanup() {
        if (stub.getChannel() instanceof ManagedChannel channel && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    @Test
    void testScoreUnknownAgreementId() {
        when(agreementGrpcClient.getApproximatedPaymentSchedule(any())).thenThrow(new StatusRuntimeException(Status.NOT_FOUND));
        assertThrowsStatusRuntimeException(() -> stub.score(DEFAULT_SCORING_REQUEST),
                Status.NOT_FOUND);
    }

    @Test
    void testScoreWithLowSalaryNoLoans() {
        mockTooHighPeriodPayment();
        mockNoLoans();
        assertEquals(1, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    @Test
    void testScoreWithHighSalaryNoLoans() {
        mockLowPeriodPayment();
        mockNoLoans();
        assertEquals(2, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    @Test
    void testScoreWithLowSalaryNoDebts() {
        mockTooHighPeriodPayment();
        mockNoDebts();
        assertEquals(1, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    @Test
    void testScoreWithHighSalaryNoDebts() {
        mockLowPeriodPayment();
        mockNoDebts();
        assertEquals(2, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    @Test
    void testScoreWithLowSalarySmallDebts() {
        mockTooHighPeriodPayment();
        mockSmallDebts();
        assertEquals(0, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    @Test
    void testScoreWithHighSalarySmallDebts() {
        mockLowPeriodPayment();
        mockSmallDebts();
        assertEquals(1, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    @Test
    void testScoreWithLowSalaryBigDebts() {
        mockTooHighPeriodPayment();
        mockBigDebts();
        assertEquals(-1, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    @Test
    void testScoreWithHighSalaryBigDebts() {
        mockLowPeriodPayment();
        mockBigDebts();
        assertEquals(0, stub.score(DEFAULT_SCORING_REQUEST).getScore());
    }

    private void mockTooHighPeriodPayment() {
        when(agreementGrpcClient.getApproximatedPaymentSchedule(any())).thenAnswer(inv ->
                List.of(ScheduledPayment.getDefaultInstance().toBuilder().setPeriodPayment("34.123").build()).iterator());
    }

    private void mockLowPeriodPayment() {
        when(agreementGrpcClient.getApproximatedPaymentSchedule(any())).thenAnswer(inv ->
                List.of(ScheduledPayment.getDefaultInstance().toBuilder().setPeriodPayment("32.123").build()).iterator());
    }

    private void mockNoLoans() {
        when(agreementGrpcClient.getAgreements(any())).thenAnswer(inv -> List.<AgreementResponse>of().iterator());
    }

    private void mockNoDebts() {
        when(agreementGrpcClient.getAgreements(any())).thenAnswer(inv -> List.of(
                AgreementResponse.newBuilder().setAgreementId("2").build(),
                AgreementResponse.newBuilder().setAgreementId("3").build(),
                AgreementResponse.newBuilder().setAgreementId("4").build()
        ).iterator());
        when(agreementGrpcClient.getPaymentSchedule(any())).thenAnswer(inv -> List.of(
                PAID, PAID, PAID, FUTURE, FUTURE, FUTURE
        ).iterator());
    }

    private void mockSmallDebts() {
        when(agreementGrpcClient.getAgreements(any())).thenAnswer(inv -> List.of(
                AgreementResponse.newBuilder().setAgreementId("2").build(),
                AgreementResponse.newBuilder().setAgreementId("3").build(),
                AgreementResponse.newBuilder().setAgreementId("4").build()
        ).iterator());
        when(agreementGrpcClient.getPaymentSchedule(any())).thenAnswer(inv -> List.of(
                PAID, PAID, PAID, FUTURE, FUTURE, FUTURE
        ).iterator());
        when(agreementGrpcClient.getPaymentSchedule(eq(PaymentScheduleRequest.newBuilder().setAgreementId("2").build()))).thenAnswer(inv -> List.of(
                PAID, PAID, PAID, OVERDUE_SMALL, FUTURE, FUTURE
        ).iterator());
    }

    private void mockBigDebts() {
        when(agreementGrpcClient.getAgreements(any())).thenAnswer(inv -> List.of(
                AgreementResponse.newBuilder().setAgreementId("2").build(),
                AgreementResponse.newBuilder().setAgreementId("3").build(),
                AgreementResponse.newBuilder().setAgreementId("4").build()
        ).iterator());
        when(agreementGrpcClient.getPaymentSchedule(any())).thenAnswer(inv -> List.of(
                PAID, PAID, PAID, FUTURE, FUTURE, FUTURE
        ).iterator());
        when(agreementGrpcClient.getPaymentSchedule(eq(PaymentScheduleRequest.newBuilder().setAgreementId("3").build()))).thenAnswer(inv -> List.of(
                PAID, PAID, PAID, OVERDUE_SMALL, FUTURE, FUTURE
        ).iterator());
        when(agreementGrpcClient.getPaymentSchedule(eq(PaymentScheduleRequest.newBuilder().setAgreementId("2").build()))).thenAnswer(inv -> List.of(
                PAID, PAID, PAID, OVERDUE_BIG, OVERDUE_SMALL, FUTURE
        ).iterator());
    }

    private static void assertThrowsStatusRuntimeException(Runnable runnable, Status status) {
        try {
            runnable.run();
            fail("StatusRuntimeException with status " + status + " expected");
        } catch (StatusRuntimeException e) {
            assertSame(status.getCode(), e.getStatus().getCode());
        }
    }

}
