package com.donryu.supplier.feed;

import com.donryu.supplier.product.Product;
import com.donryu.supplier.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Optional;

@Service
public class FeedImportService {

    private final FeedMapperRegistry mapperRegistry;
    private final ProductRepository productRepository;

    public FeedImportService(FeedMapperRegistry mapperRegistry, ProductRepository productRepository) {
        this.mapperRegistry = mapperRegistry;
        this.productRepository = productRepository;
    }

    @Transactional
    public ImportResult importFeed(String supplier, FeedFormat format, InputStream input) {
        FeedMapResult mapped = mapperRegistry.get(format).map(input);

        int created = 0;
        int updated = 0;
        for (ProductRecord record : mapped.records()) {
            Optional<Product> existing = productRepository.findBySku(record.sku());
            if (existing.isPresent()) {
                existing.get().update(record.name(), record.quantity(), record.price(), supplier);
                updated++;
            } else {
                productRepository.save(
                        new Product(record.sku(), record.name(), record.quantity(), record.price(), supplier));
                created++;
            }
        }
        return new ImportResult(supplier, format, created, updated, mapped.errors().size(), mapped.errors());
    }
}
