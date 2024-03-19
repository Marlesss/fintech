package unit.com.academy.fintech.pe.core.service.agreement.db.schedule;

import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleEntity;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleRepository;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleServiceImpl;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentEntity;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentService;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import unit.com.academy.fintech.agreement.RandomAgreement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static unit.TestConfig.RANDOM_TESTS_COUNT;
import static unit.TestConfig.SEED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PaymentScheduleServiceImplTest {
    private static final Random random = new Random(SEED);
    private static final RandomAgreement randomAgreement = new RandomAgreement(random);
    @Mock
    private PaymentScheduleRepository paymentScheduleRepository;
    @Mock(answer = Answers.RETURNS_MOCKS)
    private ScheduledPaymentService scheduledPaymentService;
    @InjectMocks
    private PaymentScheduleServiceImpl paymentScheduleService;

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testCreatePaymentSchedule() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId, randomAgreement.getCreateAgreementRequest(), "ACTIVE", randomAgreement.getDate(), null);

        when(paymentScheduleRepository.insertPaymentSchedule(any(), anyInt())).thenAnswer(i -> PaymentScheduleEntity.builder()
                .id(BigInteger.valueOf(random.nextInt()))
                .agreementEntity(i.getArgument(0))
                .version(i.getArgument(1))
                .build());
        PaymentScheduleEntity paymentScheduleEntity = paymentScheduleService.createPaymentSchedule(agreementEntity);
        verify(scheduledPaymentService, times(1)).createScheduledPayments(eq(agreementEntity), eq(agreementEntity.getDisbursementDate()));
        verify(scheduledPaymentService, times(1)).saveScheduledPayments(any(), eq(paymentScheduleEntity));
        verifyNoMoreInteractions(scheduledPaymentService);
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetPaymentScheduleEntity() {
        int agreementId = abs(random.nextInt());
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId);
        PaymentScheduleEntity paymentScheduleEntity = new PaymentScheduleEntity(BigInteger.valueOf(random.nextInt()), agreementEntity, random.nextInt());

        when(paymentScheduleRepository.findTopByAgreementEntityOrderByVersionDesc(agreementEntity)).thenReturn(Optional.of(paymentScheduleEntity));

        assertEquals(paymentScheduleEntity, paymentScheduleService.getPaymentScheduleEntity(agreementEntity).orElseThrow());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetNoPaymentScheduleEntity() {
        int agreementId = abs(random.nextInt());
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId);

        when(paymentScheduleRepository.findTopByAgreementEntityOrderByVersionDesc(agreementEntity)).thenReturn(Optional.empty());

        assertTrue(paymentScheduleService.getPaymentScheduleEntity(agreementEntity).isEmpty());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetScheduledPayments() {
        int agreementId = abs(random.nextInt());
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId);
        PaymentScheduleEntity paymentScheduleEntity = randomAgreement.getPaymentScheduleEntity(agreementEntity);
        List<ScheduledPaymentEntity> scheduledPayments = IntStream.range(1, agreementEntity.getTerm() + 1).boxed()
                .map(i -> ScheduledPaymentEntity.builder()
                        .paymentEntityPK(ScheduledPaymentEntity.ScheduledPaymentEntityPK.builder()
                                .periodNumber(i).build())
                        .status("OVERDUE")
                        .paymentDate(randomAgreement.getDate())
                        .interestPayment(BigDecimal.valueOf(random.nextDouble()))
                        .periodPayment(BigDecimal.valueOf(random.nextDouble()))
                        .principalPayment(BigDecimal.valueOf(random.nextDouble()))
                        .build()).toList();

        when(scheduledPaymentService.getScheduledPayments(paymentScheduleEntity)).thenReturn(scheduledPayments);
        when(paymentScheduleService.getPaymentScheduleEntity(agreementEntity)).thenReturn(Optional.of(paymentScheduleEntity));

        assertEquals(scheduledPayments.stream().map(ScheduledPaymentEntity::toSchedulePayment).toList(),
                paymentScheduleService.getScheduledPayments(agreementEntity).orElseThrow());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetNoScheduledPayments() {
        int agreementId = abs(random.nextInt());
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId);
        when(paymentScheduleService.getPaymentScheduleEntity(agreementEntity)).thenReturn(Optional.empty());

        assertTrue(paymentScheduleService.getScheduledPayments(agreementEntity).isEmpty());
    }
}
