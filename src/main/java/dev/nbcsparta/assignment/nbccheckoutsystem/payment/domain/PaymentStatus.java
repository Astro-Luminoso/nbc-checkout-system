package dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain;

public enum PaymentStatus {
    PENDING,    // 결제 대기 (사전 생성)
    COMPLETED,  // 결제 완료 (PG 승인 또는 포인트 전액 결제 완료)
    FAILED      // 결제 실패 (PG 거절, 검증 실패, 취소 등)
}
