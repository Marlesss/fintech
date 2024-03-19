package unit.com.academy.fintech.pe.core.service.agreement.db.product;

import com.academy.fintech.agreement.Product;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductEntity;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductRepository;
import com.academy.fintech.pe.core.service.agreement.db.product.ProductServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import unit.com.academy.fintech.agreement.RandomAgreement;
import unit.com.academy.fintech.pe.core.service.agreement.AgreementCreationServiceTest;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static unit.TestConfig.RANDOM_TESTS_COUNT;
import static unit.TestConfig.SEED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProductServiceImplTest {
    private static final Random random = new Random(SEED);
    private static final RandomAgreement randomAgreement = new RandomAgreement(random);
    private AutoCloseable mocks;
    @Mock
    private ProductRepository productRepository;
    private ProductServiceImpl productService;

    @BeforeEach
    public void openMocks() {
        mocks = MockitoAnnotations.openMocks(AgreementCreationServiceTest.class);
        productService = new ProductServiceImpl(productRepository);
    }

    @AfterEach
    public void closeMocks() {
        try {
            mocks.close();
        } catch (Exception ignored) {
        }
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testGetAvailableProducts() {
        int countOfProducts = random.nextInt(0, 100);
        List<Product> productList = IntStream.range(0, countOfProducts).boxed().map(i -> randomAgreement.getProduct()).toList();

        when(productRepository.getAvailableProducts()).thenReturn(productList.stream().map(ProductEntity::from).toList());

        assertEquals(productList, productService.getAvailableProducts());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testValidGetByCode() {
        int countOfProducts = random.nextInt(1, 100);
        List<Product> productList = IntStream.range(0, countOfProducts).boxed().map(i -> randomAgreement.getProduct()).toList();
        Product targetProduct = productList.get(random.nextInt(productList.size()));

        when(productRepository.getAvailableProducts()).thenReturn(productList.stream().map(ProductEntity::from).toList());
        when(productRepository.getByCode(any())).thenAnswer(i -> productList.stream().filter(p -> p.getCode().equals(i.getArgument(0))).findAny().map(ProductEntity::from));

        assertEquals(ProductEntity.from(targetProduct), productService.getByCode(targetProduct.getCode()).orElseThrow());
    }

    @RepeatedTest(value = RANDOM_TESTS_COUNT)
    public void testInvalidGetByCode() {
        int countOfProducts = random.nextInt(0, 100);
        List<Product> productList = IntStream.range(0, countOfProducts).boxed().map(i -> randomAgreement.getProduct()).toList();
        String code;
        do {
            code = randomAgreement.getWord(random.nextInt(1, 100));
            String finalCode = code;
            if (productList.stream().noneMatch(p -> p.getCode().equals(finalCode))) {
                break;
            }
        } while (true);

        when(productRepository.getAvailableProducts()).thenReturn(productList.stream().map(ProductEntity::from).toList());
        when(productRepository.getByCode(any())).thenAnswer(i -> productList.stream().filter(p -> p.getCode().equals(i.getArgument(0))).findAny());

        assertTrue(productService.getByCode(code).isEmpty());
    }
}
