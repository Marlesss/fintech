package com.academy.fintech.origination.core.db.client;

import com.academy.fintech.application.ClientData;

import java.math.BigInteger;

/**
 * Сервис для работы с таблицей клиентов
 */
public interface ClientService {
    /**
     * Найти клиента с соответствующими персональными данными или создать при его отсутствии
     *
     * @param clientData персональные данные клиента
     * @return entity клиента
     * @throws IllegalArgumentException если по персональным данным был идентифицирован существующий клиент,
     *                                  но часть переданных персональных данных не соответствует сохраненным
     */
    ClientEntity getOrCreate(ClientData clientData) throws IllegalArgumentException;
}
