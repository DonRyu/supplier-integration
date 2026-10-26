package com.donryu.supplier.feed;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CsvFeedMapper implements FeedMapper {

    private static final String SKU = "item_no";
    private static final String NAME = "desc";
    private static final String QUANTITY = "qty";
    private static final String PRICE = "unit_price";
    private static final List<String> REQUIRED_COLUMNS = List.of(SKU, NAME, QUANTITY, PRICE);

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();

    @Override
    public FeedFormat format() {
        return FeedFormat.CSV;
    }

    @Override
    public FeedMapResult map(InputStream input) {
        try (CSVParser parser = FORMAT.parse(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            validateHeader(parser.getHeaderMap());

            List<ProductRecord> records = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            int line = 1;
            for (CSVRecord row : parser) {
                line++;
                try {
                    records.add(ProductRecordParser.parse(
                            row.get(SKU), row.get(NAME), row.get(QUANTITY), row.get(PRICE)));
                } catch (IllegalArgumentException e) {
                    errors.add("Row " + line + ": " + e.getMessage());
                }
            }
            return new FeedMapResult(records, errors);
        } catch (IOException | UncheckedIOException | IllegalArgumentException e) {
            throw new FeedParseException("Could not read CSV feed: " + e.getMessage(), e);
        }
    }

    private void validateHeader(Map<String, Integer> header) {
        List<String> missing = REQUIRED_COLUMNS.stream()
                .filter(column -> header == null || !header.containsKey(column))
                .toList();
        if (!missing.isEmpty()) {
            throw new FeedParseException("Missing CSV columns: " + String.join(", ", missing));
        }
    }
}
