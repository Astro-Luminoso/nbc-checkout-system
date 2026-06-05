package dev.nbcsparta.assignment.nbccheckoutsystem.order.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderData>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(orderService.createOrder(memberId, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecificOrderDetail>> getOrderById(
            @PathVariable long id,
            @AuthenticationPrincipal Long memberId
    ) {
        SpecificOrderDetail data = orderService.getOrderDetail(id, memberId);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MyOrderDetail>> getMyOrders (
            @PageableDefault(size = 20)Pageable pageable,
            @AuthenticationPrincipal Long memberId) {
        MyOrderDetail data = orderService.getMyOrderDetail(memberId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(data));
    }

    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<OrderPreviewDetail>> getOrderPreview(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(value = "items", required = false) String items
    ) {
        OrderPreviewDetail data = orderService.getOrderPreview(memberId, items);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(data));
    }
}
