package com.selimhorri.app.integration;

import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.domain.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.domain.Category;

@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CategoryRepository categoryRepository;

    private ProductDto buildValidProduct(String title, int categoryId) {
        CategoryDto category = CategoryDto.builder().categoryId(categoryId).categoryTitle("Cat1").build();
        return ProductDto.builder()
            .productId(1)
            .productTitle(title)
            .imageUrl("img.png")
            .sku("SKU1")
            .priceUnit(10.0)
            .quantity(5)
            .categoryDto(category)
            .build();
    }

    @Test
    void testCreateProduct() throws Exception {
        Category category = Category.builder().categoryId(1).categoryTitle("Cat1").build();
        categoryRepository.save(category);
        ProductDto productDto = buildValidProduct("TestProduct", 1);
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productTitle").value("TestProduct"));
    }
    @Test
    void testGetProductById() throws Exception {
        Category category = Category.builder().categoryId(2).categoryTitle("Cat2").build();
        categoryRepository.save(category);
        Product product = productRepository.save(Product.builder().category(category).build());
        mockMvc.perform(get("/api/products/" + product.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(product.getProductId()));
    }
    @Test
    void testGetNonExistentProductById() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }
    @Test
    void testListProducts() throws Exception {
        Category category = Category.builder().categoryId(3).categoryTitle("Cat3").build();
        categoryRepository.save(category);
        productRepository.save(Product.builder().category(category).build());
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection").exists());
    }
    @Test
    void testUpdateProduct() throws Exception {
        Category category = Category.builder().categoryId(4).categoryTitle("Cat4").build();
        categoryRepository.save(category);
        Product product = productRepository.save(Product.builder().category(category).build());
        ProductDto productDto = buildValidProduct("UpdatedProduct", 4);
        productDto.setProductId(product.getProductId());
        mockMvc.perform(put("/api/products/" + product.getProductId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productTitle").value("UpdatedProduct"));
    }
    @Test
    void testDeleteProduct() throws Exception {
        Category category = Category.builder().categoryId(5).categoryTitle("Cat5").build();
        categoryRepository.save(category);
        Product product = productRepository.save(Product.builder().category(category).build());
        mockMvc.perform(delete("/api/products/" + product.getProductId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
} 