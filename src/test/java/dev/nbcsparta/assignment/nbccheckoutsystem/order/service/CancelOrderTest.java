package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCancelDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.CancellationNotAllowedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OrderNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CancelOrderTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                cartItemRepository,
                orderRepository,
                paymentRepository,
                memberRepository,
                pointTransactionRepository
        );
    }

    @Test
    void cancelOrderCancelsStandbyOrderAndRestoresStock() throws Exception {
        long orderId = 1001L;
        long memberId = 1L;
        Product keyboard = product(10L, 8);
        Product mouse = product(20L, 2);
        Order order = order(
                orderId,
                memberId,
                List.of(
                        new OrderItem("Keyboard", 20_000, 2, keyboard),
                        new OrderItem("Mouse", 15_000, 1, mouse)
                )
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));

        OrderCancelDetail response = orderService.cancelOrder(orderId, memberId);

        assertAll(
                () -> assertEquals(orderId, response.id()),
                () -> assertEquals(OrderStatus.CANCELLED, response.status()),
                () -> assertEquals(OrderStatus.CANCELLED, order.getOrderStatus()),
                () -> assertEquals(PaymentStatus.FAILED, order.getPayment().getStatus()),
                () -> assertEquals(10, keyboard.getStockQuantity()),
                () -> assertEquals(3, mouse.getStockQuantity())
        );
        verify(orderRepository).findOrderInFullDetailById(orderId);
        verifyNoInteractions(
                cartItemRepository,
                paymentRepository,
                memberRepository,
                pointTransactionRepository
        );
    }

    @Test
    void cancelOrderThrowsOrderNotFoundExceptionWhenOrderDoesNotExist() {
        long orderId = 1001L;

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(orderId, 1L)
        );

        assertEquals("Order Not Found", exception.getMessage());
        verify(orderRepository).findOrderInFullDetailById(orderId);
        verifyNoInteractions(
                cartItemRepository,
                paymentRepository,
                memberRepository,
                pointTransactionRepository
        );
    }

    @Test
    void cancelOrderThrowsUnauthorisedExceptionForAnotherMembersOrder() throws Exception {
        long orderId = 1001L;
        Product product = product(10L, 8);
        Order order = order(
                orderId,
                1L,
                List.of(new OrderItem("Keyboard", 20_000, 2, product))
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));

        UnauthorisedException exception = assertThrows(
                UnauthorisedException.class,
                () -> orderService.cancelOrder(orderId, 2L)
        );

        assertAll(
                () -> assertEquals("Client Not Match", exception.getMessage()),
                () -> assertEquals(OrderStatus.STANDBY, order.getOrderStatus()),
                () -> assertEquals(PaymentStatus.PENDING, order.getPayment().getStatus()),
                () -> assertEquals(8, product.getStockQuantity())
        );
        verify(orderRepository).findOrderInFullDetailById(orderId);
    }

    @Test
    void cancelOrderThrowsCancellationNotAllowedExceptionForPaidOrder() throws Exception {
        long orderId = 1001L;
        long memberId = 1L;
        Product product = product(10L, 8);
        Order order = order(
                orderId,
                memberId,
                List.of(new OrderItem("Keyboard", 20_000, 2, product))
        );
        order.paymentResult((long) order.getTotalAmount());
        order.getPayment().confirmSuccess(LocalDateTime.now());

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));

        CancellationNotAllowedException exception = assertThrows(
                CancellationNotAllowedException.class,
                () -> orderService.cancelOrder(orderId, memberId)
        );

        assertAll(
                () -> assertEquals("Cancellation is not allowed", exception.getMessage()),
                () -> assertEquals(OrderStatus.PAID, order.getOrderStatus()),
                () -> assertEquals(PaymentStatus.COMPLETED, order.getPayment().getStatus()),
                () -> assertEquals(8, product.getStockQuantity())
        );
        verify(orderRepository).findOrderInFullDetailById(orderId);
    }

    private Order order(Long id, Long memberId, List<OrderItem> orderItems) {
        int totalAmount = orderItems.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantities())
                .sum();
        Order order = new Order(totalAmount, 0, memberId, orderItems);
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "payment", new Payment(order));
        return order;
    }

    private Product product(Long id, int stockQuantity) throws Exception {
        Constructor<Product> constructor = Product.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Product product = constructor.newInstance();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "stockQuantity", stockQuantity);
        return product;
    }
}
