package dev.nbcsparta.assignment.nbccheckoutsystem.cartitem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.CartItemNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ForbiddenCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemCommandService;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemValidator;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;

import java.lang.reflect.Constructor;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CartItemCommandServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CartItemValidator cartItemValidator;

    private CartItemCommandService cartItemCommandService;

    @BeforeEach
    void setUp() {
        cartItemCommandService = new CartItemCommandService(
                cartItemRepository, memberRepository, productRepository, cartItemValidator);
    }

    // 1. 장바구니 생성

    @Test
    @DisplayName("장바구니 항목 생성 성공")
    void createCartItemSuccess() throws Exception {
        Long memberId = 1L;
        Long productId = 100L;
        CartItemRequest request = new CartItemRequest(productId, 2);

        Member member = createMember(memberId);
        Product product = createProduct(productId, 10);
        CartItem savedCartItem = createCartItem(55L, 2, member, product);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(productRepository.findById(request.productId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByMembersAndProduct(member, product)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedCartItem);

        CartItemResponse response = cartItemCommandService.addCartItem(memberId, request);

        assertThat(response).isNotNull();
        assertThat(response.quantity()).isEqualTo(2);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    // 2. 장바구니 수량 변경

    @Test
    @DisplayName("장바구니 수량 변경 성공")
    void updateCartItemSuccess() throws Exception {
        Long memberId = 1L;
        Long cartItemId = 10L;
        Member member = createMember(memberId);
        Product product = createProduct(100L, 10);
        CartItem cartItem = createCartItem(cartItemId, 2, member, product);

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        UpdateCartItemResponse response = cartItemCommandService.updateCartItem(memberId, cartItemId, request);

        assertThat(response.cartItemId()).isEqualTo(cartItemId);
        assertThat(response.quantity()).isEqualTo(5);
        assertThat(cartItem.getQuantities()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 수량 변경 실패 - 존재하지 않는 장바구니 항목")
    void updateCartItemThrowsNotFound() {
        Long memberId = 1L;
        Long cartItemId = 999L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // 수정: 람다를 첫 번째 인자로, .isInstanceOf()로 예외 타입 검증
        assertThatThrownBy(() -> cartItemCommandService.updateCartItem(memberId, cartItemId, request))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    @Test
    @DisplayName("장바구니 수량 변경 실패 - 본인 장바구니 항목이 아님")
    void updateCartItemThrowsForbidden() throws Exception {
        Long loginMemberId = 1L;
        Long otherMemberId = 2L;
        Long cartItemId = 10L;

        Member otherMember = createMember(otherMemberId);
        Product product = createProduct(100L, 10);
        CartItem cartItem = createCartItem(cartItemId, 2, otherMember, product);

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // 수정: 람다를 첫 번째 인자로, .isInstanceOf()로 예외 타입 검증
        assertThatThrownBy(() -> cartItemCommandService.updateCartItem(loginMemberId, cartItemId, request))
                .isInstanceOf(ForbiddenCartItemException.class);
    }

    @Test
    @DisplayName("장바구니 수량 변경 실패 - 변경 수량이 재고 초과")
    void updateCartItemThrowsOutOfStock() throws Exception {
        Long memberId = 1L;
        Long cartItemId = 10L;
        Member member = createMember(memberId);
        Product product = createProduct(100L, 3);
        CartItem cartItem = createCartItem(cartItemId, 1, member, product);

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        // CartItemValidator가 Mock이므로 실제로 예외를 던지도록 stubbing
        doThrow(new OutOfStockException())
                .when(cartItemValidator).validateQuantity(product, request.quantity());

        assertThatThrownBy(() -> cartItemCommandService.updateCartItem(memberId, cartItemId, request))
                .isInstanceOf(OutOfStockException.class);
    }

    // 3. 장바구니 단건 삭제

    @Test
    @DisplayName("장바구니 단건 삭제 성공")
    void deleteCartItemSuccess() throws Exception {
        Long memberId = 1L;
        Long cartItemId = 10L;
        Member member = createMember(memberId);
        CartItem cartItem = createCartItem(cartItemId, 2, member, null);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        cartItemCommandService.deleteCartItem(memberId, cartItemId);

        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    @DisplayName("장바구니 단건 삭제 실패 - 본인 장바구니 항목이 아님")
    void deleteCartItemThrowsForbidden() throws Exception {
        Long loginMemberId = 1L;
        Long otherMemberId = 2L;
        Long cartItemId = 10L;

        Member otherMember = createMember(otherMemberId);
        CartItem cartItem = createCartItem(cartItemId, 2, otherMember, null);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // 수정: 람다를 첫 번째 인자로, .isInstanceOf()로 예외 타입 검증
        assertThatThrownBy(() -> cartItemCommandService.deleteCartItem(loginMemberId, cartItemId))
                .isInstanceOf(ForbiddenCartItemException.class);
    }

    // 4. 장바구니 전체 비우기

    @Test
    @DisplayName("장바구니 전체 비우기 성공")
    void deleteAllCartItemsSuccess() throws Exception {
        Long memberId = 1L;
        Member member = createMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        cartItemCommandService.deleteAllCartItems(memberId);

        verify(cartItemRepository, times(1)).deleteAllByMembers(member);
    }

    @Test
    @DisplayName("장바구니 전체 비우기 실패 - 존재하지 않는 회원")
    void deleteAllCartItemsThrowsNotFound() {
        Long memberId = 999L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // 수정: 람다를 첫 번째 인자로, .isInstanceOf()로 예외 타입 검증
        assertThatThrownBy(() -> cartItemCommandService.deleteAllCartItems(memberId))
                .isInstanceOf(MemberNotFoundException.class);
    }

    // 헬퍼 메서드

    private Member createMember(Long id) throws Exception {
        Constructor<Member> constructor = Member.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Member member = constructor.newInstance();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Product createProduct(Long id, Integer stockQuantity) throws Exception {
        Constructor<Product> constructor = Product.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Product product = constructor.newInstance();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "stockQuantity", stockQuantity);
        return product;
    }

    private CartItem createCartItem(Long id, Integer quantity, Member member, Product product) throws Exception {
        Constructor<CartItem> constructor = CartItem.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        CartItem cartItem = constructor.newInstance();
        ReflectionTestUtils.setField(cartItem, "id", id);
        ReflectionTestUtils.setField(cartItem, "quantities", quantity);
        ReflectionTestUtils.setField(cartItem, "members", member);
        ReflectionTestUtils.setField(cartItem, "product", product);
        return cartItem;
    }
}