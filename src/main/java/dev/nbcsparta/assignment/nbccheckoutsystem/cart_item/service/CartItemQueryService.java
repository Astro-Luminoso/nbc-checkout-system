package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;


import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.CartItemNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ForbiddenCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemQueryService {

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;


    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByMemberIdIn(long memberId, List<Long> cartItemIds) {
        return cartItemRepository.findByMemberIdIn(memberId, cartItemIds);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByMemberIdIn(List<Long> cartItemIds) {
        return cartItemRepository.findAllByIdIn(cartItemIds);
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByMemberId(long memberId) {
        return cartItemRepository.findAllByMemberId(memberId);
    }

    @Transactional
    public void deletePurchasedCartItems(Long memberId, List<Product> products) {
        Member members = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (products != null && !products.isEmpty()) {
            cartItemRepository.deleteByMembersAndProductIn(members, products);
        }
    }


    // 조회 + 소유권 검증
    public CartItem findOwnedCartItem(Long memberId, Long cartItemId){
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(CartItemNotFoundException::new);
        if (!cartItem.getMember().getId().equals(memberId)){
            throw new ForbiddenCartItemException();
        }
        return cartItem;
    }

    public CartItem getByMemberAndProduct(Member member, Product product) {
        return cartItemRepository.findByMemberAndProduct(member, product);
    }

}
