package com.academy.fintech.pe;

import com.academy.fintech.agreement.ActivateAgreementRequest;
import com.academy.fintech.agreement.AgreementResponse;
import com.academy.fintech.agreement.AgreementServiceGrpc;
import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.agreement.PaymentScheduleRequest;
import com.academy.fintech.agreement.Product;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementRepository;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductEntity;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductRepository;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleRepository;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentRepository;
import com.google.protobuf.Empty;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import unit.com.academy.fintech.agreement.RandomAgreement;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.academy.fintech.agreement.Mapper.getDateFrom;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Testcontainers
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
        , properties = {"spring.datasource.port=5436"})
public class ApplicationTest {
    private static final int RANDOM_TESTS_COUNT = 10;
    private static final long SEED = 346109321234L;
    private static final Random random = new Random(SEED);
    private static final RandomAgreement randomAgreement = new RandomAgreement(random);
    @Value("${grpc.port}")
    private int agreementServiceGrpcPort;
    private AgreementServiceGrpc.AgreementServiceBlockingStub stub;
    @Container
    static DockerComposeContainer<?> postgresDockerContainer = new DockerComposeContainer<>(new File("../docker-compose.yml"))
            .withEnv("DB_PORT", "5436")
            .withEnv("ADMINER_PORT", "8081");
    @Autowired
    private AgreementRepository agreementRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PaymentScheduleRepository paymentScheduleRepository;
    @Autowired
    private ScheduledPaymentRepository scheduledPaymentRepository;

    @AfterAll
    static void shutdownDockerContainer() {
        postgresDockerContainer.stop();
        postgresDockerContainer.close();
    }

    @BeforeEach
    public void initiateGrpcConnection() {
        Channel channel = ManagedChannelBuilder.forAddress("localhost", agreementServiceGrpcPort).usePlaintext().build();
        stub = AgreementServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void cleanup() {
        scheduledPaymentRepository.deleteAll();
        paymentScheduleRepository.deleteAll();
        agreementRepository.deleteAll();
        productRepository.deleteAll();
        if (stub.getChannel() instanceof ManagedChannel channel && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    @Test
    public void testGetAllNoProducts() {
        assertFalse(stub.getProducts(Empty.getDefaultInstance()).hasNext());
    }

    @Test
    public void testGetAllOneProduct() {
        Product product = randomAgreement.getProduct();
        productRepository.save(ProductEntity.from(product));

        List<Product> productList = new ArrayList<>();
        stub.getProducts(Empty.getDefaultInstance()).forEachRemaining(productList::add);

        assertEquals(List.of(product), productList);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetAllRandomProducts() {
        int countOfProducts = random.nextInt(0, 100);
        Set<Product> products = IntStream.range(0, countOfProducts).boxed().map(i -> randomAgreement.getProduct()).collect(Collectors.toSet());
        productRepository.saveAll(products.stream().map(ProductEntity::from).toList());

        Set<Product> gotProducts = new HashSet<>();
        stub.getProducts(Empty.getDefaultInstance()).forEachRemaining(gotProducts::add);

        assertEquals(products, gotProducts);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testCreateAgreement() {
        Product product = randomAgreement.getProduct();
        productRepository.save(ProductEntity.from(product));
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest(product);

        AgreementResponse agreementResponse = stub.create(createAgreementRequest);
        BigInteger gotAgreementId = new BigInteger(agreementResponse.getAgreementId());
        AgreementEntity expectedAgreement = randomAgreement.getAgreementEntity(gotAgreementId, createAgreementRequest, "NEW", null, null);

        List<AgreementEntity> actualAgreements = agreementRepository.findAll();
        assertEquals(1, actualAgreements.size());
        AgreementEntity actualAgreement = actualAgreements.get(0);
        assertEquals(gotAgreementId, actualAgreement.getId());
        assertEquals(expectedAgreement, actualAgreement);
    }


    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testCreateAgreementNoProduct() {
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest();

        assertThrowsStatusRuntimeException(() -> stub.create(createAgreementRequest), Status.NOT_FOUND);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testInvalidCreateAgreementProduct() {
        Product product = randomAgreement.getProduct();
        productRepository.save(ProductEntity.from(product));
        List<CreateAgreementRequest> corruptedRequests = randomAgreement.getCorruptedCreateAgreementRequests(product);

        for (CreateAgreementRequest createAgreementRequest : corruptedRequests) {
            assertThrowsStatusRuntimeException(() -> stub.create(createAgreementRequest), Status.INVALID_ARGUMENT);
        }
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testActivateAgreement() {
        Product product = randomAgreement.getProduct();
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest(product);
        productRepository.save(ProductEntity.from(product));
        BigInteger agreementId = new BigInteger(stub.create(createAgreementRequest).getAgreementId());
        AgreementEntity createdAgreement = agreementRepository.findById(agreementId).orElseThrow();
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);

        stub.activateAgreement(activateAgreementRequest).forEachRemaining(s -> {
        });

        AgreementEntity activatedAgreement = agreementRepository.findById(agreementId).orElseThrow();
        createdAgreement.setStatus("ACTIVE");
        createdAgreement.setDisbursementDate(activatedAgreement.getDisbursementDate());
        createdAgreement.setNextPaymentDate(getDateFrom(randomAgreement.getScheduledPayments(createdAgreement,
                getDateFrom(activateAgreementRequest.getDisbursementDate())).get(0).getPaymentDate()));
        assertEquals(createdAgreement, activatedAgreement);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testNoSuchIdActivateAgreement() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);

        assertThrowsStatusRuntimeException(() -> stub.activateAgreement(activateAgreementRequest).forEachRemaining(s -> {
                }),
                Status.NOT_FOUND);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testAlreadyActivatedAgreement() {
        Product product = randomAgreement.getProduct();
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest(product);
        productRepository.save(ProductEntity.from(product));
        BigInteger agreementId = new BigInteger(stub.create(createAgreementRequest).getAgreementId());
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);
        stub.activateAgreement(activateAgreementRequest).forEachRemaining(s -> {
        });

        ActivateAgreementRequest activateAgreementRequest2 = randomAgreement.getActivateAgreementRequest(agreementId);
        assertThrowsStatusRuntimeException(() -> stub.activateAgreement(activateAgreementRequest2).forEachRemaining(s -> {
                }),
                Status.INVALID_ARGUMENT);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testPaymentSchedule() {
        Product product = randomAgreement.getProduct();
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest(product);
        productRepository.save(ProductEntity.from(product));
        BigInteger agreementId = new BigInteger(stub.create(createAgreementRequest).getAgreementId());
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);
        List<ScheduledPayment> scheduledPayments1 = new ArrayList<>();
        stub.activateAgreement(activateAgreementRequest).forEachRemaining(scheduledPayments1::add);
        AgreementEntity agreementEntity = agreementRepository.findById(agreementId).orElseThrow();
        validatePaymentSchedule(agreementEntity, scheduledPayments1);
        List<ScheduledPayment> scheduledPayments2 = new ArrayList<>();
        stub.getPaymentSchedule(PaymentScheduleRequest.newBuilder().setAgreementId(agreementId.toString()).build())
                .forEachRemaining(scheduledPayments2::add);
        assertEquals(scheduledPayments1, scheduledPayments2);
        validatePaymentSchedule(agreementEntity, scheduledPayments2);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testNoIdGetPaymentSchedule() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        assertThrowsStatusRuntimeException(() -> stub.getPaymentSchedule(
                PaymentScheduleRequest.newBuilder().setAgreementId(agreementId.toString()).build()).forEachRemaining(s -> {
        }), Status.NOT_FOUND);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testNotActivatedGetPaymentSchedule() {
        Product product = randomAgreement.getProduct();
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest(product);
        productRepository.save(ProductEntity.from(product));
        BigInteger agreementId = new BigInteger(stub.create(createAgreementRequest).getAgreementId());
        assertThrowsStatusRuntimeException(() -> stub.getPaymentSchedule(
                PaymentScheduleRequest.newBuilder().setAgreementId(agreementId.toString()).build()).forEachRemaining(s -> {
        }), Status.INVALID_ARGUMENT);
    }

    private static void validatePaymentSchedule(AgreementEntity agreementEntity, List<ScheduledPayment> scheduledPayments) {
        assertEquals(agreementEntity.getTerm(), scheduledPayments.size());
        BigDecimal principalPaymentsSum = BigDecimal.ZERO;
        BigDecimal totalPaymentSum = BigDecimal.ZERO;
        LocalDate prevPaymentDate = null;
        ScheduledPayment prevScheduledPayment = null;
        for (ScheduledPayment scheduledPayment : scheduledPayments) {
            LocalDate paymentDate = LocalDate.ofInstant(getDateFrom(scheduledPayment.getPaymentDate()).toInstant(), ZoneId.systemDefault());

            principalPaymentsSum = principalPaymentsSum.add(new BigDecimal(scheduledPayment.getPrincipalPayment()));
            totalPaymentSum = totalPaymentSum.add(new BigDecimal(scheduledPayment.getPeriodPayment()));

            if (prevPaymentDate != null) {
                assertTrue((paymentDate.getDayOfMonth() == paymentDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()
                        || ChronoUnit.DAYS.between(paymentDate.plusMonths(1), prevPaymentDate) < 1));
            }
            prevPaymentDate = paymentDate;

            if (prevScheduledPayment != null) {
                assertEquals(prevScheduledPayment.getPeriodNumber() + 1, scheduledPayment.getPeriodNumber());
            }
            prevScheduledPayment = scheduledPayment;
        }
        assertTrue(principalPaymentsSum.add(BigDecimal.valueOf(agreementEntity.getPrincipalAmount())).compareTo(BigDecimal.ONE) <= 0);
        assertTrue(totalPaymentSum.abs().compareTo(BigDecimal.valueOf(agreementEntity.getPrincipalAmount())) >= 0);
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
