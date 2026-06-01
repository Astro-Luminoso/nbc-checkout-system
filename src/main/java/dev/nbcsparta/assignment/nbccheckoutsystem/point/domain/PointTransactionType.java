package dev.nbcsparta.assignment.nbccheckoutsystem.point.domain;

public enum PointTransactionType {
    USE,         // 결제 시 사용
    EARN,        // 결제 완료 시 적립
    USE_CANCEL,  // 환불 시 사용 포인트 복구
    EARN_CANCEL  // 환불 시 적립 포인트 회수
}
