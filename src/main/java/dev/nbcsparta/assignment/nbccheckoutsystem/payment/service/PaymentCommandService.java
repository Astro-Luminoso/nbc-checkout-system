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
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.InvalidPaymentIdentifierException;
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

        // 멱등성 검증: 이미 종료 상태(COMPLETED, FAILED, REFUNDED)이면 상태 변경 없이 해당 상태로 응답 반환
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return new PaymentConfirmResponse(
                    order.getId(),
                    portOnePaymentId,
                    payment.getStatus()
            );
        }

        // 중복 결제 검증 (이미 완료된 동일 포트원 결제ID가 존재하는지 확인)
        if (paymentRepository.existsByPortOnePaymentIdAndStatus(portOnePaymentId, PaymentStatus.COMPLETED)) {
            throw new AlreadyPaidOrderException();
        }

        // 서버 발급 결제 ID와 요청 결제 ID 일치 검증
        if (!payment.getPortOnePaymentId().equals(portOnePaymentId)) {
            throw new InvalidPaymentIdentifierException();
        }

        try {
            // 포트원 실제 결제 정보 조회 (트랜잭션 바깥)
            PaymentGatewayResponse pgResponse = paymentGateway.getPayment(portOnePaymentId);

            // PG 응답의 ID 일치 이중 검증
            if (!pgResponse.id().equals(portOnePaymentId)) {
                throw new InvalidPaymentIdentifierException();
            }

            // 결제 상태 검증 및 금액 위변조 검증
            boolean isPaid = "PAID".equals(pgResponse.status());
            boolean isAmountMatch = (order.getTotalAmount() - order.getUsedPoint()) == pgResponse.totalAmount();

            if (isPaid && isAmountMatch) {
                // 성공 시 DB 트랜잭션 반영 호출
                paymentService.savePaymentSuccess(payment.getId(), order.getMemberId(), pgResponse.totalAmount());

                List<Product> orderedProducts = order.getOrderItems().stream()
                        .map(OrderItem::getProduct)
                        .toList();
                cartItemService.deletePurchasedCartItems(order.getMemberId(), orderedProducts);
            } else if (isPaid && !isAmountMatch) {
                // 결제는 성공했으나 금액 불일치 -> 악의적 접근이거나 설정 오류
                // 이 경우 결제를 취소해야 함, 그리고 우리 서버 상태는 FAILED로 마킹
                paymentService.savePaymentFailed(payment.getId());
                cancelPaymentWithLogging(portOnePaymentId, "Verification failed: Amount mismatch");
            } else {
                // isPaid가 아닌 경우 (예: READY, FAILED, CANCELLED 등 명시적 실패 응답)
                // 이 경우 FAILED 전환 및 재고 복구 (단, 포트원 결제 자체는 안 된 상태이므로 취소 요청 불필요)
                paymentService.savePaymentFailed(payment.getId());
            }
        } catch (InvalidPaymentIdentifierException e) {
            throw e; // 취소 및 실패 처리 없이 에러 반환
        } catch (Exception e) {
            // 알 수 없는 장애(네트워크 예외 등) 발생 시 재고 복구(FAILED 전환)를 수행하지 않음
            // 취소 요청도 함부로 보내지 않고 PENDING 상태 유지, Webhook 또는 배치 대기
            log.error("Payment confirmation unknown error for portOnePaymentId {}: {}", portOnePaymentId, e.getMessage(), e);
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
