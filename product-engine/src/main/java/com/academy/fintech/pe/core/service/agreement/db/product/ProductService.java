package com.academy.fintech.pe.core.service.agreement.db.product;

import com.academy.fintech.agreement.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service to work with {@link ProductRepository}
 */
public interface ProductService {
    /**
     * Get all available products
     *
     * @return {@link List} of available {@link Product}
     */
    List<Product> getAvailableProducts();

    /**
     * Get {@link ProductEntity} by code
     *
     * @param code code of the {@link ProductEntity} to get
     * @return {@code Optional.of(ProductEntity)} if the product was found, else {@code Optional.empty()}
     */
    Optional<ProductEntity> getByCode(String code);
}
