package com.academy.fintech.origination.core.db.application;

import com.academy.fintech.origination.core.db.client.ClientEntity;

import java.math.BigInteger;
import java.util.NoSuchElementException;

/**
 * Сервис для работы с таблицей "application"
 */
public interface ApplicationService {
    /**
     * Создать новую заявку
     *
     * @param clientEntity       клиент, который подал заявку
     * @param agreementId        id созданного договора по заявке
     * @param disbursementAmount запрошенный disbursement
     * @return entity созданной заявки
     */
    ApplicationEntity create(ClientEntity clientEntity, BigInteger agreementId, int disbursementAmount);

    /**
     * Получить заявку по ее id
     *
     * @param applicationId id заявки
     * @return заявка с соответствующим id
     * @throws NoSuchElementException если не была найдена заявка с таким id
     */
    ApplicationEntity getById(BigInteger applicationId) throws NoSuchElementException;

    /**
     * Получить набор всех новых заявок
     *
     * @return итератор по всем новым заявкам
     */
    Iterable<ApplicationEntity> getNew();

    /**
     * Перевести заявку в статус отклоненной
     *
     * @param application entity заявки, которую нужно отклонить
     */
    void reject(ApplicationEntity application);

    /**
     * Перевести заявку в статус одобренной
     *
     * @param application entity заявки, которую нужно одобрить
     */
    void accept(ApplicationEntity application);

    /**
     * Перевести заявку в статус проверяемой
     *
     * @param application entity проверяемой заявки
     */
    void scoring(ApplicationEntity application);

    /**
     * Перевести заявку в статус активной
     *
     * @param application entity активной заявки
     */
    void active(ApplicationEntity application);

    /**
     * Перевести заявку в статус отмененной
     *
     * @param application entity отменённой заявки
     */
    void cancel(ApplicationEntity application);

    /**
     * Проверить, является ли заявка отмененной
     *
     * @param application entity заявки
     * @return является ли заявка отмененной
     */
    boolean isCancelled(ApplicationEntity application);
}
