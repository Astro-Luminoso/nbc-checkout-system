package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.domain.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NoCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NotOnSaleException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.SaleStatus;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
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

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(cartItemRepository, orderRepository, paymentRepository);
    }

    @Test
    void createOrderCreatesStandbyOrderAndPendingPayment() throws Exception {
        Long memberId = 1L;
        Product keyboard = product(10L, "키보드", 20_000, 10);
        Product mouse = product(20L, "마우스", 15_000, 3);
        CartItem keyboardCartItem = cartItem(11L, keyboard, 2);
        CartItem mouseCartItem = cartItem(12L, mouse, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L, 12L), 5_000);

        when(cartItemRepository.findAllById(request.cartItemIds()))
                .thenReturn(List.of(keyboardCartItem, mouseCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1001L);
            return order;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderCreateResponse response = orderService.createOrder(memberId, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(orderRepository).save(orderCaptor.capture());
        verify(paymentRepository).save(paymentCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        Payment savedPayment = paymentCaptor.getValue();

        assertSoftly(softly -> {
            softly.assertThat(response.success()).isTrue();
            softly.assertThat(response.data().OrderId()).isEqualTo(1001L);
            softly.assertThat(response.data().portOnePaymentId()).isNotBlank();
            softly.assertThat(response.data().totalAmount()).isEqualTo(50_000);
            softly.assertThat(response.data().usedPoint()).isEqualTo(5_000);
            softly.assertThat(response.data().orderStatus().name()).isEqualTo("STANDBY");

            softly.assertThat(savedOrder.getMemberId()).isEqualTo(memberId);
            softly.assertThat(savedOrder.getTotalAmount()).isEqualTo(55_000);
            softly.assertThat(savedOrder.getUsedPoint()).isEqualTo(5_000);
            softly.assertThat(savedOrder.getOrderStatus().name()).isEqualTo("STANDBY");
            softly.assertThat(savedOrder.getOrderItems()).hasSize(2);

            softly.assertThat(savedPayment.getOrder()).isSameAs(savedOrder);
            softly.assertThat(savedPayment.getPortOnePaymentId()).isNotBlank();
            softly.assertThat(savedPayment.getTotalAmount()).isEqualTo(50_000);
            softly.assertThat(savedPayment.getStatus().name()).isEqualTo("PENDING");

            softly.assertThat(stockQuantity(keyboard)).isEqualTo(10);
            softly.assertThat(stockQuantity(mouse)).isEqualTo(3);
        });
    }

    @Test
    void createOrderDoesNotClearCartBeforePaymentIsCompleted() throws Exception {
        Product product = product(10L, "키보드", 20_000, 10);
        CartItem cartItem = cartItem(11L, product, 2);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 0);

        when(cartItemRepository.findAllById(request.cartItemIds())).thenReturn(List.of(cartItem));

        orderService.createOrder(1L, request);

        verify(cartItemRepository, never()).deleteAll(anyList());
    }

    @Test
    void createOrderRollsBackWhenProductStockIsInsufficient() throws Exception {
        Product product = product(10L, "키보드", 20_000, 1);
        CartItem cartItem = cartItem(11L, product, 2);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 0);

        when(cartItemRepository.findAllById(request.cartItemIds())).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder(1L, request))
                .isInstanceOf(OutOfStockException.class)
                .hasMessage("Product Is Out of Stock");

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
        assertThat(stockQuantity(product)).isEqualTo(1);
    }

    @Test
    void createOrderThrowsNotOnSaleExceptionWhenProductIsNotOnSale() throws Exception {
        Product product = product(10L, "키보드", 20_000, 10, SaleStatus.DISCONTINUED);
        CartItem cartItem = cartItem(11L, product, 1);
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L), 0);

        when(cartItemRepository.findAllById(request.cartItemIds())).thenReturn(List.of(cartItem));

        assertThatThrownBy(() -> orderService.createOrder(1L, request))
                .isInstanceOf(NotOnSaleException.class)
                .hasMessage("Product Is Not On Sale");

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
    }

    @Test
    void createOrderThrowsNoCartItemExceptionWhenSelectedCartItemsDoNotExist() {
        OrderCreateRequest request = new OrderCreateRequest(List.of(11L, 12L), 0);
        when(cartItemRepository.findAllById(request.cartItemIds())).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createOrder(1L, request))
                .isInstanceOf(NoCartItemException.class)
                .hasMessage("No cart items found for the provided IDs.");

        verify(orderRepository, never()).save(any(Order.class));
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(cartItemRepository, never()).deleteAll(anyList());
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

    private Integer stockQuantity(Product product) {
        return (Integer) ReflectionTestUtils.getField(product, "stockQuantity");
    }
}
