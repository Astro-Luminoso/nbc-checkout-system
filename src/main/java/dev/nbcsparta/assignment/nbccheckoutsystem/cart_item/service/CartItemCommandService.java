package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.CartItemNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.exception.ProductNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ForbiddenCartItemException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemCommandService {


    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CartItemValidator cartItemValidator;

    @Transactional
    public CartItemResponse addCartItem(Long memberId, CartItemRequest request) {

        Member members = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(ProductNotFoundException::new);

        cartItemValidator.validateQuantity(product, request.quantity());


        CartItem cartItem = cartItemRepository
                .findByMembersAndProduct(members, product)
                .map(existing -> {
                    int newQuantity = existing.getQuantities() + request.quantity();
                    cartItemValidator.validateQuantity(product, newQuantity);
                    existing.addQuantity(request.quantity());
                    return existing;
                })
                .orElseGet(() -> cartItemRepository.save(
                        new CartItem(members, product, request.quantity())
                ));

        return CartItemResponse.from(cartItem);
    }



    @Transactional
    public UpdateCartItemResponse updateCartItem(Long memberId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = findOwnedCartItem(memberId, cartItemId);

        cartItemValidator.validateQuantity(cartItem.getProduct(), request.quantity());

        cartItem.updateQuantity(request.quantity());
        return new UpdateCartItemResponse(cartItem.getId(), cartItem.getQuantities());
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = findOwnedCartItem(memberId,cartItemId);
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void deleteAllCartItems(Long memberId) {
        Member members = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        cartItemRepository.deleteAllByMembers(members);
    }

    // 조회 + 소유권 검증
    private CartItem findOwnedCartItem(Long memberId, Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(CartItemNotFoundException::new);
        if (!cartItem.getMembers().getId().equals(memberId)){
            throw new ForbiddenCartItemException();
        }
        return cartItem;
    }

    @Transactional
    public void deletePurchasedCartItems(Long memberId, List<Product> products) {
        Member members = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (products != null && !products.isEmpty()) {
            cartItemRepository.deleteByMembersAndProductIn(members, products);
        }
    }
}
