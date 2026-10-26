package com.donryu.supplier.feed;

import java.util.List;

public record ImportResult(
        String supplier,
        FeedFormat format,
        int created,
        int updated,
        int rejected,
        List<String> errors) {
}
