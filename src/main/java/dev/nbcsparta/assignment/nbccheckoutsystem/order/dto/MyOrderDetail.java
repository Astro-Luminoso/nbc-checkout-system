package dev.nbcsparta.assignment.nbccheckoutsystem.order.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record MyOrderDetail(
        List<SimpleOrderDetail> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static MyOrderDetail from(Page<SimpleOrderDetail> contents) {
        return new MyOrderDetail(
                contents.getContent(),
                contents.getNumber(),
                contents.getSize(),
                contents.getTotalElements(),
                contents.getTotalPages()

        );
    }
}
