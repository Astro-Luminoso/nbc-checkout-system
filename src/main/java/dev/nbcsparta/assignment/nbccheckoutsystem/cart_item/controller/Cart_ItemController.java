package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.controller;


import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.domain.Cart_Item;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.Cart_ItemRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.Cart_ItemResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.Cart_ItemService;
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
public class Cart_ItemController {

    private final Cart_ItemService cartItemService;


    @PostMapping
    public ResponseEntity<Cart_ItemResponse> addCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            // Spring Security가 JWT 필터에서 토큰을 파싱한 뒤
            // SecurityContextHolder에 저장함 @AuthenticationPrincipal은 그 정보를 파라미터로 꺼내줌

            @Valid @RequestBody Cart_ItemRequest request
            ) {
        String email = userDetails.getUsername(); // JWT에서 파싱된 이메일
        Cart_ItemResponse response = cartItemService.addCartItem(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
