package unit.com.academy.fintech.agreement;

import com.academy.fintech.agreement.ActivateAgreementRequest;
import com.academy.fintech.agreement.CreateAgreementRequest;
import com.academy.fintech.agreement.Product;
import com.academy.fintech.agreement.ProductRequest;
import com.academy.fintech.agreement.ScheduledPayment;
import com.academy.fintech.pe.core.service.agreement.db.agreement.AgreementEntity;
import com.academy.fintech.pe.core.service.agreement.db.schedule.PaymentScheduleEntity;
import com.google.protobuf.Timestamp;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

/**
 * Class for generating random entities
 */
public class RandomAgreement {
    private static final List<String> AGREEMENT_STATUS = List.of("NEW", "ACTIVE", "CLOSED");
    private final Random random;

    public RandomAgreement(Random random) {
        this.random = random;
    }


    public String getWord(int length) {
        return random.ints('a', 'z' + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public Product getProduct() {
        String code = getWord(random.nextInt(1, 100)) + abs(random.nextInt()) + '.' + abs(random.nextInt());
        int minLoanTerm = random.nextInt(1, 400);
        float minInterest = abs(random.nextFloat());
        int minOriginationAmount = abs(random.nextInt());
        int maxOriginationAmount = random.nextInt(minOriginationAmount, minOriginationAmount + 10000);
        int minPrincipalAmount = random.nextInt(minOriginationAmount + 10000, Integer.MAX_VALUE);
        return Product.newBuilder()
                .setCode(code)
                .setMinLoanTerm(minLoanTerm)
                .setMaxLoanTerm(random.nextInt(minLoanTerm + 1, minLoanTerm + 100))
                .setMinPrincipalAmount(minPrincipalAmount)
                .setMaxPrincipalAmount(random.nextInt(minPrincipalAmount + 1, Integer.MAX_VALUE))
                .setMinInterest(BigDecimal.valueOf(minInterest).toString())
                .setMaxInterest(BigDecimal.valueOf(random.nextFloat((float) (minInterest + 1e-5), minInterest + 10)).toString())
                .setMinOriginationAmount(minOriginationAmount)
                .setMaxOriginationAmount(maxOriginationAmount + 1)
                .build();
    }

    public CreateAgreementRequest getCreateAgreementRequest() {
        return getCreateAgreementRequest(getProduct());
    }

    public CreateAgreementRequest getCreateAgreementRequest(Product product) {
        long originationAmount = random.nextLong(product.getMinOriginationAmount(), product.getMaxOriginationAmount() + 1);
        return CreateAgreementRequest.newBuilder()
                .setClientId(BigInteger.valueOf(abs(random.nextInt())).toString())
                .setProduct(ProductRequest.newBuilder()
                        .setCode(product.getCode())
                        .setLoanTerm(random.nextInt(product.getMinLoanTerm(), product.getMaxLoanTerm()) + 1)
                        .setDisbursementAmount(random.nextLong(product.getMinPrincipalAmount(), product.getMaxPrincipalAmount() + 1) - originationAmount)
                        .setInterest(BigDecimal.valueOf(random.nextDouble(new BigDecimal(product.getMinInterest()).doubleValue(),
                                new BigDecimal(product.getMaxInterest()).doubleValue())).toString())
                        .setOriginationAmount(originationAmount)
                        .build())
                .build();
    }

    public ActivateAgreementRequest getActivateAgreementRequest(BigInteger agreementId) {
        return ActivateAgreementRequest.newBuilder()
                .setAgreementId(agreementId.toString())
                .setDisbursementDate(Timestamp.newBuilder()
                        .setSeconds(random.nextLong(0, Instant.now().getEpochSecond()))
                        .setNanos(0)
                        .build())
                .build();
    }

    public AgreementEntity getAgreementEntity(int agreementId) {
        return getAgreementEntity(BigInteger.valueOf(agreementId), getCreateAgreementRequest(), getAgreementStatus(), getDate(), getDate());
    }

    public AgreementEntity getAgreementEntity(BigInteger agreementId, CreateAgreementRequest createAgreementRequest, String status, Date disbursementDate, Date nextPaymentDate) {
        return AgreementEntity.builder()
                .id(agreementId)
                .productCode(createAgreementRequest.getProduct().getCode())
                .clientId(new BigInteger(createAgreementRequest.getClientId()))
                .term(createAgreementRequest.getProduct().getLoanTerm())
                .interest(new BigDecimal(createAgreementRequest.getProduct().getInterest()))
                .principalAmount(createAgreementRequest.getProduct().getOriginationAmount() + createAgreementRequest.getProduct().getDisbursementAmount())
                .originationAmount(createAgreementRequest.getProduct().getOriginationAmount())
                .status(status)
                .disbursementDate(disbursementDate)
                .nextPaymentDate(nextPaymentDate)
                .build();
    }

    public PaymentScheduleEntity getPaymentScheduleEntity(AgreementEntity agreementEntity) {
        return new PaymentScheduleEntity(BigInteger.valueOf(random.nextInt()), agreementEntity, random.nextInt());
    }

    public List<ScheduledPayment> getScheduledPayments(AgreementEntity agreementEntity) {
        return getScheduledPayments(agreementEntity, Date.from(Instant.now()));
    }

    public List<ScheduledPayment> getScheduledPayments(AgreementEntity agreementEntity, Date disbursementDate) {
        LocalDate paymentDate = disbursementDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return IntStream.range(1, agreementEntity.getTerm() + 1).boxed().map(i ->
                ScheduledPayment.getDefaultInstance().toBuilder()
                        .setPaymentDate(Timestamp.newBuilder()
                                .setSeconds(paymentDate.plusMonths(i).atStartOfDay(ZoneId.systemDefault()).toEpochSecond())
                                .build())
                        .build()).toList();
    }

    public Date getDate() {
        return Date.from(Instant.ofEpochSecond(random.nextLong(0, Instant.now().getEpochSecond())));
    }

    public String getAgreementStatus() {
        return AGREEMENT_STATUS.get(random.nextInt(0, AGREEMENT_STATUS.size()));
    }

    public List<CreateAgreementRequest> getCorruptedCreateAgreementRequests(Product product) {
        List<Function<CreateAgreementRequest, CreateAgreementRequest>> funcs = List.of(
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setOriginationAmount(product.getMinOriginationAmount() - random.nextInt(1, 100))
                                .build()).build(),
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setOriginationAmount(product.getMaxOriginationAmount() + random.nextInt(1, 100))
                                .build()).build(),
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setInterest(new BigDecimal(product.getMinInterest()).subtract(BigDecimal.valueOf(random.nextFloat(1e-5F, 10))).toString())
                                .build()).build(),
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setInterest(new BigDecimal(product.getMaxInterest()).add(BigDecimal.valueOf(random.nextFloat(1e-5F, 10))).toString())
                                .build()).build(),
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setDisbursementAmount(product.getMinPrincipalAmount() - r.getProduct().getOriginationAmount() - random.nextInt(1, 100))
                                .build()).build(),
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setDisbursementAmount(product.getMaxPrincipalAmount() - r.getProduct().getOriginationAmount() + random.nextInt(1, 100))
                                .build()).build(),
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setLoanTerm(product.getMinLoanTerm() - random.nextInt(1, 100))
                                .build()).build(),
                r -> r.toBuilder().setProduct(
                        r.getProduct().toBuilder()
                                .setLoanTerm(product.getMaxLoanTerm() + random.nextInt(1, 100))
                                .build()).build()
        );
        CreateAgreementRequest createAgreementRequest = getCreateAgreementRequest(product);
        List<CreateAgreementRequest> corruptedRequests = new ArrayList<>();
        for (int i = 1; i < 1 << funcs.size(); i++) {
            CreateAgreementRequest corruptedRequest = createAgreementRequest;
            for (int j = 0; j < funcs.size(); j++) {
                if ((i & (1 << j)) != 0) {
                    corruptedRequest = funcs.get(j).apply(corruptedRequest);
                }
            }
            corruptedRequests.add(corruptedRequest);
        }
        return corruptedRequests;
    }
}
