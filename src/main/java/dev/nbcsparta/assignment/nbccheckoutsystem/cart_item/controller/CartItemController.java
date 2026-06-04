package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemService;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public ResponseEntity<?> addCartItem(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CartItemRequest request
    ) {
        try {
            CartItemResponse response = cartItemService.addCartItem(memberId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
        } catch (OutOfStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getCartItems(
            @AuthenticationPrincipal Long memberId
    ) {
        try {
            GetCartResponse response = cartItemService.getCartItems(memberId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemRequest request
    ){
        try {
            UpdateCartItemResponse response = cartItemService.updateCartItem(memberId, cartItemId, request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch(OutOfStockException e){
            // 409 -> 변경수량 재고초과
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch(IllegalArgumentException e){
            // 403 or 404
            HttpStatus status = e.getMessage().contains("본인")?
                    HttpStatus.FORBIDDEN: HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status)
                    .body(Map.of("Message", e.getMessage()));
        }

    }
}
