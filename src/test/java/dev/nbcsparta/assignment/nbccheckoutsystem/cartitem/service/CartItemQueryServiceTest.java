package dev.nbcsparta.assignment.nbccheckoutsystem.cartitem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.GetCartResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemQueryService;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CartItemQueryServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MemberRepository memberRepository;

    // 수정: 서비스 인스턴스 직접 생성 (CommandServiceTest와 동일한 방식)
    private CartItemQueryService cartItemQueryService;

    @BeforeEach
    void setUp() {
        cartItemQueryService = new CartItemQueryService(cartItemRepository, memberRepository);
    }

    // 장바구니 조회 성공

    @Test
    @DisplayName("로그인한 회원의 장바구니 목록 조회 성공")
    void getCartItemsSuccess() throws Exception {
        Long memberId = 1L;
        Member member = createMember(memberId);

        Product product1 = createProduct(101L, 5);
        Product product2 = createProduct(102L, 20);

        List<CartItem> cartItems = List.of(
                createCartItem(1L, 2, member, product1),
                createCartItem(2L, 1, member, product2)
        );

        // 수정: memberRepository stubbing을 findAllByMembers보다 먼저
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(cartItemRepository.findAllByMembers(member)).thenReturn(cartItems);

        GetCartResponse responses = cartItemQueryService.getCartItems(memberId);

        assertThat(responses).isNotNull();
        assertThat(responses.items()).hasSize(2);
    }

    // 장바구니 조회 실패 - 존재하지 않는 회원

    @Test
    @DisplayName("장바구니 목록 조회 실패 - 존재하지 않는 회원")
    void getCartItemsThrowsNotFound() {
        Long memberId = 999L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemQueryService.getCartItems(memberId))
                .isInstanceOf(MemberNotFoundException.class);
    }

    // 헬퍼 메서드 (CommandServiceTest와 동일 - 공통 Fixture로 추출 가능)

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