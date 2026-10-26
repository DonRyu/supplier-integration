package com.donryu.supplier.feed;

import java.math.BigDecimal;

public record ProductRecord(String sku, String name, int quantity, BigDecimal price) {
}
