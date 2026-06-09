package dev.nbcsparta.assignment.nbccheckoutsystem.payment.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentConfirmRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.PaymentConfirmResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.facade.PaymentFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
            @Valid @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal Long memberId) {
        PaymentConfirmResponse response = paymentFacade.confirmPayment(request, memberId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }
}
