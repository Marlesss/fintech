package unit.com.academy.fintech.pe.core.service.agreement.db.agreement;

import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementRepository;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import unit.com.academy.fintech.agreement.RandomAgreement;
import unit.com.academy.fintech.pe.core.service.agreement.AgreementCreationServiceTest;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static unit.TestConfig.RANDOM_TESTS_COUNT;
import static unit.TestConfig.SEED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AgreementServiceImplTest {
    private static final Random random = new Random(SEED);
    private static final RandomAgreement randomAgreement = new RandomAgreement(random);
    private AutoCloseable mocks;
    @Mock
    private AgreementRepository agreementRepository;
    @Captor
    private ArgumentCaptor<CreateAgreementRequest> createAgreementRequestCaptor;
    @Captor
    private ArgumentCaptor<AgreementEntity> agreementEntityCaptor;


    private AgreementServiceImpl agreementService;

    @BeforeEach
    public void openMocks() {
        mocks = MockitoAnnotations.openMocks(AgreementCreationServiceTest.class);
        agreementService = new AgreementServiceImpl(agreementRepository);
    }

    @AfterEach
    public void closeMocks() {
        try {
            mocks.close();
        } catch (Exception ignored) {
        }
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testInsertNewAgreement() {
        int agreementId = abs(random.nextInt());
        CreateAgreementRequest createAgreementRequest = randomAgreement.getCreateAgreementRequest();
        when(agreementRepository.insertNewAgreement(any())).thenReturn(BigInteger.valueOf(agreementId));

        assertEquals(BigInteger.valueOf(agreementId), agreementService.insertNewAgreement(createAgreementRequest));

        verify(agreementRepository).insertNewAgreement(createAgreementRequestCaptor.capture());
        assertEquals(createAgreementRequest, createAgreementRequestCaptor.getValue());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetByPresentAgreementEntity() {
        int agreementId = abs(random.nextInt());
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId);
        when(agreementRepository.findById(BigInteger.valueOf(agreementId))).thenReturn(Optional.of(agreementEntity));

        assertEquals(agreementEntity, agreementService.getBy(BigInteger.valueOf(agreementId)).orElseThrow());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetByUnknownAgreementEntity() {
        int agreementId = abs(random.nextInt());
        when(agreementRepository.findById(BigInteger.valueOf(agreementId))).thenReturn(Optional.empty());

        assertTrue(agreementService.getBy(BigInteger.valueOf(agreementId)).isEmpty());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testSave() {
        int agreementId = abs(random.nextInt());
        AgreementEntity agreementEntity = randomAgreement.getAgreementEntity(agreementId);

        agreementService.save(agreementEntity);

        verify(agreementRepository).save(agreementEntityCaptor.capture());
        assertEquals(agreementEntity, agreementEntityCaptor.getValue());
    }
}
