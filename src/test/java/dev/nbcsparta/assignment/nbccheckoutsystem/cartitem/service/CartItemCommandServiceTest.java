package dev.nbcsparta.assignment.nbccheckoutsystem.cartitem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemCommandService;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartItemCommandServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    private CartItemCommandService cartItemCommandService;

    @BeforeEach
    void setUp() {
        cartItemCommandService = new CartItemCommandService(cartItemRepository);
    }

    // 1. 장바구니 생성

    @Test
    @DisplayName("장바구니 항목 생성 성공")
    void createCartItemSuccess() {
        Member member = mock(Member.class);
        Product product = mock(Product.class);
        CartItem savedCartItem = new CartItem(member, product, 2);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedCartItem);

        CartItem response = cartItemCommandService.updateCartItem(member, product, null, 2);

        assertThat(response).isSameAs(savedCartItem);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    // 2. 장바구니 수량 변경

    @Test
    @DisplayName("장바구니 수량 변경 성공")
    void updateCartItemSuccess() {
        CartItem cartItem = new CartItem(mock(Member.class), mock(Product.class), 2);

        CartItem response = cartItemCommandService.updateCartItem(cartItem, 3);

        assertThat(response).isSameAs(cartItem);
        assertThat(cartItem.getQuantities()).isEqualTo(5);
        verifyNoInteractions(cartItemRepository);
    }

    // 3. 장바구니 단건 삭제

    @Test
    @DisplayName("장바구니 단건 삭제 성공")
    void deleteCartItemSuccess() {
        CartItem cartItem = new CartItem(mock(Member.class), mock(Product.class), 2);

        cartItemCommandService.deleteCartItem(cartItem);

        verify(cartItemRepository).delete(cartItem);
    }

    // 4. 장바구니 전체 비우기

    @Test
    @DisplayName("장바구니 전체 비우기 성공")
    void deleteAllCartItemsSuccess() {
        Long memberId = 1L;

        cartItemCommandService.deleteAllCartItems(memberId);

        verify(cartItemRepository).deleteAllByMember_Id(memberId);
    }

}
