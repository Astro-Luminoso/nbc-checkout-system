package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.NotValidatedPaidAmountException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public void savePaymentSuccess(Long paymentId, Long memberId, int paidAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
        Order order = payment.getOrder();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        // 주문 상태 반영 및 결제 승인금액 기록
        order.paymentResult((long) paidAmount);

        // 결제 승인 금액 DB 반영
        payment.setPaidAmount(paidAmount);

        if (order.getOrderStatus() == OrderStatus.PAID) {
            payment.confirmSuccess(LocalDateTime.now());

            // 1. 포인트 사용(차감) 및 적립(결제금액의 1%)
            member.deductPointBalance(order.getUsedPoint());
            pointTransactionRepository.save(PointTransaction.createUse(member, order.getUsedPoint(), order));

            member.addPointBalance(payment.getPaidAmount());
            pointTransactionRepository.save(PointTransaction.createEarn(member, (int) (paidAmount * 0.01), order));

        } else {
            // 결제 상태가 PAID로 가지 못한 경우 강제 예외 발생을 통해 전체 롤백 유도
            throw new NotValidatedPaidAmountException();
        }
    }

    @Transactional
    public void savePaymentFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
        
        // 중복 실패 처리 및 재고 중복 복구 차단
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        Order order = payment.getOrder();

        order.paymentResult(0L); // DECLINED 전이
        payment.confirmFailed();

        // 재고 복구
        restoreStock(order);
    }

    private void restoreStock(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.restoreStockValue(item.getQuantities());
                }
            }
        }
    }

    @Transactional
    public Payment savePayment(Order order) {
        Payment payment = new Payment(order);
        payment = paymentRepository.save(payment);

        return payment;
    }
}
