package com.example.order.service;

import com.example.order.client.ProductClient;
import com.example.order.client.PaymentClient;
import com.example.order.dto.OrderDtos.CreateOrderItemRequest;
import com.example.order.dto.OrderDtos.CreateOrderRequest;
import com.example.order.dto.OrderDtos.OrderItemResponse;
import com.example.order.dto.OrderDtos.OrderResponse;
import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.entity.OrderStatus;
import com.example.order.repository.OrderRepository;
import com.example.order.events.OrderEventPublisher;
import com.example.order.events.OrderEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1) Check availability with product service
        List<ProductClient.QuantityCheckItem> items = request.items().stream()
                .map(i -> new ProductClient.QuantityCheckItem(i.productId(), i.quantity()))
                .toList();
        var availability = productClient.checkAvailability(items);
        Map<Long, ProductClient.AvailabilityResponse> byId = availability.stream()
                .collect(Collectors.toMap(ProductClient.AvailabilityResponse::productId, a -> a));

        boolean allAvailable = request.items().stream()
                .allMatch(i -> {
                    var a = byId.get(i.productId());
                    return a != null && a.available();
                });

        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setCreatedAt(OffsetDateTime.now());
        order.setStatus(allAvailable ? OrderStatus.CREATED : OrderStatus.REJECTED);

        List<OrderItem> orderItems = request.items().stream()
                .map(i -> OrderItem.builder()
                        .order(order)
                        .productId(i.productId())
                        .quantity(i.quantity())
                        .build())
                .toList();

        order.setItems(orderItems);
        // TODO: compute actual total if price service exists; using quantity sum as placeholder amount
        BigDecimal totalAmount = BigDecimal.valueOf(orderItems.stream().mapToInt(OrderItem::getQuantity).sum());
        order.setTotalAmount(totalAmount);

        // Precheck balance before placing order
        Boolean ok = paymentClient.precheck(request.customerId(), totalAmount);
        if (ok == null || !ok) {
            order.setStatus(OrderStatus.REJECTED);
        }

        Order saved = orderRepository.save(order);

        // Publish domain events
        var itemEvents = saved.getItems().stream()
                .map(oi -> new OrderEvents.OrderItemEvent(oi.getProductId(), oi.getQuantity()))
                .toList();
        if (saved.getStatus() == OrderStatus.CREATED) {
            eventPublisher.publishCreated(new OrderEvents.OrderCreatedEvent(saved.getId(), saved.getCustomerId(), itemEvents));
        } else {
            eventPublisher.publishRejected(new OrderEvents.OrderRejectedEvent(saved.getId(), saved.getCustomerId(), "INSUFFICIENT_STOCK", itemEvents));
        }

        List<OrderItemResponse> itemResponses = saved.getItems().stream()
                .map(oi -> new OrderItemResponse(oi.getProductId(), oi.getQuantity()))
                .toList();

        return new OrderResponse(saved.getId(), saved.getCustomerId(), saved.getStatus(), saved.getTotalAmount(), itemResponses);
    }
}


