package com.selimhorri.app.unit;

import com.selimhorri.app.service.impl.ProductServiceImpl;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.helper.ProductMappingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceUnitTest {
    @InjectMocks
    private ProductServiceImpl productService;
    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    private ProductDto buildValidProduct(String title) {
        CategoryDto category = CategoryDto.builder().categoryId(1).categoryTitle("Cat1").build();
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
    void testSaveProduct() {
        ProductDto productDto = buildValidProduct("test");
        when(productRepository.save(any())).thenReturn(ProductMappingHelper.map(productDto));
        ProductDto result = productService.save(productDto);
        assertNotNull(result);
        assertEquals("test", result.getProductTitle());
    }
    @Test
    void testFindProductById() {
        ProductDto productDto = buildValidProduct("find");
        when(productRepository.findById(1)).thenReturn(Optional.of(ProductMappingHelper.map(productDto)));
        ProductDto result = productService.findById(1);
        assertNotNull(result);
        assertEquals("find", result.getProductTitle());
    }
    @Test
    void testUpdateProduct() {
        ProductDto productDto = buildValidProduct("update");
        when(productRepository.save(any())).thenReturn(ProductMappingHelper.map(productDto));
        ProductDto updated = productService.update(productDto);
        assertEquals("update", updated.getProductTitle());
    }
    @Test
    void testDeleteProduct() {
        doNothing().when(productRepository).delete(any());
        when(productRepository.findById(1)).thenReturn(Optional.of(ProductMappingHelper.map(buildValidProduct("del"))));
        assertDoesNotThrow(() -> productService.deleteById(1));
    }
    @Test
    void testListProducts() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(ProductMappingHelper.map(buildValidProduct("list"))));
        assertFalse(productService.findAll().isEmpty());
    }
} 