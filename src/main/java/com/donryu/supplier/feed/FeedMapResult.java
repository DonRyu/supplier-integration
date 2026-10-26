package com.donryu.supplier.feed;

import java.util.List;

public record FeedMapResult(List<ProductRecord> records, List<String> errors) {
}
