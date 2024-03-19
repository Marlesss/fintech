package unit.com.academy.fintech.pe.core.service.agreement;

import com.academy.fintech.agreement.ActivateAgreementRequest;
import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.agreement.Mapper;
import com.academy.fintech.agreement.PaymentScheduleRequest;
import com.academy.fintech.agreement.Product;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.AgreementCreationService;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementService;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductEntity;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductService;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleService;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentService;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import unit.com.academy.fintech.agreement.RandomAgreement;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static unit.TestConfig.RANDOM_TESTS_COUNT;
import static unit.TestConfig.SEED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AgreementCreationServiceTest {
    private static final Random random = new Random(SEED);
    private static final RandomAgreement randomAgreement = new RandomAgreement(random);
    private AutoCloseable mocks;
    @Mock
    private AgreementService agreementService;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentScheduleService paymentScheduleService;
    @Mock
    private ScheduledPaymentService scheduledPaymentService;

    @Captor
    private ArgumentCaptor<AgreementEntity> agreementEntityCaptor;
    @Captor
    private ArgumentCaptor<CreateAgreementRequest> createAgreementRequestCaptor;
    @InjectMocks
    private AgreementCreationService agreementCreationService;

    @Test
    public void testDBAccessCreateAgreement() {
        Product product = randomAgreement.getProduct();
        when(productService.getByCode(product.getCode())).thenReturn(Optional.of(ProductEntity.from(product)));
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest(product);

        when(agreementService.insertNewAgreement(any())).thenReturn(BigInteger.valueOf(random.nextInt()));
        agreementCreationService.createAgreement(createAgreementRequest);

        verify(agreementService).insertNewAgreement(any());
        verify(productService).getByCode(any());
        verifyNoMoreInteractions(agreementService, productService);
        verifyNoInteractions(paymentScheduleService, scheduledPaymentService);
    }

    @Test
    public void testDBAccessActivateAgreement() {
        BigInteger agreementId = BigInteger.valueOf(random.nextInt());
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest();
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);
        when(agreementService.getBy(agreementId)).thenReturn(Optional.of(randomAgreement.getAgreementEntity(
                agreementId, createAgreementRequest, "NEW", null, null)));
        when(paymentScheduleService.createPaymentSchedule(any())).thenAnswer(i -> randomAgreement.getPaymentScheduleEntity(i.getArgument(0)));
        when(paymentScheduleService.getScheduledPayments(any())).thenAnswer(i -> Optional.of(randomAgreement.getScheduledPayments(i.getArgument(0))));
        agreementCreationService.activateAgreement(activateAgreementRequest);
        verify(agreementService).getBy(any());
        verify(agreementService, atMost(2)).save(any());
        verifyNoMoreInteractions(agreementService);
        verify(paymentScheduleService).createPaymentSchedule(any());
        verify(paymentScheduleService).getScheduledPayments(any());
        verifyNoInteractions(productService, scheduledPaymentService);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testValidCreateAgreement() {
        Product product = randomAgreement.getProduct();
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest(product);
        when(productService.getByCode(product.getCode())).thenReturn(Optional.of(ProductEntity.from(product)));
        when(agreementService.insertNewAgreement(any())).thenReturn(BigInteger.valueOf(random.nextInt()));

        agreementCreationService.createAgreement(createAgreementRequest);

        verify(agreementService).insertNewAgreement(createAgreementRequestCaptor.capture());
        assertEquals(createAgreementRequest, createAgreementRequestCaptor.getValue());
    }

    @Test
    public void testWrongProductCreateAgreement() {
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest();
        when(productService.getByCode(any())).thenReturn(Optional.empty());
        when(agreementService.insertNewAgreement(any())).thenReturn(BigInteger.valueOf(random.nextInt()));

        assertThrows(NoSuchElementException.class, () -> agreementCreationService.createAgreement(createAgreementRequest));
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testWrongRequestCreateAgreement() {
        Product product = randomAgreement.getProduct();
        List<CreateAgreementRequest> corruptedRequests = randomAgreement.getCorruptedCreateAgreementRequests(product);
        when(productService.getByCode(product.getCode())).thenReturn(Optional.of(ProductEntity.from(product)));
        when(agreementService.insertNewAgreement(any())).thenReturn(BigInteger.valueOf(random.nextInt()));

        for (CreateAgreementRequest corruptedRequest : corruptedRequests) {
            assertThrows(IllegalArgumentException.class, () -> agreementCreationService.createAgreement(corruptedRequest));
        }
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testValidActivateAgreement() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId, randomAgreement.getCreateAgreementRequest(), "NEW", null, null);
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);
        Date disbursementDate = Mapper.getDateFrom(activateAgreementRequest.getDisbursementDate());

        when(agreementService.getBy(new BigInteger(activateAgreementRequest.getAgreementId()))).thenReturn(Optional.of(agreementEntity));
        when(paymentScheduleService.createPaymentSchedule(any())).thenAnswer(i -> randomAgreement.getPaymentScheduleEntity(i.getArgument(0)));
        when(paymentScheduleService.getScheduledPayments(any())).thenAnswer(i -> Optional.of(randomAgreement.getScheduledPayments(i.getArgument(0), disbursementDate)));

        agreementCreationService.activateAgreement(activateAgreementRequest);

        verify(agreementService, atMost(2)).save(agreementEntityCaptor.capture());
        AgreementEntity savedAgreementEntity = agreementEntityCaptor.getValue();
        AgreementEntity exceptedAgreementEntity = agreementEntity;
        exceptedAgreementEntity.setStatus("ACTIVE");
        exceptedAgreementEntity.setDisbursementDate(disbursementDate);
        exceptedAgreementEntity.setNextPaymentDate(Mapper.getDateFrom(randomAgreement.getScheduledPayments(agreementEntity, disbursementDate).get(0).getPaymentDate()));
        assertEquals(exceptedAgreementEntity, savedAgreementEntity);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testNoSuchIdActivateAgreement() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);
        Date disbursementDate = Mapper.getDateFrom(activateAgreementRequest.getDisbursementDate());

        when(agreementService.getBy(any())).thenReturn(Optional.empty());
        when(paymentScheduleService.createPaymentSchedule(any())).thenAnswer(i -> randomAgreement.getPaymentScheduleEntity(i.getArgument(0)));
        when(paymentScheduleService.getScheduledPayments(any())).thenAnswer(i -> Optional.of(randomAgreement.getScheduledPayments(i.getArgument(0), disbursementDate)));

        assertThrows(NoSuchElementException.class, () -> agreementCreationService.activateAgreement(activateAgreementRequest));
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testAlreadyActivatedActivateAgreement() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId, randomAgreement.getCreateAgreementRequest(), "ACTIVE", randomAgreement.getDate(), randomAgreement.getDate());
        ActivateAgreementRequest activateAgreementRequest = randomAgreement.getActivateAgreementRequest(agreementId);
        Date disbursementDate = Mapper.getDateFrom(activateAgreementRequest.getDisbursementDate());

        when(agreementService.getBy(agreementId)).thenReturn(Optional.of(agreementEntity));
        when(paymentScheduleService.createPaymentSchedule(any())).thenAnswer(i -> randomAgreement.getPaymentScheduleEntity(i.getArgument(0)));
        when(paymentScheduleService.getScheduledPayments(any())).thenAnswer(i -> Optional.of(randomAgreement.getScheduledPayments(i.getArgument(0), disbursementDate)));

        assertThrows(IllegalArgumentException.class, () -> agreementCreationService.activateAgreement(activateAgreementRequest));
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testValidGetPaymentSchedule() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId, randomAgreement.getCreateAgreementRequest(), "ACTIVE", randomAgreement.getDate(), null);
        List<ScheduledPayment> scheduledPayments = randomAgreement.getScheduledPayments(agreementEntity, agreementEntity.getDisbursementDate());
        agreementEntity.setNextPaymentDate(Mapper.getDateFrom(scheduledPayments.get(0).getPaymentDate()));
        PaymentScheduleRequest paymentScheduleRequest = PaymentScheduleRequest.newBuilder().setAgreementId(agreementId.toString()).build();

        when(agreementService.getBy(agreementId)).thenReturn(Optional.of(agreementEntity));
        when(paymentScheduleService.getScheduledPayments(agreementEntity)).thenReturn(Optional.of(scheduledPayments));

        assertEquals(scheduledPayments, agreementCreationService.getPaymentSchedule(paymentScheduleRequest));
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testNoSuchIdGetPaymentSchedule() {
        int agreementId = abs(random.nextInt());
        PaymentScheduleRequest paymentScheduleRequest = PaymentScheduleRequest.newBuilder().setAgreementId(BigInteger.valueOf(agreementId).toString()).build();
        when(agreementService.getBy(BigInteger.valueOf(agreementId))).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> agreementCreationService.getPaymentSchedule(paymentScheduleRequest));
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testNotActivatedAgreementGetPaymentSchedule() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId, randomAgreement.getCreateAgreementRequest(), "NEW", null, null);
        PaymentScheduleRequest paymentScheduleRequest = PaymentScheduleRequest.newBuilder().setAgreementId(agreementId.toString()).build();

        when(agreementService.getBy(agreementId)).thenReturn(Optional.of(agreementEntity));

        assertThrows(IllegalArgumentException.class, () -> agreementCreationService.getPaymentSchedule(paymentScheduleRequest));
    }
}
