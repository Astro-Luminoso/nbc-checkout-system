package dev.nbcsparta.assignment.nbccheckoutsystem.order.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.CreateOrderData;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.SpecificOrderDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<OrderResponse<CreateOrderData>> createOrder(
            @Valid @RequestBody OrderCreateRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new OrderResponse<>(true, orderService.createOrder(memberId, request))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse<SpecificOrderDetail>> getOrderById(
            @PathVariable long id,
            @AuthenticationPrincipal Long memberId
    ) {
        SpecificOrderDetail data = orderService.getOrderDetail(id, memberId);

        return ResponseEntity.status(HttpStatus.OK).body(
                new OrderResponse<>(true, data)
        );
    }
}
