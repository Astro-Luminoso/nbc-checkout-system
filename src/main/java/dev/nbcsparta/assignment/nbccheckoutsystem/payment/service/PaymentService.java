package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(PaymentNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyPaidPortOnePaymentId(String portOnePaymentId) {
        return paymentRepository.existsByPortOnePaymentIdAndStatus(portOnePaymentId, PaymentStatus.COMPLETED);
    }

    @Transactional
    public void savePaymentSuccess(Long paymentId, int paidAmount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);

        // 결제 승인 금액 DB 반영
        payment.setPaidAmount(paidAmount);
        payment.confirmSuccess(LocalDateTime.now());
    }

    @Transactional
    public void savePaymentFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
        
        // 중복 실패 처리 방지
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.confirmFailed();
    }

    @Transactional
    public Payment savePayment(Order order) {
        Payment payment = new Payment(order);
        payment = paymentRepository.save(payment);

        return payment;
    }
}
