package com.donryu.supplier.feed;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class ProductRecordParser {

    private ProductRecordParser() {
    }

    static ProductRecord parse(String sku, String name, String quantity, String price) {
        if (isBlank(sku)) {
            throw new IllegalArgumentException("sku is required");
        }
        if (isBlank(name)) {
            throw new IllegalArgumentException("name is required");
        }
        return new ProductRecord(sku.trim(), name.trim(), parseQuantity(quantity), parsePrice(price));
    }

    private static int parseQuantity(String value) {
        int quantity;
        try {
            quantity = Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid quantity '" + value + "'");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        return quantity;
    }

    private static BigDecimal parsePrice(String value) {
        BigDecimal price;
        try {
            price = new BigDecimal(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid price '" + value + "'");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
