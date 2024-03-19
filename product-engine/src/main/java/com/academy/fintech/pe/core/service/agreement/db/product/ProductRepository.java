package com.academy.fintech.pe.core.service.agreement.db.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@code product} table
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, String> {
    /**
     * Get all available products
     *
     * @return {@link List} of available {@link ProductEntity}
     */
    default List<ProductEntity> getAvailableProducts() {
        return findAll();
    }

    /**
     * Get {@link ProductEntity} by code
     *
     * @param code code of the {@link ProductEntity} to get
     * @return {@code Optional.of(ProductEntity)} if the product was found, else {@code Optional.empty()}
     */
    Optional<ProductEntity> getByCode(String code);
}
