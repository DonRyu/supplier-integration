package com.donryu.supplier.feed;

import com.donryu.supplier.product.Product;
import com.donryu.supplier.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedImportServiceTest {

    @Mock
    private ProductRepository productRepository;

    private FeedImportService service;

    @BeforeEach
    void setUp() {
        FeedMapperRegistry registry = new FeedMapperRegistry(List.of(new CsvFeedMapper(), new XmlFeedMapper()));
        service = new FeedImportService(registry, productRepository);
    }

    @Test
    void createsNewProductsAndUpdatesExistingOnes() {
        Product existing = new Product("A100", "Milk 2L", 5, new BigDecimal("4.49"), "supplier-a");
        when(productRepository.findBySku("A100")).thenReturn(Optional.of(existing));
        when(productRepository.findBySku("A101")).thenReturn(Optional.empty());

        String csv = """
                item_no,desc,qty,unit_price
                A100,Milk 2L,24,4.99
                A101,Bread,10,3.50
                """;

        ImportResult result = service.importFeed("supplier-a", FeedFormat.CSV, stream(csv));

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.rejected()).isZero();
        assertThat(existing.getQuantity()).isEqualTo(24);
        assertThat(existing.getPrice()).isEqualByComparingTo("4.99");

        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(saved.capture());
        assertThat(saved.getValue().getSku()).isEqualTo("A101");
    }

    @Test
    void reportsRejectedRowsWithoutSavingThem() {
        String csv = """
                item_no,desc,qty,unit_price
                A100,Milk 2L,abc,4.99
                """;

        ImportResult result = service.importFeed("supplier-a", FeedFormat.CSV, stream(csv));

        assertThat(result.rejected()).isEqualTo(1);
        assertThat(result.errors()).containsExactly("Row 2: invalid quantity 'abc'");
        verify(productRepository, never()).save(any());
    }

    private ByteArrayInputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
