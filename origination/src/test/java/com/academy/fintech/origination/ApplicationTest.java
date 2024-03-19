package com.academy.fintech.origination;

import com.academy.fintech.agreement.AgreementResponse;
import com.academy.fintech.agreement.Product;
import com.academy.fintech.application.ApplicationRequest;
import com.academy.fintech.application.ApplicationResponse;
import com.academy.fintech.application.ApplicationServiceGrpc;
import com.academy.fintech.application.CancelRequest;
import com.academy.fintech.application.ClientData;
import com.academy.fintech.disbursement.DisbursementRequest;
import com.academy.fintech.origination.core.agreement.client.grpc.AgreementGrpcClient;
import com.academy.fintech.origination.core.db.application.ApplicationEntity;
import com.academy.fintech.origination.core.db.application.ApplicationRepository;
import com.academy.fintech.origination.core.db.client.ClientRepository;
import com.academy.fintech.origination.core.disbursement.client.grpc.DisbursementGrpcClient;
import com.academy.fintech.origination.core.email.ResponseSender;
import com.academy.fintech.origination.core.scoring.client.grpc.ScoringGrpcClient;
import com.academy.fintech.scoring.ScoringRequest;
import com.academy.fintech.scoring.ScoringResponse;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
        , properties = {"spring.datasource.port=5436"})
@ExtendWith(SpringExtension.class)
public class ApplicationTest {
    @Value("${grpc.port}")
    private int applicationServiceGrpcPort;
    @Container
    static DockerComposeContainer<?> postgresDockerContainer = new DockerComposeContainer<>(new File("../docker-compose.yml"))
            .withEnv("DB_PORT", "5436")
            .withEnv("ADMINER_PORT", "8081");
    @MockBean
    private AgreementGrpcClient agreementGrpcClient;
    @MockBean
    private DisbursementGrpcClient disbursementGrpcClient;
    @MockBean
    private ScoringGrpcClient scoringGrpcClient;
    @MockBean(reset = MockReset.BEFORE)
    private ResponseSender responseSender;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private ClientRepository clientRepository;
    private ApplicationServiceGrpc.ApplicationServiceBlockingStub stub;
    private final ClientData finalClient = ClientData.newBuilder()
            .setFirstName("firstName")
            .setLastName("lastName")
            .setEmail("final_client@domen.com")
            .setSalary(40000)
            .build();
    private final ApplicationRequest finalApplicationRequest = ApplicationRequest.newBuilder()
            .setClientData(finalClient)
            .setDisbursementAmount(100000)
            .build();

    private final Product finalProduct = Product.newBuilder()
            .setCode("CL1.0")
            .setMinLoanTerm(6)
            .setMaxLoanTerm(12)
            .setMinPrincipalAmount(10000)
            .setMaxPrincipalAmount(200000)
            .setMinInterest("1.0")
            .setMaxInterest("10.0")
            .setMinOriginationAmount(1000)
            .setMaxOriginationAmount(10000)
            .build();

    @AfterAll
    static void closeContainer() {
        postgresDockerContainer.close();
    }

    @BeforeEach
    void initStub() {
        Channel channel = ManagedChannelBuilder.forAddress("localhost", applicationServiceGrpcPort).usePlaintext().build();
        stub = ApplicationServiceGrpc.newBlockingStub(channel);
    }

    @BeforeEach
    void addProducts() {
        when(agreementGrpcClient.getProducts()).thenReturn(List.of(finalProduct));
    }

    @AfterEach
    void clearDB() {
        applicationRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void testCreate() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        ApplicationResponse response = stub.create(finalApplicationRequest);
        assertNotNull(response);
        assertTrue(Integer.parseInt(response.getApplicationId()) >= 0);
    }

    private void testClientWithEqualEmailDifferentData(ClientData anotherClient) {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        ApplicationRequest anotherRequest = finalApplicationRequest.toBuilder().setClientData(anotherClient).build();
        stub.create(finalApplicationRequest);
        assertThrowsSRE(Status.INVALID_ARGUMENT, () -> stub.create(anotherRequest));
    }

    @Test
    void testClientWithEqualEmailDifferentFirstName() {
        testClientWithEqualEmailDifferentData(finalClient.toBuilder().setFirstName("Steve").build());
    }

    @Test
    void testClientWithEqualEmailDifferentLastName() {
        testClientWithEqualEmailDifferentData(finalClient.toBuilder().setLastName("Jobs").build());
    }

    @Test
    void testCreateWithNegativeDisbursementAmount() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        assertThrowsSRE(Status.NOT_FOUND,
                () -> stub.create(finalApplicationRequest.toBuilder().setDisbursementAmount(-40000).build()));
    }

    @Test
    void testCreateWithNoSatisfyingProduct() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        assertThrowsSRE(Status.NOT_FOUND,
                () -> stub.create(finalApplicationRequest.toBuilder().setDisbursementAmount((int) 1e10).build()));
    }

    @Test
    void testScoringIsCalling() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        stub.create(finalApplicationRequest).getApplicationId();
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ArgumentCaptor<ScoringRequest> scoringRequestCaptor = ArgumentCaptor.forClass(ScoringRequest.class);
                    verify(scoringGrpcClient, times(1)).score(scoringRequestCaptor.capture());
                    assertEquals("123", scoringRequestCaptor.getValue().getAgreementId());
                    assertEquals(finalApplicationRequest.getClientData().getSalary(),
                            scoringRequestCaptor.getValue().getClientSalary());
                });
    }

    @Test
    void testAcceptedScoring() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        when(scoringGrpcClient.score(any())).thenReturn(ScoringResponse.newBuilder().setScore(1).build());
        BigInteger applicationId = new BigInteger(stub.create(finalApplicationRequest).getApplicationId());
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(scoringGrpcClient, times(1)).score(any());
                    verify(disbursementGrpcClient, times(1)).disbursement(any());
                });
    }

    @Test
    void testRejectedScoring() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        when(scoringGrpcClient.score(any())).thenReturn(ScoringResponse.newBuilder().setScore(-1).build());
        doAnswer(inv -> {
            ClientData clientData = inv.getArgument(0);
            assertEquals(finalApplicationRequest.getClientData(), clientData);
            return null;
        }).when(responseSender).sendRejected(any());
        BigInteger applicationId = new BigInteger(stub.create(finalApplicationRequest).getApplicationId());
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ArgumentCaptor<ScoringRequest> scoringRequestCaptor = ArgumentCaptor.forClass(ScoringRequest.class);
                    verify(scoringGrpcClient, times(1)).score(scoringRequestCaptor.capture());
                    ApplicationEntity application = applicationRepository.getReferenceById(applicationId);
                    assertEquals("REJECTED", application.getStatus());
                    verify(responseSender, times(1)).sendRejected(any());
                    verifyNoMoreInteractions(responseSender);
                });
    }

    @Test
    void testDisbursementIsCalling() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        when(scoringGrpcClient.score(any())).thenReturn(ScoringResponse.newBuilder().setScore(1).build());
        stub.create(finalApplicationRequest).getApplicationId();
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    ArgumentCaptor<DisbursementRequest> disbursementRequestCaptor = ArgumentCaptor.forClass(DisbursementRequest.class);
                    verify(disbursementGrpcClient, times(1)).disbursement(disbursementRequestCaptor.capture());
                    assertEquals(finalApplicationRequest.getDisbursementAmount(),
                            disbursementRequestCaptor.getValue().getDisbursementAmount());
                    assertEquals("123", disbursementRequestCaptor.getValue().getAgreementId());
                });
    }

    @Test
    void testSuccessfulDisbursement() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        when(scoringGrpcClient.score(any())).thenReturn(ScoringResponse.newBuilder().setScore(1).build());
        doAnswer(inv -> {
            ClientData clientData = inv.getArgument(0);
            assertEquals(finalApplicationRequest.getClientData(), clientData);
            return null;
        }).when(responseSender).sendAccepted(any());
        BigInteger applicationId = new BigInteger(stub.create(finalApplicationRequest).getApplicationId());
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(disbursementGrpcClient, times(1)).disbursement(any());
                    ApplicationEntity application = applicationRepository.getReferenceById(applicationId);
                    assertEquals("ACTIVE", application.getStatus());
                    verify(responseSender, times(1)).sendAccepted(any());
                    verifyNoMoreInteractions(responseSender);
                });
    }

    @Test
    void testCancelNew() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        String applicationId = stub.create(finalApplicationRequest).getApplicationId();
        stub.cancel(CancelRequest.newBuilder().setApplicationId(applicationId).build());
        ApplicationEntity application = applicationRepository.getReferenceById(new BigInteger(applicationId));
        assertEquals("CANCELLED", application.getStatus());
    }

    @Test
    void testCancelScoring() {
        when(agreementGrpcClient.createAgreement(any())).thenReturn(AgreementResponse.newBuilder().setAgreementId("123").build());
        when(scoringGrpcClient.score(any())).thenAnswer(inv -> {
            try {
                sleep((long) 1e5);
            } catch (Exception ignored) {
            }
            return ScoringResponse.newBuilder().setScore(1).build();
        });
        String applicationId = stub.create(finalApplicationRequest).getApplicationId();
        stub.cancel(CancelRequest.newBuilder().setApplicationId(applicationId).build());
        ApplicationEntity application = applicationRepository.getReferenceById(new BigInteger(applicationId));
        assertEquals("CANCELLED", application.getStatus());
    }

    private static void assertThrowsSRE(Status status, Runnable runnable) {
        try {
            runnable.run();
            fail("StatusRuntimeException with status " + status + " expected");
        } catch (StatusRuntimeException e) {
            assertSame(status.getCode(), e.getStatus().getCode());
        }
    }
}
