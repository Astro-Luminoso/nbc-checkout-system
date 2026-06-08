package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemCommandService;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemQueryService;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemCommandService cartItemCommandService;
    private final CartItemQueryService cartItemQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addCartItem(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CartItemRequest request
    ) {
        CartItemResponse response = cartItemCommandService.addCartItem(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<GetCartResponse>> getCartItems(
            @AuthenticationPrincipal Long memberId
    ) {
        GetCartResponse response = cartItemQueryService.getCartItems(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<UpdateCartItemResponse>> updateCartItem(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        UpdateCartItemResponse response = cartItemCommandService.updateCartItem(memberId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> deleteCartItem(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cartItemId
    ) {
        cartItemCommandService.deleteCartItem(memberId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("장바구니 항목이 성공적으로 삭제되었습니다."));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllCartItems(
            @AuthenticationPrincipal Long memberId
    ) {
        cartItemCommandService.deleteAllCartItems(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
