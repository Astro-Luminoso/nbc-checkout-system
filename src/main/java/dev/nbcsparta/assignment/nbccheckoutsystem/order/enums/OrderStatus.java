package dev.nbcsparta.assignment.nbccheckoutsystem.order.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum OrderStatus {

    STANDBY, // 주문 생성 및 결재 대기
    PAID,    // 결제 완료
    CANCELLED, // 주문 취소
    DECLINED;


    @JsonCreator
    public static OrderStatus getStatus(String status) {

        return Arrays.stream(OrderStatus.values())
                .filter(orderStatus -> orderStatus.name().equals(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid order status: " + status));
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }

}
