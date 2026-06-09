package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemService;
import dev.nbcsparta.assignment.nbccheckoutsystem.facade.OrderFacade;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.service.MemberService;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.MyOrderDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.ItemDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.SimpleOrderDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.SpecificOrderDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OrderNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.service.PaymentService;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransactionType;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.service.PointService;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Mock
    private ProductRepository productRepository;

    private OrderFacade orderFacade;

    @BeforeEach
    void setUp() {
        orderFacade = new OrderFacade(
                new OrderService(cartItemRepository, orderRepository, pointTransactionRepository),
                new PaymentService(paymentRepository, memberRepository, pointTransactionRepository),
                new CartItemService(cartItemRepository, memberRepository, productRepository),
                new MemberService(memberRepository),
                new PointService(pointTransactionRepository)
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
        Member member = member(memberId);
        List<PointTransaction> pointTransactions = List.of(
                PointTransaction.createUse(member, 5_000, order),
                PointTransaction.createEarn(member, 1_000, order)
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));
        when(pointTransactionRepository.findByOrderId(orderId)).thenReturn(pointTransactions);

        SpecificOrderDetail response = orderFacade.getOrderDetail(orderId, memberId);

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

        ItemDetail firstItem = response.items().get(0);
        ItemDetail secondItem = response.items().get(1);

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
    void getOrderDetailReturnsNullPointWhenPointTransactionsDoNotExist() {
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

        SpecificOrderDetail response = orderFacade.getOrderDetail(orderId, memberId);

        assertNull(response.point());

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
        Member member = member(memberId);
        List<PointTransaction> pointTransactions = List.of(
                PointTransaction.createUse(member, 1_000, order),
                PointTransaction.createEarn(member, 200, order),
                PointTransaction.createCancel(member, 300, PointTransactionType.USE_CANCEL, order),
                PointTransaction.createCancel(member, 50, PointTransactionType.EARN_CANCEL, order)
        );

        when(orderRepository.findOrderInFullDetailById(orderId)).thenReturn(Optional.of(order));
        when(pointTransactionRepository.findByOrderId(orderId)).thenReturn(pointTransactions);

        SpecificOrderDetail response = orderFacade.getOrderDetail(orderId, memberId);

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
                () -> orderFacade.getOrderDetail(orderId, memberId)
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
                () -> orderFacade.getOrderDetail(orderId, requestMemberId)
        );

        Assertions.assertEquals("Client Not Match", exception.getMessage());
        verify(orderRepository).findOrderInFullDetailById(orderId);
        verify(pointTransactionRepository, never()).findByOrderId(orderId);
    }

    @Test
    void getMyOrderDetailReturnsPagedOrdersForMember() {
        long memberId = 1L;
        Pageable requestPageable = PageRequest.of(0, 20);
        List<SimpleOrderDetail> orders = List.of(
                new SimpleOrderDetail(
                        1001L,
                        OrderStatus.PAID,
                        78_000,
                        LocalDateTime.of(2026, 5, 29, 10, 0)
                ),
                new SimpleOrderDetail(
                        1000L,
                        OrderStatus.STANDBY,
                        32_000,
                        LocalDateTime.of(2026, 5, 28, 9, 30)
                )
        );

        when(orderRepository.findByMemberIdFormatOfSimpleOrderDetail(eq(memberId), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(orders, invocation.getArgument(1), 2));

        MyOrderDetail response = orderFacade.getMyOrderDetail(memberId, requestPageable);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, response.content().size()),
                () -> Assertions.assertEquals(0, response.page()),
                () -> Assertions.assertEquals(20, response.size()),
                () -> Assertions.assertEquals(2, response.totalElements()),
                () -> Assertions.assertEquals(1, response.totalPages())
        );

        SimpleOrderDetail firstOrder = response.content().get(0);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1001L, firstOrder.orderId()),
                () -> Assertions.assertEquals(OrderStatus.PAID, firstOrder.status()),
                () -> Assertions.assertEquals(78_000, firstOrder.totalAmount()),
                () -> Assertions.assertEquals(
                        LocalDateTime.of(2026, 5, 29, 10, 0),
                        firstOrder.orderedAt()
                )
        );

        verify(orderRepository).findByMemberIdFormatOfSimpleOrderDetail(eq(memberId), any(Pageable.class));
    }

    @Test
    void getMyOrderDetailForcesLatestSortAndKeepsRequestedPageAndSize() {
        long memberId = 1L;
        Pageable requestPageable = PageRequest.of(
                2,
                5,
                Sort.by(Sort.Direction.ASC, "totalAmount")
        );

        when(orderRepository.findByMemberIdFormatOfSimpleOrderDetail(eq(memberId), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(1), 0));

        orderFacade.getMyOrderDetail(memberId, requestPageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findByMemberIdFormatOfSimpleOrderDetail(eq(memberId), pageableCaptor.capture());

        Pageable actualPageable = pageableCaptor.getValue();

        Assertions.assertAll(
                () -> Assertions.assertEquals(2, actualPageable.getPageNumber()),
                () -> Assertions.assertEquals(5, actualPageable.getPageSize()),
                () -> Assertions.assertEquals(
                        Sort.Direction.DESC,
                        actualPageable.getSort().getOrderFor("createdDate").getDirection()
                ),
                () -> Assertions.assertNull(actualPageable.getSort().getOrderFor("totalAmount"))
        );
    }

    @Test
    void getMyOrderDetailReturnsEmptyPageWhenMemberHasNoOrders() {
        long memberId = 1L;
        Pageable requestPageable = PageRequest.of(0, 20);

        when(orderRepository.findByMemberIdFormatOfSimpleOrderDetail(eq(memberId), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(), invocation.getArgument(1), 0));

        MyOrderDetail response = orderFacade.getMyOrderDetail(memberId, requestPageable);

        Assertions.assertAll(
                () -> Assertions.assertTrue(response.content().isEmpty()),
                () -> Assertions.assertEquals(0, response.page()),
                () -> Assertions.assertEquals(20, response.size()),
                () -> Assertions.assertEquals(0, response.totalElements()),
                () -> Assertions.assertEquals(0, response.totalPages())
        );

        verify(orderRepository).findByMemberIdFormatOfSimpleOrderDetail(eq(memberId), any(Pageable.class));
    }

    private Order order(
            Long id,
            Long memberId,
            int totalAmount,
            int usedPoint,
            List<OrderItem> orderItems
    ) {
        Order order = newInstance(Order.class);
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "member", member(memberId));
        ReflectionTestUtils.setField(order, "totalAmount", totalAmount);
        ReflectionTestUtils.setField(order, "usedPoint", usedPoint);
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.STANDBY);
        ReflectionTestUtils.setField(order, "orderItems", orderItems);

        Payment payment = new Payment(order);
        ReflectionTestUtils.setField(order, "payment", payment);

        return order;
    }

    private OrderItem orderItem(Long id, String name, int price, int quantities) {
        OrderItem orderItem = newInstance(OrderItem.class);
        ReflectionTestUtils.setField(orderItem, "id", id);
        ReflectionTestUtils.setField(orderItem, "name", name);
        ReflectionTestUtils.setField(orderItem, "price", price);
        ReflectionTestUtils.setField(orderItem, "quantities", quantities);
        return orderItem;
    }

    private Member member(Long id) {
        Member member = new Member("test@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
