package dev.nbcsparta.assignment.nbccheckoutsystem.payment.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.service.RefundCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/{paymentId}/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundCommandService refundCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<RefundResponse>> refund(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody RefundRequest request
    ) {
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefundResponse response = refundCommandService.refund(paymentId, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
