package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.CartItemRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.CartItemResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemService {


    private final CartItemRepository cartItemRepository;

    // 상품 담기
    @Transactional
    public CartItemResponse addCartItem(String email, CartItemRequest request){
        // TODO: MemberRepository 머지 후 구현
        return null;
    }

}
