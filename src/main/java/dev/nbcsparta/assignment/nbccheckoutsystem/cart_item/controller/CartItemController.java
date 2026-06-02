package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.controller;


import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.CartItemRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.CartItemResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;


    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CartItemRequest request
            ) {
        CartItemResponse response = cartItemService.addCartItem(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
