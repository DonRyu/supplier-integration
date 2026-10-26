package com.donryu.supplier.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> findAll(String supplier) {
        if (supplier == null || supplier.isBlank()) {
            return repository.findAllByOrderBySkuAsc();
        }
        return repository.findBySupplierOrderBySkuAsc(supplier);
    }

    public Product findBySku(String sku) {
        return repository.findBySku(sku).orElseThrow(() -> new ProductNotFoundException(sku));
    }

    public List<Product> findLowStock(int threshold) {
        return repository.findByQuantityLessThanOrderByQuantityAsc(threshold);
    }
}
