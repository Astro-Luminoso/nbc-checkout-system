package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderItemDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.SpecificOrderDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OrderNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransactionType;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GetOrderTest {

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
    void getOrderDetailReturnsOrderDetailForOwner() {
        Long orderId = 100L;
        long memberId = 1L;
        Order order = order(
                orderId,
                memberId,
                55_000,
                5_000,
                List.of(
                        orderItem(11L, "Keyboard", 20_000, 2),
                        orderItem(12L, "Mouse", 15_000, 1)
                )
        );
        Members member = member(memberId);
        List<PointTransaction> pointTransactions = List.of(
                PointTransaction.createUse(member, 5_000, order),
                PointTransaction.createEarn(member, 1_000, order)
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));
        when(pointTransactionRepository.findByOrderId(orderId)).thenReturn(pointTransactions);

        SpecificOrderDetail response = orderService.getOrderDetail(orderId, memberId);

        Assertions.assertAll(
                () -> Assertions.assertEquals(orderId, response.orderId()),
                () -> Assertions.assertEquals("STANDBY", response.orderStatus().name()),
                () -> Assertions.assertEquals(55_000, response.totalAmount()),
                () -> Assertions.assertEquals(PaymentStatus.PENDING, response.payment().paymentStatus()),
                () -> Assertions.assertEquals(50_000, response.payment().amount()),
                () -> Assertions.assertEquals(5_000, response.point().used()),
                () -> Assertions.assertEquals(1_000, response.point().earned()),
                () -> Assertions.assertEquals(2, response.items().size())
        );

        OrderItemDetail firstItem = response.items().get(0);
        OrderItemDetail secondItem = response.items().get(1);

        Assertions.assertAll(
                () -> Assertions.assertEquals(11L, firstItem.id()),
                () -> Assertions.assertEquals("Keyboard", firstItem.name()),
                () -> Assertions.assertEquals(20_000, firstItem.price()),
                () -> Assertions.assertEquals(2, firstItem.quantities()),
                () -> Assertions.assertEquals(12L, secondItem.id()),
                () -> Assertions.assertEquals("Mouse", secondItem.name()),
                () -> Assertions.assertEquals(15_000, secondItem.price()),
                () -> Assertions.assertEquals(1, secondItem.quantities())
        );

        verify(orderRepository).findOrderInFullDetailById(orderId);
        verify(pointTransactionRepository).findByOrderId(orderId);
    }

    @Test
    void getOrderDetailReturnsZeroPointWhenPointTransactionsDoNotExist() {
        Long orderId = 100L;
        long memberId = 1L;
        Order order = order(
                orderId,
                memberId,
                20_000,
                0,
                List.of(orderItem(11L, "Keyboard", 20_000, 1))
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));
        when(pointTransactionRepository.findByOrderId(orderId)).thenReturn(List.of());

        SpecificOrderDetail response = orderService.getOrderDetail(orderId, memberId);

        Assertions.assertAll(
                () -> Assertions.assertEquals(0, response.point().used()),
                () -> Assertions.assertEquals(0, response.point().earned())
        );

        verify(orderRepository).findOrderInFullDetailById(orderId);
        verify(pointTransactionRepository).findByOrderId(orderId);
    }

    @Test
    void getOrderDetailAggregatesPointTransactionsByType() {
        Long orderId = 100L;
        long memberId = 1L;
        Order order = order(
                orderId,
                memberId,
                20_000,
                1_000,
                List.of(orderItem(11L, "Keyboard", 20_000, 1))
        );
        Members member = member(memberId);
        List<PointTransaction> pointTransactions = List.of(
                PointTransaction.createUse(member, 1_000, order),
                PointTransaction.createEarn(member, 200, order),
                PointTransaction.createCancel(member, 300, PointTransactionType.USE_CANCEL, order),
                PointTransaction.createCancel(member, 50, PointTransactionType.EARN_CANCEL, order)
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));
        when(pointTransactionRepository.findByOrderId(orderId)).thenReturn(pointTransactions);

        SpecificOrderDetail response = orderService.getOrderDetail(orderId, memberId);

        Assertions.assertAll(
                () -> Assertions.assertEquals(700, response.point().used()),
                () -> Assertions.assertEquals(150, response.point().earned())
        );

        verify(orderRepository).findOrderInFullDetailById(orderId);
        verify(pointTransactionRepository).findByOrderId(orderId);
    }

    @Test
    void getOrderDetailThrowsOrderNotFoundExceptionWhenOrderDoesNotExist() {
        long orderId = 100L;
        long memberId = 1L;

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = Assertions.assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderDetail(orderId, memberId)
        );

        Assertions.assertEquals("Order Not Found", exception.getMessage());
        verify(orderRepository).findOrderInFullDetailById(orderId);
        verify(pointTransactionRepository, never()).findByOrderId(orderId);
    }

    @Test
    void getOrderDetailThrowsUnauthorisedExceptionWhenOrderMemberDoesNotMatch() {
        Long orderId = 100L;
        long orderMemberId = 1L;
        long requestMemberId = 2L;
        Order order = order(
                orderId,
                orderMemberId,
                20_000,
                0,
                List.of(orderItem(11L, "Keyboard", 20_000, 1))
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));

        UnauthorisedException exception = Assertions.assertThrows(
                UnauthorisedException.class,
                () -> orderService.getOrderDetail(orderId, requestMemberId)
        );

        Assertions.assertEquals("Client Not Match", exception.getMessage());
        verify(orderRepository).findOrderInFullDetailById(orderId);
        verify(pointTransactionRepository, never()).findByOrderId(orderId);
    }

    private Order order(
            Long id,
            Long memberId,
            int totalAmount,
            int usedPoint,
            List<OrderItem> orderItems
    ) {
        Order order = new Order(totalAmount, usedPoint, memberId, orderItems);
        ReflectionTestUtils.setField(order, "id", id);

        Payment payment = new Payment(order);
        ReflectionTestUtils.setField(order, "payment", payment);

        return order;
    }

    private OrderItem orderItem(Long id, String name, int price, int quantities) {
        OrderItem orderItem = new OrderItem(name, price, quantities, null);
        ReflectionTestUtils.setField(orderItem, "id", id);
        return orderItem;
    }

    private Members member(Long id) {
        Members member = new Members("test@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
