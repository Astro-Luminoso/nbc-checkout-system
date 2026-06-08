package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;


import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.GetCartItemResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.GetCartResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemQueryService {

    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public GetCartResponse getCartItems(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        List<CartItem> cartItems = cartItemRepository.findAllByMembers(member);

        List<GetCartItemResponse> items = cartItems.stream()
                .map(GetCartItemResponse::from)
                .toList();

        int totalAmount = items.stream()
                .mapToInt(GetCartItemResponse::lineAmount)
                .sum();

        return new GetCartResponse(items, totalAmount);
    }
}
