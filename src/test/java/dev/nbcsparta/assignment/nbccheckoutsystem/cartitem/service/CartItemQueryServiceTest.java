package dev.nbcsparta.assignment.nbccheckoutsystem.cartitem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemQueryService;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CartItemQueryServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MemberRepository memberRepository;

    private CartItemQueryService cartItemQueryService;

    @BeforeEach
    void setUp() {
        cartItemQueryService = new CartItemQueryService(cartItemRepository, memberRepository);
    }

    // 장바구니 조회 성공

    @Test
    @DisplayName("로그인한 회원의 장바구니 목록 조회 성공")
    void getCartItemsSuccess() {
        Long memberId = 1L;
        List<CartItem> cartItems = List.of(
                new CartItem(mock(Member.class), mock(Product.class), 2),
                new CartItem(mock(Member.class), mock(Product.class), 1)
        );

        when(cartItemRepository.findAllByMemberId(memberId)).thenReturn(cartItems);

        List<CartItem> responses = cartItemQueryService.getCartItemsByMemberId(memberId);

        assertThat(responses).containsExactlyElementsOf(cartItems);
        verify(cartItemRepository).findAllByMemberId(memberId);
    }
}
