package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.DuplicateRefundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.ForbiddenPaymentException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.InvalidPaymentStatusException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.RefundFailedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGateway;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.RefundRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefundCommandServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private RefundService refundService;

    private RefundCommandService refundCommandService;

    @BeforeEach
    void setUp() {
        refundCommandService = new RefundCommandService(
                paymentRepository,
                refundRepository,
                paymentGateway,
                refundService
        );
    }

    @Test
    void refundCompletedCardPaymentPersistsRefundThenCallsPortOne() {
        long paymentId = 10L;
        long memberId = 1L;
        Payment payment = payment(paymentId, memberId, 30_000, 0, 30_000, PaymentStatus.COMPLETED);
        RefundRequest request = new RefundRequest("simple change of mind");

        when(paymentRepository.findByIdWithOrderAndItems(paymentId)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPaymentId(paymentId)).thenReturn(false);
        when(refundService.refundCompletedPayment(payment, request.reason())).thenReturn(
                new RefundResponse(1L, paymentId, 30_000L, 0, request.reason(), PaymentStatus.REFUNDED, OrderStatus.CANCELLED)
        );

        RefundResponse response = refundCommandService.refund(paymentId, memberId, request);

        InOrder inOrder = inOrder(refundService, paymentGateway);
        inOrder.verify(refundService).refundCompletedPayment(payment, request.reason());
        inOrder.verify(paymentGateway).cancelPayment(payment.getPortOnePaymentId(), request.reason());

        assertEquals(1L, response.refundId());
        assertEquals(paymentId, response.paymentId());
        assertEquals(30_000L, response.pgRefundAmount());
        assertEquals(0, response.pointRefundAmount());
        assertEquals(PaymentStatus.REFUNDED, response.paymentStatus());
        assertEquals(OrderStatus.CANCELLED, response.orderStatus());
    }

    @Test
    void refundPointOnlyPaymentDoesNotCallPortOne() {
        long paymentId = 10L;
        long memberId = 1L;
        Payment payment = payment(paymentId, memberId, 30_000, 30_000, 0, PaymentStatus.COMPLETED);
        RefundRequest request = new RefundRequest("simple change of mind");

        when(paymentRepository.findByIdWithOrderAndItems(paymentId)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPaymentId(paymentId)).thenReturn(false);
        when(refundService.refundCompletedPayment(payment, request.reason())).thenReturn(
                new RefundResponse(1L, paymentId, 0L, 30_000, request.reason(), PaymentStatus.REFUNDED, OrderStatus.CANCELLED)
        );

        refundCommandService.refund(paymentId, memberId, request);

        verify(paymentGateway, never()).cancelPayment(anyString(), anyString());
        verify(refundService).refundCompletedPayment(payment, request.reason());
    }

    @Test
    void refundThrowsForbiddenWhenPaymentOwnerDoesNotMatch() {
        long paymentId = 10L;
        Payment payment = payment(paymentId, 1L, 30_000, 0, 30_000, PaymentStatus.COMPLETED);

        when(paymentRepository.findByIdWithOrderAndItems(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(ForbiddenPaymentException.class,
                () -> refundCommandService.refund(paymentId, 2L, new RefundRequest("simple change of mind")));

        verify(paymentGateway, never()).cancelPayment(anyString(), anyString());
        verify(refundService, never()).refundCompletedPayment(any(), anyString());
    }

    @Test
    void refundThrowsInvalidPaymentStatusWhenPaymentIsNotCompleted() {
        long paymentId = 10L;
        Payment payment = payment(paymentId, 1L, 30_000, 0, 30_000, PaymentStatus.PENDING);

        when(paymentRepository.findByIdWithOrderAndItems(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStatusException.class,
                () -> refundCommandService.refund(paymentId, 1L, new RefundRequest("simple change of mind")));

        verify(paymentGateway, never()).cancelPayment(anyString(), anyString());
        verify(refundService, never()).refundCompletedPayment(any(), anyString());
    }

    @Test
    void refundThrowsInvalidPaymentStatusWhenOrderIsNotPaid() {
        long paymentId = 10L;
        Payment payment = payment(paymentId, 1L, 30_000, 0, 30_000, PaymentStatus.COMPLETED);
        ReflectionTestUtils.setField(payment.getOrder(), "orderStatus", OrderStatus.CANCELLED);

        when(paymentRepository.findByIdWithOrderAndItems(paymentId)).thenReturn(Optional.of(payment));

        assertThrows(InvalidPaymentStatusException.class,
                () -> refundCommandService.refund(paymentId, 1L, new RefundRequest("simple change of mind")));

        verify(paymentGateway, never()).cancelPayment(anyString(), anyString());
        verify(refundService, never()).refundCompletedPayment(any(), anyString());
    }

    @Test
    void refundThrowsDuplicateRefundWhenRefundAlreadyExists() {
        long paymentId = 10L;
        Payment payment = payment(paymentId, 1L, 30_000, 0, 30_000, PaymentStatus.COMPLETED);

        when(paymentRepository.findByIdWithOrderAndItems(paymentId)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPaymentId(paymentId)).thenReturn(true);

        assertThrows(DuplicateRefundException.class,
                () -> refundCommandService.refund(paymentId, 1L, new RefundRequest("simple change of mind")));

        verify(paymentGateway, never()).cancelPayment(anyString(), anyString());
        verify(refundService, never()).refundCompletedPayment(any(), anyString());
    }

    @Test
    void refundPersistsButThrowsExceptionWhenPortOneCancelFails() {
        long paymentId = 10L;
        Payment payment = payment(paymentId, 1L, 30_000, 0, 30_000, PaymentStatus.COMPLETED);
        RefundRequest request = new RefundRequest("simple change of mind");

        when(paymentRepository.findByIdWithOrderAndItems(paymentId)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPaymentId(paymentId)).thenReturn(false);
        
        when(refundService.refundCompletedPayment(payment, request.reason())).thenReturn(
                new RefundResponse(1L, paymentId, 30_000L, 0, request.reason(), PaymentStatus.REFUNDED, OrderStatus.CANCELLED)
        );

        doThrow(new RefundFailedException())
                .when(paymentGateway).cancelPayment(payment.getPortOnePaymentId(), "simple change of mind");

        assertThrows(RefundFailedException.class,
                () -> refundCommandService.refund(paymentId, 1L, request));

        verify(refundService).refundCompletedPayment(payment, request.reason());
    }

    private Payment payment(
            Long paymentId,
            Long memberId,
            int totalAmount,
            int usedPoint,
            int paidAmount,
            PaymentStatus status
    ) {
        Order order = newInstance(Order.class);
        Member member = new Member("test@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(order, "member", member);
        ReflectionTestUtils.setField(order, "totalAmount", totalAmount);
        ReflectionTestUtils.setField(order, "usedPoint", usedPoint);
        ReflectionTestUtils.setField(order, "orderItems", List.of());
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.PAID);

        Payment payment = new Payment(order);
        ReflectionTestUtils.setField(payment, "id", paymentId);
        ReflectionTestUtils.setField(payment, "portOnePaymentId", "portone-" + paymentId);
        ReflectionTestUtils.setField(payment, "paidAmount", paidAmount);
        ReflectionTestUtils.setField(payment, "status", status);
        ReflectionTestUtils.setField(payment, "paidAt", LocalDateTime.now());
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
