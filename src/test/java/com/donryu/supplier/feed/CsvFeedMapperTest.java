package com.donryu.supplier.feed;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvFeedMapperTest {

    private final CsvFeedMapper mapper = new CsvFeedMapper();

    @Test
    void mapsSupplierColumnsToProductRecords() {
        String csv = """
                item_no,desc,qty,unit_price
                A100,Milk 2L,24,4.99
                A101,Whole Wheat Bread,10,3.5
                """;

        FeedMapResult result = mapper.map(stream(csv));

        assertThat(result.errors()).isEmpty();
        assertThat(result.records()).containsExactly(
                new ProductRecord("A100", "Milk 2L", 24, new BigDecimal("4.99")),
                new ProductRecord("A101", "Whole Wheat Bread", 10, new BigDecimal("3.50")));
    }

    @Test
    void collectsRowErrorsAndKeepsValidRows() {
        String csv = """
                item_no,desc,qty,unit_price
                A100,Milk 2L,24,4.99
                A101,Bread,abc,3.50
                ,Eggs,12,5.25
                A103,Butter,-1,6.00
                """;

        FeedMapResult result = mapper.map(stream(csv));

        assertThat(result.records()).hasSize(1);
        assertThat(result.errors()).containsExactly(
                "Row 3: invalid quantity 'abc'",
                "Row 4: sku is required",
                "Row 5: quantity cannot be negative");
    }

    @Test
    void handlesQuotedValuesWithCommas() {
        String csv = """
                item_no,desc,qty,unit_price
                A200,"Apples, Gala 3lb",8,5.99
                """;

        FeedMapResult result = mapper.map(stream(csv));

        assertThat(result.records().get(0).name()).isEqualTo("Apples, Gala 3lb");
    }

    @Test
    void failsWhenRequiredColumnIsMissing() {
        String csv = """
                item_no,desc,qty
                A100,Milk 2L,24
                """;

        assertThatThrownBy(() -> mapper.map(stream(csv)))
                .isInstanceOf(FeedParseException.class)
                .hasMessageContaining("unit_price");
    }

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
