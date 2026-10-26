package com.donryu.supplier.feed;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlFeedMapperTest {

    private final XmlFeedMapper mapper = new XmlFeedMapper();

    @Test
    void mapsSupplierElementsToProductRecords() {
        String xml = """
                <products>
                  <product>
                    <sku>B200</sku>
                    <productName>Cheddar Cheese 400g</productName>
                    <stock>15</stock>
                    <cost>7.49</cost>
                  </product>
                </products>
                """;

        FeedMapResult result = mapper.map(stream(xml));

        assertThat(result.errors()).isEmpty();
        assertThat(result.records()).containsExactly(
                new ProductRecord("B200", "Cheddar Cheese 400g", 15, new BigDecimal("7.49")));
    }

    @Test
    void collectsErrorsForInvalidProducts() {
        String xml = """
                <products>
                  <product><sku>B200</sku><productName>Cheese</productName><stock>15</stock><cost>7.49</cost></product>
                  <product><sku>B201</sku><productName>Yogurt</productName><stock>5</stock></product>
                  <product><sku>B202</sku><stock>3</stock><cost>2.00</cost></product>
                </products>
                """;

        FeedMapResult result = mapper.map(stream(xml));

        assertThat(result.records()).hasSize(1);
        assertThat(result.errors()).containsExactly(
                "Product 2: invalid price 'null'",
                "Product 3: name is required");
    }

    @Test
    void failsOnMalformedXml() {
        assertThatThrownBy(() -> mapper.map(stream("<products><product>")))
                .isInstanceOf(FeedParseException.class);
    }

    @Test
    void rejectsDoctypeToPreventXxe() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE products [<!ENTITY secret SYSTEM "file:///etc/passwd">]>
                <products><product><sku>&secret;</sku></product></products>
                """;

        assertThatThrownBy(() -> mapper.map(stream(xml)))
                .isInstanceOf(FeedParseException.class);
    }

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
