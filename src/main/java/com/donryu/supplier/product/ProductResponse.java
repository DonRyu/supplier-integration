package com.donryu.supplier.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        String sku,
        String name,
        int quantity,
        BigDecimal price,
        String supplier,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getSku(),
                product.getName(),
                product.getQuantity(),
                product.getPrice(),
                product.getSupplier(),
                product.getUpdatedAt());
    }
}
