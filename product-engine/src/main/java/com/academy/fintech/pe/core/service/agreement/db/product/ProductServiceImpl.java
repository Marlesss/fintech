package com.academy.fintech.pe.core.service.agreement.db.product;

import com.academy.fintech.agreement.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link ProductService} interface
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public List<Product> getAvailableProducts() {
        List<ProductEntity> res = productRepository.getAvailableProducts();
        return res.stream().map(ProductEntity::toProduct).toList();
    }

    @Override
    public Optional<ProductEntity> getByCode(String code) {
        return productRepository.getByCode(code);
    }
}
