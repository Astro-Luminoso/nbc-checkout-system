package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
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
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.RefundRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransactionType;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public RefundResponse refundCompletedPayment(Payment payment, String reason) {
        payment = paymentRepository.findByIdWithOrderAndItems(payment.getId())
                .orElseThrow(PaymentNotFoundException::new);
        Order order = payment.getOrder();

        if (payment.getStatus() != PaymentStatus.COMPLETED || order.getOrderStatus() != OrderStatus.PAID) {
            throw new InvalidPaymentStatusException();
        }
        if (refundRepository.existsByPaymentId(payment.getId())) {
            throw new DuplicateRefundException();
        }

        Member member = memberRepository.findById(order.getMember().getId())
                .orElseThrow(MemberNotFoundException::new);

        int pointRefundAmount = order.getUsedPoint();
        long pgRefundAmount = payment.getPaidAmount();
        int earnedPointToCancel = (int) (payment.getPaidAmount() * 0.01);

        restoreStock(order);
        restoreUsedPoint(member, order, pointRefundAmount);
        cancelEarnedPoint(member, order, earnedPointToCancel);

        order.cancel();
        payment.refund();

        Refund refund = refundRepository.save(
                new Refund(payment, pgRefundAmount, pointRefundAmount, reason)
        );

        return RefundResponse.from(refund);
    }

    private void restoreStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.restoreStockValue(orderItem.getQuantities());
        }
    }

    private void restoreUsedPoint(Member member, Order order, int pointRefundAmount) {
        if (pointRefundAmount <= 0) {
            return;
        }

        member.restoreUsedPoint(pointRefundAmount);
        pointTransactionRepository.save(
                PointTransaction.createCancel(member, pointRefundAmount, PointTransactionType.USE_CANCEL, order)
        );
    }

    private void cancelEarnedPoint(Member member, Order order, int earnedPointToCancel) {
        if (earnedPointToCancel <= 0) {
            return;
        }

        // 다른 주문에서 적립 포인트를 이미 소진한 경우 잔액만큼만 취소
        int actual = Math.min(earnedPointToCancel, member.getPointBalance());
        if (actual <= 0) {
            return;
        }

        member.cancelEarnedPoint(actual);
        pointTransactionRepository.save(
                PointTransaction.createCancel(member, actual, PointTransactionType.EARN_CANCEL, order)
        );
    }
}
