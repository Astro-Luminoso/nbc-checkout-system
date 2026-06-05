package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.DuplicateRefundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.ForbiddenPaymentException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.InvalidPaymentStatusException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.RefundFailedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGateway;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundCommandService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentGateway paymentGateway;
    private final RefundService refundService;

    public RefundResponse refund(Long paymentId, Long memberId, RefundRequest request) {
        Payment payment = paymentRepository.findByIdWithOrderAndItems(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
        Order order = payment.getOrder();
        // 주문을 만든 회원과 현재 로그인한 회원이 같은지 확인
        if (!order.getMemberId().equals(memberId)) {
            throw new ForbiddenPaymentException();
        }
        // 결제 완료 상태인지 확인
        if (payment.getStatus() != PaymentStatus.COMPLETED || order.getOrderStatus() != OrderStatus.PAID) {
            throw new InvalidPaymentStatusException();
        }
        // 이 결제에 대한 환불 이력이 있는지 확인
        if (refundRepository.existsByPaymentId(paymentId)) {
            throw new DuplicateRefundException();
        }
        // 실제 포트원 결제 금액이 있는지 확인
        if (payment.getPaidAmount() > 0) {
            try {
                paymentGateway.cancelPayment(payment.getPortOnePaymentId(), request.reason());
            } catch (Exception exception) {
                throw new RefundFailedException();
            }
        }

        try {
            return refundService.refundCompletedPayment(payment, request.reason());
        } catch (Exception e) {
            // PG 취소는 이미 완료됐지만 DB 반영에 실패한 상태 — 수동 복구 필요
            log.error("CRITICAL: PG cancellation succeeded but DB update failed. " +
                    "PaymentId: {}, PortOnePaymentId: {}, Error: {}",
                    paymentId, payment.getPortOnePaymentId(), e.getMessage(), e);
            throw new RefundFailedException();
        }
    }
}
