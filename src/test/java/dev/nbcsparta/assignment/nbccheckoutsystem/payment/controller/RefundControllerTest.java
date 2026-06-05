package dev.nbcsparta.assignment.nbccheckoutsystem.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.GlobalExceptionHandler;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.dto.RefundResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.DuplicateRefundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.ForbiddenPaymentException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.InvalidPaymentStatusException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.PaymentNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.exception.RefundFailedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.service.RefundCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

class RefundControllerTest {

    @Mock
    private RefundCommandService refundCommandService;

    private MockMvc mockMvc;
    private Long resolvedMemberId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolvedMemberId = 1L;
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RefundController(refundCommandService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(memberIdResolver())
                .build();
    }

    @Test
    void refundReturnsSuccessResponse() throws Exception {
        when(refundCommandService.refund(eq(10L), eq(1L), any(RefundRequest.class)))
                .thenReturn(new RefundResponse(
                        300L,
                        10L,
                        25_000L,
                        5_000,
                        "simple change of mind",
                        PaymentStatus.REFUNDED,
                        OrderStatus.CANCELLED
                ));

        mockMvc.perform(post("/api/payments/{paymentId}/refunds", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": "simple change of mind"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refundId").value(300))
                .andExpect(jsonPath("$.data.paymentId").value(10))
                .andExpect(jsonPath("$.data.pgRefundAmount").value(25000))
                .andExpect(jsonPath("$.data.pointRefundAmount").value(5000))
                .andExpect(jsonPath("$.data.paymentStatus").value("REFUNDED"))
                .andExpect(jsonPath("$.data.orderStatus").value("CANCELLED"));
    }

    @Test
    void refundReturnsBadRequestWhenReasonIsBlank() throws Exception {
        mockMvc.perform(post("/api/payments/{paymentId}/refunds", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reason": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refundReturnsNotFoundWhenPaymentDoesNotExist() throws Exception {
        when(refundCommandService.refund(eq(10L), eq(1L), any(RefundRequest.class)))
                .thenThrow(new PaymentNotFoundException());

        performRefund()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refundReturnsForbiddenWhenPaymentOwnerDoesNotMatch() throws Exception {
        when(refundCommandService.refund(eq(10L), eq(1L), any(RefundRequest.class)))
                .thenThrow(new ForbiddenPaymentException());

        performRefund()
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refundReturnsConflictWhenPaymentStatusIsInvalid() throws Exception {
        when(refundCommandService.refund(eq(10L), eq(1L), any(RefundRequest.class)))
                .thenThrow(new InvalidPaymentStatusException());

        performRefund()
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refundReturnsConflictWhenRefundAlreadyExists() throws Exception {
        when(refundCommandService.refund(eq(10L), eq(1L), any(RefundRequest.class)))
                .thenThrow(new DuplicateRefundException());

        performRefund()
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refundReturnsBadGatewayWhenPortOneCancelFails() throws Exception {
        when(refundCommandService.refund(eq(10L), eq(1L), any(RefundRequest.class)))
                .thenThrow(new RefundFailedException());

        performRefund()
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refundReturnsUnauthorizedWhenMemberIdIsMissing() throws Exception {
        resolvedMemberId = null;

        performRefund()
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions performRefund() throws Exception {
        return mockMvc.perform(post("/api/payments/{paymentId}/refunds", 10L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "reason": "simple change of mind"
                        }
                        """));
    }

    private HandlerMethodArgumentResolver memberIdResolver() {
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(
                    org.springframework.core.MethodParameter parameter,
                    org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                    org.springframework.web.context.request.NativeWebRequest webRequest,
                    org.springframework.web.bind.support.WebDataBinderFactory binderFactory
            ) {
                return resolvedMemberId;
            }
        };
    }
}
