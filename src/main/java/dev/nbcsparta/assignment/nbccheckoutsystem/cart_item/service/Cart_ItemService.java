package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.Cart_ItemRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.Cart_ItemResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.Cart_ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Cart_ItemService {


    private final Cart_ItemRepository cartItemRepository;

    @Transactional
    public Cart_ItemResponse addCartItem(String email, Cart_ItemRequest request){
        // TODO: MemberRepository, ProductRepository 구현 완료 후 로직 구현
        return null;
    }
}
