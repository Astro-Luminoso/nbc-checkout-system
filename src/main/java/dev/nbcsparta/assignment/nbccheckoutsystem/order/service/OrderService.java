package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ItemsNotMatchException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.PointExceedTotalCostException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NotOnSaleException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentData;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NoCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.SaleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;



    @Transactional
    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        List<CartItem> cartItems = cartItemRepository.findAllById(memberId, request.cartItemIds());
        if (cartItems.isEmpty()) {
            throw new NoCartItemException();
        }
        if (cartItems.size() != request.cartItemIds().size()) {
            throw new ItemsNotMatchException();
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem item : cartItems) {
            Product product = item.getProductId();
            if(product.getSaleStatus() != SaleStatus.ON_SALE) {
                throw new NotOnSaleException();
            }

            if (product.getStockQuantity() - item.getQuantity() < 0) {
                throw new OutOfStockException();
            }

            orderItems.add(new OrderItem(product.getPrice(), item.getQuantity(), product));
            product.deductStockValue(item.getQuantity());
        }

        int totalCost = orderItems.stream().mapToInt(item -> item.getPrice() * item.getQuantities()).sum();

        if (totalCost < request.usePoint()) {
            throw new PointExceedTotalCostException();
        }

        Order order = new Order(totalCost, request.usePoint(), memberId, orderItems);
        orderRepository.save(order);

        Payment payment = new Payment(order);
        paymentRepository.save(payment);

        PaymentData data = PaymentData.from(payment);

        return OrderCreateResponse.from(data);
    }
}
