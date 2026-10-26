package com.donryu.supplier.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void returnsProductBySku() throws Exception {
        when(productService.findBySku("A100"))
                .thenReturn(new Product("A100", "Milk 2L", 24, new BigDecimal("4.99"), "supplier-a"));

        mockMvc.perform(get("/api/products/A100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Milk 2L"))
                .andExpect(jsonPath("$.quantity").value(24));
    }

    @Test
    void returnsNotFoundForUnknownSku() throws Exception {
        when(productService.findBySku("X999")).thenThrow(new ProductNotFoundException("X999"));

        mockMvc.perform(get("/api/products/X999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Product not found: X999"));
    }

    @Test
    void returnsLowStockProducts() throws Exception {
        when(productService.findLowStock(5))
                .thenReturn(List.of(new Product("A101", "Bread", 2, new BigDecimal("3.50"), "supplier-a")));

        mockMvc.perform(get("/api/products/low-stock").param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("A101"));
    }
}
