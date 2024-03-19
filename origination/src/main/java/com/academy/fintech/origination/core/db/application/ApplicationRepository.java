package com.academy.fintech.origination.core.db.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

/**
 * Репозиторий для таблицы заявок
 */
@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, BigInteger> {
    /**
     * Найти все заявки с соответствующим статусом
     *
     * @param status статус, в котором должна находиться заявка
     * @return итератор по всем заявкам в соответствующем статусе
     */
    Iterable<ApplicationEntity> findAllByStatus(String status);
}
