package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ItemsNotMatchException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.PointExceedTotalCostException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.CreateOrderData;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NoCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NotOnSaleException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.SaleStatus;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderCreateTest {

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
    void createOrderCreatesStandbyOrderAndPendingPayment() throws Exception {
        Long memberId = 1L;
        Product keyboard = product(10L, "키보드", 20_000, 10);
        Product mouse = product(20L, "마우스", 15_000, 3);
        CartItem keyboardCartItem = cartItem(11L, keyboard, 2);
        CartItem mouseCartItem = cartItem(12L, mouse, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L, 12L), 5_000);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member(memberId, 10_000)));
        when(cartItemRepository.findAllByIdIn(memberId, request.cartItemIds()))
                .thenReturn(List.of(keyboardCartItem, mouseCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1001L);
            return order;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderData response = orderService.createOrder(memberId, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(orderRepository).save(orderCaptor.capture());
        verify(paymentRepository).save(paymentCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        Payment savedPayment = paymentCaptor.getValue();

        assertAll(
                () -> assertEquals(1001L, response.orderId()),
                () -> assertFalse(response.portOnePaymentId().isBlank()),
                () -> assertEquals(55_000, response.totalAmount()),
                () -> assertEquals(5_000, response.usedPoint()),
                () -> assertEquals("STANDBY", response.orderStatus().name()),

                () -> assertEquals(memberId, savedOrder.getMember().getId()),
                () -> assertEquals(55_000, savedOrder.getTotalAmount()),
                () -> assertEquals(5_000, savedOrder.getUsedPoint()),
                () -> assertEquals("STANDBY", savedOrder.getOrderStatus().name()),
                () -> assertEquals(2, savedOrder.getOrderItems().size()),
                () -> savedOrder.getOrderItems().forEach(orderItem -> assertSame(savedOrder, orderItem.getOrder())),

                () -> assertSame(savedOrder, savedPayment.getOrder()),
                () -> assertFalse(savedPayment.getPortOnePaymentId().isBlank()),
                () -> assertEquals(50_000, savedPayment.getPaidAmount()),
                () -> assertEquals("PENDING", savedPayment.getStatus().name()),

                () -> assertEquals(8, stockQuantity(keyboard)),
                () -> assertEquals(2, stockQuantity(mouse))
        );
    }

    @Test
    void createOrderDoesNotClearCartBeforePaymentIsCompleted() throws Exception {
        Product product = product(10L, "키보드", 20_000, 10);
        CartItem cartItem = cartItem(11L, product, 2);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, 0)));
        when(cartItemRepository.findAllByIdIn(1L, request.cartItemIds())).thenReturn(List.of(cartItem));

        orderService.createOrder(1L, request);

        verify(cartItemRepository, never()).deleteAll(anyList());
    }

    @Test
    void createOrderRollsBackWhenProductStockIsInsufficient() throws Exception {
        Product product = product(10L, "키보드", 20_000, 1);
        CartItem cartItem = cartItem(11L, product, 2);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, 0)));
        when(cartItemRepository.findAllByIdIn(1L, request.cartItemIds())).thenReturn(List.of(cartItem));

        OutOfStockException exception = assertThrows(
                OutOfStockException.class,
                () -> orderService.createOrder(1L, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
        assertAll(
                () -> assertEquals("Product Is Out of Stock", exception.getMessage()),
                () -> assertEquals(1, stockQuantity(product))
        );
    }

    @Test
    void createOrderThrowsNotOnSaleExceptionWhenProductIsNotOnSale() throws Exception {
        Product product = product(10L, "키보드", 20_000, 10, SaleStatus.DISCONTINUED);
        CartItem cartItem = cartItem(11L, product, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, 0)));
        when(cartItemRepository.findAllByIdIn(1L, request.cartItemIds())).thenReturn(List.of(cartItem));

        NotOnSaleException exception = assertThrows(
                NotOnSaleException.class,
                () -> orderService.createOrder(1L, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
        assertEquals("Product Is Not On Sale", exception.getMessage());
    }

    @Test
    void createOrderThrowsNoCartItemExceptionWhenSelectedCartItemsDoNotExist() {
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L, 12L), 0);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, 0)));
        when(cartItemRepository.findAllByIdIn(1L, request.cartItemIds())).thenReturn(List.of());

        NoCartItemException exception = assertThrows(
                NoCartItemException.class,
                () -> orderService.createOrder(1L, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
        assertEquals("No cart items found for the provided IDs.", exception.getMessage());
    }

    @Test
    void createOrderThrowsItemsNotMatchExceptionWhenSomeSelectedCartItemsDoNotBelongToMember() throws Exception {
        Product product = product(10L, "키보드", 20_000, 10);
        CartItem cartItem = cartItem(11L, product, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L, 12L), 0);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, 0)));
        when(cartItemRepository.findAllByIdIn(1L, request.cartItemIds())).thenReturn(List.of(cartItem));

        ItemsNotMatchException exception = assertThrows(
                ItemsNotMatchException.class,
                () -> orderService.createOrder(1L, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
        assertAll(
                () -> assertEquals("Number of Items we found and you want to purchase is not match. Perhaps the member is not unauthorised client?", exception.getMessage()),
                () -> assertEquals(10, stockQuantity(product))
        );
    }

    @Test
    void createOrderThrowsMemberNotFoundExceptionWhenMemberDoesNotExist() {
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 0);
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        MemberNotFoundException exception = assertThrows(
                MemberNotFoundException.class,
                () -> orderService.createOrder(1L, request)
        );

        verify(cartItemRepository, never()).findAllByIdIn(any(Long.class), anyList());
        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        assertEquals("사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void createOrderThrowsPointExceedTotalCostExceptionWhenUsePointExceedsTotalCost() throws Exception {
        Product product = product(10L, "키보드", 20_000, 10);
        CartItem cartItem = cartItem(11L, product, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 20_001);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, 30_000)));
        when(cartItemRepository.findAllByIdIn(1L, request.cartItemIds())).thenReturn(List.of(cartItem));

        PointExceedTotalCostException exception = assertThrows(
                PointExceedTotalCostException.class,
                () -> orderService.createOrder(1L, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
        assertAll(
                () -> assertEquals("Point Cannot exceed total cost", exception.getMessage()),
                () -> assertEquals(10, stockQuantity(product))
        );
    }

    @Test
    void createOrderThrowsPointExceedTotalCostExceptionWhenUsePointExceedsMemberBalance() throws Exception {
        Product product = product(10L, "키보드", 20_000, 10);
        CartItem cartItem = cartItem(11L, product, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 5_000);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member(1L, 4_999)));
        when(cartItemRepository.findAllByIdIn(1L, request.cartItemIds())).thenReturn(List.of(cartItem));

        PointExceedTotalCostException exception = assertThrows(
                PointExceedTotalCostException.class,
                () -> orderService.createOrder(1L, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
        assertAll(
                () -> assertEquals("Point Cannot exceed total cost", exception.getMessage()),
                () -> assertEquals(10, stockQuantity(product))
        );
    }

    private Product product(Long id, String name, int price, int stockQuantity) throws Exception {
        return product(id, name, price, stockQuantity, SaleStatus.ON_SALE);
    }

    private Product product(
            Long id,
            String name,
            int price,
            int stockQuantity,
            SaleStatus saleStatus
    ) throws Exception {
        Constructor<Product> constructor = Product.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Product product = constructor.newInstance();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "name", name);
        ReflectionTestUtils.setField(product, "description", name + " 설명");
        ReflectionTestUtils.setField(product, "price", price);
        ReflectionTestUtils.setField(product, "stockQuantity", stockQuantity);
        ReflectionTestUtils.setField(product, "category", "DIGITAL");
        ReflectionTestUtils.setField(product, "salePrice", "ON_SALE");
        ReflectionTestUtils.setField(product, "saleStatus", saleStatus);
        ReflectionTestUtils.setField(product, "createdDate", LocalDateTime.now());
        ReflectionTestUtils.setField(product, "updatedDate", LocalDateTime.now());
        return product;
    }

    private CartItem cartItem(Long id, Product product, int quantity) {
        CartItem cartItem = new CartItem(null, product, quantity);
        ReflectionTestUtils.setField(cartItem, "id", id);
        ReflectionTestUtils.setField(cartItem, "createdDate", LocalDateTime.now());
        return cartItem;
    }

    private Member member(Long id, int pointBalance) {
        Member member = new Member("test@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "pointBalance", pointBalance);
        return member;
    }

    private Integer stockQuantity(Product product) {
        return (Integer) ReflectionTestUtils.getField(product, "stockQuantity");
    }
}
