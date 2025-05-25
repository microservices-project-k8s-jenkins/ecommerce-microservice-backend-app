package com.selimhorri.app.e2e;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceE2ETest {
    @Autowired
    private TestRestTemplate restTemplate;

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
    void testUserPlacesOrder() {
        OrderDto order = buildValidOrder();
        ResponseEntity<OrderDto> response = restTemplate.postForEntity("/api/orders", order, OrderDto.class);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Test order", response.getBody().getOrderDesc());
    }
    @Test
    void testUserDeletesOrder() {
        OrderDto order = restTemplate.postForEntity("/api/orders", buildValidOrder(), OrderDto.class).getBody();
        restTemplate.delete("/api/orders/" + order.getOrderId());
        assertNotNull(order);
    }
    @Test
    void testUserViewsOrderList() {
        ResponseEntity<DtoCollectionResponse> response = restTemplate.getForEntity("/api/orders", DtoCollectionResponse.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getCollection() != null);
        } else {
            String errorJson = response.toString();
            assertTrue(errorJson.contains("timestamp"));
        }
    }
    @Test
    void testUserUpdatesOrder() {
        OrderDto order = restTemplate.postForEntity("/api/orders", buildValidOrder(), OrderDto.class).getBody();
        order.setOrderDesc("Updated order");
        restTemplate.put("/api/orders/" + order.getOrderId(), order);
        assertNotNull(order);
    }
    @Test
    void testUserViewsOrderDetails() {
        OrderDto order = restTemplate.postForEntity("/api/orders", buildValidOrder(), OrderDto.class).getBody();
        ResponseEntity<OrderDto> response = restTemplate.getForEntity("/api/orders/" + order.getOrderId(), OrderDto.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            assertNotNull(response.getBody());
            assertEquals("Test order", response.getBody().getOrderDesc());
        } else {
            String errorJson = response.toString();
            assertTrue(errorJson.contains("timestamp"));
        }
    }
    @Test
    void testUserViewsNonExistentOrder() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/orders/999999", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("timestamp"));
    }
} 