package unit.com.academy.fintech.pe.core.service.agreement.db.scheduled_payment;

import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentEntity;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentRepository;
import com.academy.fintech.pe.core.service.agreement.db.scheduled_payment.ScheduledPaymentServiceImpl;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import unit.com.academy.fintech.agreement.RandomAgreement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static unit.TestConfig.RANDOM_TESTS_COUNT;
import static unit.TestConfig.SEED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScheduledPaymentServiceImplTest {
    private static final Random random = new Random(SEED);
    private static final RandomAgreement randomAgreement = new RandomAgreement(random);
    @Mock
    private ScheduledPaymentRepository scheduledPaymentRepository;
    @InjectMocks
    private ScheduledPaymentServiceImpl scheduledPaymentService;

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    void testCreateScheduledPaymentsCheckPayments() {
        BigInteger agreementId = BigInteger.valueOf(abs(random.nextInt()));
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId, randomAgreement.getCreateAgreementRequest(), "ACTIVE", randomAgreement.getDate(), null);
        List<ScheduledPaymentEntity> scheduledPaymentEntities = scheduledPaymentService.createScheduledPayments(agreementEntity, agreementEntity.getDisbursementDate());

        assertEquals(agreementEntity.getTerm(), scheduledPaymentEntities.size());
        BigDecimal principalPaymentsSum = BigDecimal.ZERO;
        BigDecimal totalPaymentSum = BigDecimal.ZERO;
        LocalDate prevPaymentDate = LocalDate.ofInstant(agreementEntity.getDisbursementDate().toInstant(), ZoneId.systemDefault());
        ScheduledPaymentEntity prevScheduledPaymentEntity = null;
        for (ScheduledPaymentEntity scheduledPaymentEntity : scheduledPaymentEntities) {
            LocalDate paymentDate = LocalDate.ofInstant(scheduledPaymentEntity.getPaymentDate().toInstant(), ZoneId.systemDefault());

            principalPaymentsSum = principalPaymentsSum.add(scheduledPaymentEntity.getPrincipalPayment());
            totalPaymentSum = totalPaymentSum.add(scheduledPaymentEntity.getPeriodPayment());

            assertNotEquals(prevPaymentDate, paymentDate);
            assertTrue((paymentDate.getDayOfMonth() == paymentDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()
                    || ChronoUnit.DAYS.between(paymentDate.plusMonths(1), prevPaymentDate) < 1));
            prevPaymentDate = paymentDate;

            if (prevScheduledPaymentEntity != null) {
                assertEquals(prevScheduledPaymentEntity.getPaymentEntityPK().getPeriodNumber() + 1,
                        scheduledPaymentEntity.getPaymentEntityPK().getPeriodNumber());
            }
            prevScheduledPaymentEntity = scheduledPaymentEntity;
        }
        assertTrue(principalPaymentsSum.add(BigDecimal.valueOf(agreementEntity.getPrincipalAmount())).compareTo(BigDecimal.ONE) <= 0);
        assertTrue(totalPaymentSum.abs().compareTo(BigDecimal.valueOf(agreementEntity.getPrincipalAmount())) >= 0);
    }
}
