package com.academy.fintech.scoring.core.grpc.scoring.v1;

import com.academy.fintech.scoring.ScoringRequest;
import com.academy.fintech.scoring.ScoringResponse;
import com.academy.fintech.scoring.ScoringServiceGrpc;
import com.academy.fintech.scoring.core.ScoringService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import java.math.BigInteger;

@Slf4j
@GRpcService
@RequiredArgsConstructor
public class ScoringController extends ScoringServiceGrpc.ScoringServiceImplBase {
    private final ScoringService scoringService;

    /**
     * Обрабатывает {@code score} gRPC запрос.
     * Возвращает баллы одобрения.
     * <p>
     * Возможные ошибки:
     * {@link Status#NOT_FOUND}, если передан неизвестный в product-engine id договора
     * {@link Status#INTERNAL}, если случилась непредвиденная ошибка
     *
     * @param request          запрос, содержащий информацию о договоре, который нужно проверить на одобрение
     * @param responseObserver observer для обработки результата
     */
    @Override
    public void score(ScoringRequest request, StreamObserver<ScoringResponse> responseObserver) {
        log.info("Got score request: {}", request);

        try {
            BigInteger clientId = new BigInteger(request.getClientId());
            BigInteger agreementId = new BigInteger(request.getAgreementId());
            int salary = request.getClientSalary();
            int score = scoringService.score(clientId, agreementId, salary);

            responseObserver.onNext(
                    ScoringResponse.newBuilder().setScore(score).build()
            );
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.NOT_FOUND) {
                responseObserver.onError(e);
            } else {
                responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("Unknown error during scoring").asException());
            }
        } catch (RuntimeException e) {
            responseObserver.onError(Status.INTERNAL.withCause(e).withDescription("Unknown error during scoring").asException());
        }
    }

}
