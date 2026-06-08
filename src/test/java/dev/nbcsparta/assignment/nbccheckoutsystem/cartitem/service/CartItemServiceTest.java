package dev.nbcsparta.assignment.nbccheckoutsystem.cartitem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemService;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CartItemServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ProductRepository productRepository;

    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        cartItemService = new CartItemService(cartItemRepository, memberRepository, productRepository);
    }
    // 1. 장바구니 생성
    @Test
    @DisplayName("장바구니 항목 생성 성공")
    void createCartItemSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long productId = 100L;

        CartItemRequest request = new CartItemRequest(productId, 2);

        Member member = createMember(memberId);
        Product product = createProduct(productId, 10); // 재고 10개짜리 상품
        CartItem savedCartItem = createCartItem(55L, 2, member, product); // 저장 후 생성될 가짜 장바구니 객체

        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedCartItem);

        // when
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(productRepository.findById(request.productId())).thenReturn(Optional.of(product));

        CartItemResponse response = cartItemService.addCartItem(memberId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.quantity()).isEqualTo(2);
        // 저장 메서드가 최소 1번 호출되었는지 검증
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }


    // 2. 장바구니 조회
    @Test
    @DisplayName("로그인한 회원의 장바구니 목록 조회 성공")
    void getCartItemsSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Member member = createMember(memberId);

        Product product1 = createProduct(101L, 5);
        Product product2 = createProduct(102L, 20);

        List<CartItem> cartItems = List.of(
                createCartItem(1L, 2, member, product1),
                createCartItem(2L, 1, member, product2)
        );

        when(cartItemRepository.findAllByMembers(any())).thenReturn(cartItems);

        // when
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        GetCartResponse responses = cartItemService.getCartItems(memberId);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.items()).hasSize(2);
    }

    // 3. 장바구니 수량 변경

    @Test
    @DisplayName("장바구니 수량 변경 성공")
    void updateCartItemSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long cartItemId = 10L;
        Member member = createMember(memberId);
        Product product = createProduct(100L, 10); // 재고 10개짜리 상품
        CartItem cartItem = createCartItem(cartItemId, 2, member, product); // 기존 수량 2개

        UpdateCartItemRequest request = new UpdateCartItemRequest(5); // 5개로 변경 요청

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // when
        UpdateCartItemResponse response = cartItemService.updateCartItem(memberId, cartItemId, request);

        // then
        assertThat(response.cartItemId()).isEqualTo(cartItemId);
        assertThat(response.quantity()).isEqualTo(5);
        assertThat(cartItem.getQuantities()).isEqualTo(5); // 엔티티 자체의 수량도 변경되었는지 검증
    }

    @Test
    @DisplayName("장바구니 수량 변경 실패 - 존재하지 않는 장바구니 항목")
    void updateCartItemThrowsNotFound() {
        // given
        Long memberId = 1L;
        Long cartItemId = 999L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartItemService.updateCartItem(memberId, cartItemId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 장바구니 항목입니다.");
    }

    @Test
    @DisplayName("장바구니 수량 변경 실패 - 본인 장바구니 항목이 아님")
    void updateCartItemThrowsForbidden() throws Exception {
        // given
        Long loginMemberId = 1L;
        Long otherMemberId = 2L; // 다른 사람 ID
        Long cartItemId = 10L;

        Member otherMember = createMember(otherMemberId);
        Product product = createProduct(/* id */ 100L, /* stockQuantity */ 10);
        CartItem cartItem = createCartItem(cartItemId, 2, otherMember, product);

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // when & then
        assertThatThrownBy(() -> cartItemService.updateCartItem(loginMemberId, cartItemId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원님의 장바구니만 변경할 수 있습니다.");
    }

    @Test
    @DisplayName("장바구니 수량 변경 실패 - 변경 수량이 재고 초과")
    void updateCartItemThrowsOutOfStock() throws Exception {
        // given
        Long memberId = 1L;
        Long cartItemId = 10L;
        Member member = createMember(memberId);
        Product product = createProduct(100L, 3); // 재고가 단 3개인 상품
        CartItem cartItem = createCartItem(cartItemId, 1, member, product);

        UpdateCartItemRequest request = new UpdateCartItemRequest(5); // 재고를 초과하는 5개 요청

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // when & then
        assertThatThrownBy(() -> cartItemService.updateCartItem(memberId, cartItemId, request))
                .isInstanceOf(OutOfStockException.class); // 아까 비워두기로 한 기본 생성자 예외 검증
    }


    // 4. 장바구니 단건 삭제

    @Test
    @DisplayName("장바구니 단건 삭제 성공")
    void deleteCartItemSuccess() throws Exception {
        // given
        Long memberId = 1L;
        Long cartItemId = 10L;
        Member member = createMember(memberId);
        CartItem cartItem = createCartItem(cartItemId, 2, member, null);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // when
        cartItemService.deleteCartItem(memberId, cartItemId);

        // then
        verify(cartItemRepository, times(1)).delete(cartItem); // 레포지토리의 delete가 정상 호출되었는지 확인
    }

    @Test
    @DisplayName("장바구니 단건 삭제 실패 - 본인 장바구니 항목이 아님")
    void deleteCartItemThrowsForbidden() throws Exception {
        // given
        Long loginMemberId = 1L;
        Long otherMemberId = 2L;
        Long cartItemId = 10L;

        Member otherMember = createMember(otherMemberId);
        CartItem cartItem = createCartItem(cartItemId, 2, otherMember, null);

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // when & then
        assertThatThrownBy(() -> cartItemService.deleteCartItem(loginMemberId, cartItemId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인 장바구니 항목이 아닌 경우 삭제할 수 없습니다.");
    }


    // 가짜 데이터 생성을 위한 리플렉션 헬퍼 메서드들

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

    private CartItem createCartItem(Long id, Integer quantity, Member members, Product product) throws Exception {
        Constructor<CartItem> constructor = CartItem.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        CartItem cartItem = constructor.newInstance();
        ReflectionTestUtils.setField(cartItem, "id", id);
        ReflectionTestUtils.setField(cartItem, "quantities", quantity);
        ReflectionTestUtils.setField(cartItem, "member", members);
        ReflectionTestUtils.setField(cartItem, "product", product);
        return cartItem;
    }

    // 4. 장바구니 전체 비우기

    @Test
    @DisplayName("장바구니 전체 비우기 성공")
    void deleteALlCartItemsSuccess() throws Exception{

        // given
        Long memberId = 1L;
        Member member = createMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when - 실제 서비스 메서드 호출
        cartItemService.deleteAllCartItems(memberId);

        // then
        verify(cartItemRepository, times(1)).deleteAllByMember(member);

    }

    @Test
    @DisplayName("장바구니 전체 비우기 실패 - 존재하지 않는 회원")
    void deleteAllCartItemsThrowsNotFound() throws Exception{
        // given
        Long memberId = 999L;

        // 존재하지 않는 회원 empty() 반환
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> cartItemService.deleteAllCartItems(memberId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }
}
