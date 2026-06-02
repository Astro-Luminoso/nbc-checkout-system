package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.CartItemRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.CartItemResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemService {


    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItemResponse addCartItem(Long memberId, CartItemRequest request){

        Members members = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Product product = productRepository.findById(request.product_id())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 상품입니다."));

        if (request.quantity() > product.getStock_quantity()){
            throw new OutOfStockException();
        }

        CartItem cartItem = cartItemRepository
                .findByMembersAndProductId(members, product)
                .map(existing -> {
                    int newQuantity = existing.getQuantity() + request.quantity();

                    if (newQuantity > product.getStock_quantity()){
                        throw new OutOfStockException();
                    }

                    existing.addQuantity(request.quantity());
                    return existing;
                })
                .orElseGet(()-> cartItemRepository.save(
                        new CartItem(members, product, request.quantity())
                ));

        return CartItemResponse.from(cartItem);
    }

}
