package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Refund;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.DuplicateRefundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.InvalidPaymentStatusException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.RefundRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransactionType;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import java.lang.reflect.Constructor;
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
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private RefundService refundService;

    @BeforeEach
    void setUp() {
        refundService = new RefundService(refundRepository, pointTransactionRepository, memberRepository, paymentRepository);
    }

    @Test
    void refundCompletedMixedPaymentRestoresPointsCancelsEarnedPointsRestoresStockAndSavesRefund() {
        Member member = member(1L, 1_000);
        Product keyboard = product(10L, "Keyboard", 8);
        Product mouse = product(11L, "Mouse", 4);
        Order order = paidOrder(100L, member.getId(), 30_000, 5_000, List.of(
                orderItem(1000L, "Keyboard", 20_000, 1, keyboard),
                orderItem(1001L, "Mouse", 10_000, 1, mouse)
        ));
        Payment payment = completedPayment(200L, order, "portone-200", 25_000);
        ReflectionTestUtils.setField(order, "payment", payment);
        ReflectionTestUtils.setField(payment, "order", order);

        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            ReflectionTestUtils.setField(refund, "id", 300L);
            return refund;
        });
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(paymentRepository.findByIdWithOrderAndItems(payment.getId())).thenReturn(Optional.of(payment));

        RefundResponse response = refundService.refundCompletedPayment(payment, "simple change of mind");

        ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository).save(refundCaptor.capture());
        Refund savedRefund = refundCaptor.getValue();

        ArgumentCaptor<PointTransaction> pointCaptor = ArgumentCaptor.forClass(PointTransaction.class);
        verify(pointTransactionRepository, times(2)).save(pointCaptor.capture());

        assertAll(
                () -> assertEquals(payment, savedRefund.getPayment()),
                () -> assertEquals(25_000L, savedRefund.getPgRefundAmount()),
                () -> assertEquals(5_000, savedRefund.getPointRefundAmount()),
                () -> assertEquals("simple change of mind", savedRefund.getReason()),
                () -> assertEquals(PaymentStatus.REFUNDED, payment.getStatus()),
                () -> assertEquals(OrderStatus.CANCELLED, order.getOrderStatus()),
                () -> assertEquals(9, keyboard.getStockQuantity()),
                () -> assertEquals(5, mouse.getStockQuantity()),
                () -> assertEquals(5_750, member.getPointBalance()),
                () -> assertEquals(300L, response.refundId()),
                () -> assertEquals(200L, response.paymentId()),
                () -> assertEquals(25_000L, response.pgRefundAmount()),
                () -> assertEquals(5_000, response.pointRefundAmount())
        );

        List<PointTransaction> transactions = pointCaptor.getAllValues();
        assertEquals(PointTransactionType.USE_CANCEL, transactions.get(0).getType());
        assertEquals(PointTransactionType.EARN_CANCEL, transactions.get(1).getType());
    }

    @Test
    void refundCompletedPaymentRechecksPaymentStatusInsideTransaction() {
        Member member = member(1L, 1_000);
        Order order = paidOrder(100L, member.getId(), 30_000, 0, List.of());
        Payment payment = completedPayment(200L, order, "portone-200", 30_000);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.REFUNDED);
        ReflectionTestUtils.setField(order, "payment", payment);

        when(paymentRepository.findByIdWithOrderAndItems(payment.getId())).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStatusException.class,
                () -> refundService.refundCompletedPayment(payment, "simple change of mind"));

        verify(refundRepository, never()).save(any(Refund.class));
        verify(pointTransactionRepository, never()).save(any(PointTransaction.class));
    }

    @Test
    void refundCompletedPaymentRechecksOrderStatusInsideTransaction() {
        Member member = member(1L, 1_000);
        Order order = paidOrder(100L, member.getId(), 30_000, 0, List.of());
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.CANCELLED);
        Payment payment = completedPayment(200L, order, "portone-200", 30_000);
        ReflectionTestUtils.setField(order, "payment", payment);

        when(paymentRepository.findByIdWithOrderAndItems(payment.getId())).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStatusException.class,
                () -> refundService.refundCompletedPayment(payment, "simple change of mind"));

        verify(refundRepository, never()).save(any(Refund.class));
        verify(pointTransactionRepository, never()).save(any(PointTransaction.class));
    }

    @Test
    void refundCompletedPaymentRechecksDuplicateRefundInsideTransaction() {
        Member member = member(1L, 1_000);
        Order order = paidOrder(100L, member.getId(), 30_000, 0, List.of());
        Payment payment = completedPayment(200L, order, "portone-200", 30_000);
        ReflectionTestUtils.setField(order, "payment", payment);

        when(paymentRepository.findByIdWithOrderAndItems(payment.getId())).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPaymentId(payment.getId())).thenReturn(true);

        assertThrows(DuplicateRefundException.class,
                () -> refundService.refundCompletedPayment(payment, "simple change of mind"));

        verify(refundRepository, never()).save(any(Refund.class));
        verify(pointTransactionRepository, never()).save(any(PointTransaction.class));
    }

    @Test
    void refundCompletedPaymentCancelsOnlyAvailableEarnedPointsWhenAlreadyPartiallySpent() {
        // paidAmount=30,000 → earnedPointToCancel=300, but member only has 100 left
        Member member = member(1L, 100);
        Order order = paidOrder(100L, member.getId(), 30_000, 0, List.of());
        Payment payment = completedPayment(200L, order, "portone-200", 30_000);
        ReflectionTestUtils.setField(order, "payment", payment);

        when(paymentRepository.findByIdWithOrderAndItems(payment.getId())).thenReturn(Optional.of(payment));
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> {
            Refund refund = invocation.getArgument(0);
            ReflectionTestUtils.setField(refund, "id", 300L);
            return refund;
        });

        refundService.refundCompletedPayment(payment, "simple change of mind");

        // 300 중 100만 취소 가능 → 잔액 0
        assertEquals(0, member.getPointBalance());

        // USE_CANCEL 없고(usedPoint=0), EARN_CANCEL 1건만 저장됨
        ArgumentCaptor<PointTransaction> pointCaptor = ArgumentCaptor.forClass(PointTransaction.class);
        verify(pointTransactionRepository, times(1)).save(pointCaptor.capture());
        PointTransaction earnCancelTx = pointCaptor.getValue();
        assertEquals(PointTransactionType.EARN_CANCEL, earnCancelTx.getType());
        assertEquals(100, earnCancelTx.getAmount());
    }

    private Member member(Long id, int pointBalance) {
        Member member = new Member("test@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", id);
        ReflectionTestUtils.setField(member, "pointBalance", pointBalance);
        return member;
    }

    private Product product(Long id, String name, int stockQuantity) {
        Product product = newInstance(Product.class);
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "name", name);
        ReflectionTestUtils.setField(product, "stockQuantity", stockQuantity);
        return product;
    }

    private Order paidOrder(Long id, Long memberId, int totalAmount, int usedPoint, List<OrderItem> orderItems) {
        Order order = newInstance(Order.class);
        Member member = member(memberId, 0);
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "member", member);
        ReflectionTestUtils.setField(order, "totalAmount", totalAmount);
        ReflectionTestUtils.setField(order, "usedPoint", usedPoint);
        ReflectionTestUtils.setField(order, "orderItems", orderItems);
        orderItems.forEach(item -> item.setOrder(order));
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PAID);
        return order;
    }

    private OrderItem orderItem(Long id, String name, int price, int quantities, Product product) {
        OrderItem orderItem = newInstance(OrderItem.class);
        ReflectionTestUtils.setField(orderItem, "id", id);
        ReflectionTestUtils.setField(orderItem, "name", name);
        ReflectionTestUtils.setField(orderItem, "price", price);
        ReflectionTestUtils.setField(orderItem, "quantities", quantities);
        ReflectionTestUtils.setField(orderItem, "product", product);
        return orderItem;
    }

    private Payment completedPayment(Long id, Order order, String portOnePaymentId, int paidAmount) {
        Payment payment = new Payment(order);
        ReflectionTestUtils.setField(payment, "id", id);
        ReflectionTestUtils.setField(payment, "portOnePaymentId", portOnePaymentId);
        ReflectionTestUtils.setField(payment, "paidAmount", paidAmount);
        ReflectionTestUtils.setField(payment, "status", PaymentStatus.COMPLETED);
        return payment;
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
