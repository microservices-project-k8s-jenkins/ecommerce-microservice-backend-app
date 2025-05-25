package com.selimhorri.app.unit;

import com.selimhorri.app.service.impl.OrderServiceImpl;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.helper.OrderMappingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceUnitTest {
    @InjectMocks
    private OrderServiceImpl orderService;
    @Mock
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    private OrderDto buildValidOrder() {
        CartDto cart = CartDto.builder().cartId(1).userId(1).build();
        return OrderDto.builder()
            .orderId(1)
            .orderDate(LocalDateTime.now())
            .orderDesc("Test order")
            .orderFee(100.0)
            .cartDto(cart)
            .build();
    }

    @Test
    void testSaveOrder() {
        OrderDto orderDto = buildValidOrder();
        when(orderRepository.save(any())).thenReturn(OrderMappingHelper.map(orderDto));
        OrderDto result = orderService.save(orderDto);
        assertNotNull(result);
        assertEquals("Test order", result.getOrderDesc());
    }
    @Test
    void testFindOrderById() {
        OrderDto orderDto = buildValidOrder();
        when(orderRepository.findById(1)).thenReturn(Optional.of(OrderMappingHelper.map(orderDto)));
        OrderDto result = orderService.findById(1);
        assertNotNull(result);
        assertEquals("Test order", result.getOrderDesc());
    }
    @Test
    void testUpdateOrder() {
        OrderDto orderDto = buildValidOrder();
        when(orderRepository.save(any())).thenReturn(OrderMappingHelper.map(orderDto));
        OrderDto updated = orderService.update(orderDto);
        assertEquals("Test order", updated.getOrderDesc());
    }
    @Test
    void testDeleteOrder() {
        doNothing().when(orderRepository).delete(any());
        when(orderRepository.findById(1)).thenReturn(Optional.of(OrderMappingHelper.map(buildValidOrder())));
        assertDoesNotThrow(() -> orderService.deleteById(1));
    }
    @Test
    void testListOrders() {
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(OrderMappingHelper.map(buildValidOrder())));
        assertFalse(orderService.findAll().isEmpty());
    }
} 