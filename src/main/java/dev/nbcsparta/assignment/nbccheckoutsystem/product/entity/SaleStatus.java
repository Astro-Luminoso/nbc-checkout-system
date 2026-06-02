package dev.nbcsparta.assignment.nbccheckoutsystem.product.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SaleStatus {
    ON_SALE,
    OUT_OF_STOCK,
    DISCONTINUED;

    @JsonCreator
    public static SaleStatus getStatus(String status) {

        return Arrays.stream(SaleStatus.values())
                .filter(saleStatus -> saleStatus.name().equals(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid order status: " + status));
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }
}
