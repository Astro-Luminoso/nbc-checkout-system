package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemQueryService;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.service.MemberService;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.NotValidatedPaidAmountException;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.service.PointService;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final PointService pointService;
    private final CartItemQueryService cartItemQueryService;

    @Transactional
    public void processPaymentSuccess(Payment payment, Order order, int paidAmount) {
        // 1. 주문 상태 반영
        order.paymentResult((long) paidAmount);
        
        if (order.getOrderStatus() != dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus.PAID) {
            throw new NotValidatedPaidAmountException();
        }

        // 2. 결제 상태 승인 처리
        paymentService.savePaymentSuccess(payment.getId(), paidAmount);

        Long memberId = order.getMember().getId();
        Member member = memberService.getMemberById(memberId);

        // 3. 포인트 처리
        int usedPoint = order.getUsedPoint();
        if (usedPoint > 0) {
            memberService.deductPoint(memberId, usedPoint);
            pointService.recordPointUse(member, usedPoint, order);
        }

        int earnedPoint = (int) (paidAmount * 0.01);
        if (earnedPoint > 0) {
            memberService.addPoint(memberId, earnedPoint);
            pointService.recordPointEarn(member, earnedPoint, order);
        }

        // 4. 장바구니 구매 항목 비우기
        List<Product> orderedProducts = order.getOrderItems().stream()
                .map(OrderItem::getProduct)
                .toList();
        cartItemQueryService.deletePurchasedCartItems(memberId, orderedProducts);
    }

    @Transactional
    public void processPaymentFailed(Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        
        // 중복 실패 처리 방지
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        Order order = payment.getOrder();
        order.paymentResult(0L); // DECLINED 전이
        paymentService.savePaymentFailed(paymentId);

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
}
