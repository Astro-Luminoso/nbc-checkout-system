package dev.nbcsparta.assignment.nbccheckoutsystem.payment.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentConfirmRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentConfirmResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.AlreadyPaidOrderException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.UnauthorizeAccessException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.UnexpectedPaymentFailException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGateway;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.port.PaymentGatewayResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemService;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentService paymentService;
    private final CartItemService cartItemService;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request, Long memberId) {
        Payment payment = paymentRepository.findByOrderIdWithOrder(request.orderId())
                .orElseThrow(PaymentNotFoundException::new);
        return processConfirmation(payment, request.portOnePaymentId(), memberId, true);
    }

    public PaymentConfirmResponse processConfirmation(Payment payment, String portOnePaymentId, Long memberId, boolean checkOwnership) {
        Order order = payment.getOrder();

        // 소유자 검증 (로그인한 멤버의 주문인지 확인, API 호출시에만 수행)
        if (checkOwnership && !order.getMemberId().equals(memberId)) {
            throw new UnauthorizeAccessException();
        }

        // 멱등성 검증: 이미 성공 처리 완료된 건이면 상태 변경 없이 성공 응답 반환
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return new PaymentConfirmResponse(
                    order.getId(),
                    portOnePaymentId,
                    PaymentStatus.COMPLETED
            );
        }

        // 중복 결제 검증 (이미 완료된 동일 포트원 결제ID가 존재하는지 확인)
        if (paymentRepository.existsByPortOnePaymentIdAndStatus(portOnePaymentId, PaymentStatus.COMPLETED)) {
            throw new AlreadyPaidOrderException();
        }

        try {
            // 포트원 실제 결제 정보 조회 (트랜잭션 바깥)
            PaymentGatewayResponse pgResponse = paymentGateway.getPayment(portOnePaymentId);

            // 결제 상태 검증 및 금액 위변조 검증
            boolean isPaid = "PAID".equals(pgResponse.status());
            boolean isAmountMatch = (order.getTotalAmount() - order.getUsedPoint()) == pgResponse.totalAmount();

            if (isPaid && isAmountMatch) {
                // 성공 시 DB 트랜잭션 반영 호출
                paymentService.savePaymentSuccess(payment.getId(), portOnePaymentId, order.getMemberId(), pgResponse.totalAmount());

                List<Product> orderedProducts = order.getOrderItems().stream()
                        .map(OrderItem::getProduct)
                        .toList();
                cartItemService.deletePurchasedCartItems(order.getMemberId(), orderedProducts);
            } else {
                // 검증 실패 시 DB 트랜잭션 실패 반영 호출 후 보상 취소 요청
                paymentService.savePaymentFailed(payment.getId());
                if (isPaid) {
                    cancelPaymentWithLogging(portOnePaymentId, "Verification failed: Amount mismatch");
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 DB 트랜잭션 실패 반영 호출 후 결제 보상 취소 요청
            paymentService.savePaymentFailed(payment.getId());
            cancelPaymentWithLogging(portOnePaymentId, "Exception during confirmation: " + e.getMessage());
            throw new UnexpectedPaymentFailException();
        }

        Payment updatedPayment = paymentRepository.findById(payment.getId())
                .orElseThrow(PaymentNotFoundException::new);

        return new PaymentConfirmResponse(
                order.getId(),
                portOnePaymentId,
                updatedPayment.getStatus()
        );
    }

    private void cancelPaymentWithLogging(String portOnePaymentId, String reason) {
        try {
            paymentGateway.cancelPayment(portOnePaymentId, reason);
        } catch (Exception cancelEx) {
            log.error("CRITICAL: PortOne payment cancellation failed! " +
                    "PortOnePaymentId: {}, Reason: {}, Error: {}",
                    portOnePaymentId, reason, cancelEx.getMessage(), cancelEx);
        }
    }
}
