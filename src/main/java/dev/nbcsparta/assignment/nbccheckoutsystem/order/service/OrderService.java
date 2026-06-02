package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.domain.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentData;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NoCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;



    public OrderCreateResponse createOrder(Long memberId, OrderCreateRequest request) {
        List<CartItem> cartItems = cartItemRepository.findAllById(request.cartItemIds());
        if (cartItems.isEmpty()) {
            throw new NoCartItemException();
        }

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = cartItem.getProductId();
                    return new OrderItem(
                            product.getPrice(),
                            cartItem.getQuantity(),
                            product
                    );
                })
                .toList();

        Order order = new Order(request.usePoint(), memberId, orderItems);
        orderRepository.save(order);

        Payment payment = new Payment(order);
        paymentRepository.save(payment);

        cartItemRepository.deleteAll(cartItems);

        PaymentData data = PaymentData.from(payment);

        return OrderCreateResponse.from(data);
    }
}
