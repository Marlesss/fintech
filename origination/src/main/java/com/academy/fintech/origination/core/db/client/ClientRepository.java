package com.academy.fintech.origination.core.db.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Репозиторий для работы с таблицей клиентов
 */
@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, BigInteger> {
    /**
     * Найти entity клиента по email адресу
     *
     * @param email email адрес искомого клиента
     * @return Entity клиента, если он был найден, иначе - {@link Optional#empty()}
     */
    Optional<ClientEntity> findByEmail(String email);
}
