package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentConfirmRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentConfirmResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.DuplicatePaymentException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.UnauthorizeAccessException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGateway;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGatewayResponse;
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
    private final CartItemRepository cartItemRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request, Long memberId) {

        // 중복 결제 검증 (이미 완료된 동일 포트원 결제ID가 존재하는지 확인)
        if (paymentRepository.existsByPortOnePaymentIdAndStatus(request.portOnePaymentId(), PaymentStatus.COMPLETED)) {
            throw new DuplicatePaymentException();
        }

        Payment payment = paymentRepository.findByOrderIdWithOrder(request.orderId())
                .orElseThrow(PaymentNotFoundException::new);

        Order order = payment.getOrder();

        // 소유자 검증 (로그인한 멤버의 주문인지 확인)
        if (!order.getMemberId().equals(memberId)) {
            throw new UnauthorizeAccessException();
        }

        Members member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        try {
            // 1. 포트원 실제 결제 정보 조회
            PaymentGatewayResponse pgResponse = paymentGateway.getPayment(request.portOnePaymentId());

            // 2. 결제 상태 검증 및 금액 위변조 검증
            boolean isPaid = "PAID".equals(pgResponse.status());
            order.paymentResult((long) pgResponse.totalAmount());

            if (isPaid && order.getOrderStatus() == OrderStatus.PAID) {
                // 결제 성공 및 검증 완료
                payment.confirmSuccess(request.portOnePaymentId(), LocalDateTime.now());

                // 3. 포인트 사용(차감) 및 적립(결제금액의 1%)
                if (order.getUsedPoint() > 0) {
                    member.deductPointBalance(order.getUsedPoint());
                    pointTransactionRepository.save(PointTransaction.createUse(member, order.getUsedPoint(), payment));
                }

                int earnPoint = (int) (payment.getTotalAmount() * 0.01);
                if (earnPoint > 0) {
                    member.addPointBalance(payment.getTotalAmount());
                    pointTransactionRepository.save(PointTransaction.createEarn(member, earnPoint, payment));
                }

                // 4. 장바구니 초기화
                cartItemRepository.deleteByMembers(member);
            } else {
                // 검증 실패 혹은 포트원 결제 미완료 상태 -> 포트원 결제 즉시 취소 처리 및 실패 처리
                if (isPaid) {
                    paymentGateway.cancelPayment(request.portOnePaymentId(), "Verification failed: Amount mismatch");
                }
                payment.confirmFailed();

                // 5. 재고 복구
                restoreStock(order);
            }
        } catch (Exception e) {
            // 연동 실패 또는 예외 발생 시 결제 실패 처리 및 포트원 결제 취소 시도
            try {
                paymentGateway.cancelPayment(request.portOnePaymentId(), "Exception during confirmation: " + e.getMessage());
            } catch (Exception cancelEx) {
                // 취소 실패 예외 무시
            }
            payment.confirmFailed();

            // 5. 재고 복구
            restoreStock(order);

            throw new IllegalStateException("Payment confirmation failed: " + e.getMessage(), e);
        }

        return new PaymentConfirmResponse(
                order.getId(),
                request.portOnePaymentId(),
                payment.getStatus()
        );
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
}
