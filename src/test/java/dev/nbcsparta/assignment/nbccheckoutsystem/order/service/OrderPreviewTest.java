package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.ItemDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderPreviewDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class OrderPreviewTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                cartItemRepository,
                orderRepository,
                paymentRepository,
                memberRepository,
                pointTransactionRepository
        );
    }

    @Test
    void getOrderPreviewReturnsAllCartItemsWhenItemsParameterIsOmitted() throws Exception {
        long memberId = 1L;
        CartItem keyboard = cartItem(11L, member(memberId), product(10L, "무선 키보드", 39_000), 2);
        CartItem mouse = cartItem(12L, member(memberId), product(20L, "무선 마우스", 25_000), 1);
        when(cartItemRepository.findAllByMembersId(memberId)).thenReturn(List.of(keyboard, mouse));

        OrderPreviewDetail response = orderService.getOrderPreview(memberId, null);

        assertAll(
                () -> assertEquals(2, response.items().size()),
                () -> assertEquals(103_000, response.totalAmount()),
                () -> assertItem(response.items().get(0), 10L, "무선 키보드", 39_000, 2),
                () -> assertItem(response.items().get(1), 20L, "무선 마우스", 25_000, 1)
        );
        verify(cartItemRepository).findAllByMembersId(memberId);
        verify(cartItemRepository, never()).findAllByIdIn(anyList());
        verifyReadOnlyDependencies();
    }

    @Test
    void getOrderPreviewReturnsAllCartItemsWhenItemsParameterIsBlank() throws Exception {
        long memberId = 1L;
        CartItem keyboard = cartItem(11L, member(memberId), product(10L, "무선 키보드", 39_000), 2);
        when(cartItemRepository.findAllByMembersId(memberId)).thenReturn(List.of(keyboard));

        OrderPreviewDetail response = orderService.getOrderPreview(memberId, "   ");

        assertAll(
                () -> assertEquals(1, response.items().size()),
                () -> assertEquals(78_000, response.totalAmount())
        );
        verify(cartItemRepository).findAllByMembersId(memberId);
    }

    @Test
    void getOrderPreviewReturnsOnlySelectedCartItemsUsingCurrentProductPrice() throws Exception {
        long memberId = 1L;
        CartItem keyboard = cartItem(11L, member(memberId), product(10L, "무선 키보드", 39_000), 2);
        CartItem mouse = cartItem(15L, member(memberId), product(20L, "무선 마우스", 25_000), 3);
        when(cartItemRepository.findAllByIdIn(List.of(11L, 15L))).thenReturn(List.of(keyboard, mouse));

        OrderPreviewDetail response = orderService.getOrderPreview(memberId, "11,15");

        assertAll(
                () -> assertEquals(2, response.items().size()),
                () -> assertEquals(153_000, response.totalAmount()),
                () -> assertItem(response.items().get(0), 10L, "무선 키보드", 39_000, 2),
                () -> assertItem(response.items().get(1), 20L, "무선 마우스", 25_000, 3)
        );
        verify(cartItemRepository).findAllByIdIn(List.of(11L, 15L));
        verify(cartItemRepository, never()).findAllByMembersId(memberId);
        verifyReadOnlyDependencies();
    }

    @Test
    void getOrderPreviewReturnsEmptyPreviewWhenCartIsEmpty() {
        long memberId = 1L;
        when(cartItemRepository.findAllByMembersId(memberId)).thenReturn(List.of());

        OrderPreviewDetail response = orderService.getOrderPreview(memberId, null);

        assertAll(
                () -> assertEquals(List.of(), response.items()),
                () -> assertEquals(0, response.totalAmount())
        );
    }

    @Test
    void getOrderPreviewRejectsCartItemOwnedByAnotherMember() throws Exception {
        long requestMemberId = 1L;
        CartItem anotherMembersItem = cartItem(
                11L,
                member(2L),
                product(10L, "무선 키보드", 39_000),
                2
        );
        when(cartItemRepository.findAllByIdIn(List.of(11L))).thenReturn(List.of(anotherMembersItem));

        UnauthorisedException exception = assertThrows(
                UnauthorisedException.class,
                () -> orderService.getOrderPreview(requestMemberId, "11")
        );

        assertEquals("Client Not Match", exception.getMessage());
        verify(cartItemRepository).findAllByIdIn(List.of(11L));
        verifyReadOnlyDependencies();
    }

    @Test
    void orderPreviewSerializesUsingDocumentedResponseFieldNames() throws Exception {
        long memberId = 1L;
        CartItem keyboard = cartItem(11L, member(memberId), product(10L, "무선 키보드", 39_000), 2);
        OrderPreviewDetail preview = OrderPreviewDetail.from(List.of(keyboard));

        JsonNode response = JsonMapper.builder().build().valueToTree(ApiResponse.success(preview));
        JsonNode item = response.path("data").path("items").get(0);

        assertAll(
                () -> assertEquals(true, response.path("success").asBoolean()),
                () -> assertEquals(10L, item.path("productId").asLong()),
                () -> assertEquals("무선 키보드", item.path("productName").asText()),
                () -> assertEquals(39_000, item.path("price").asInt()),
                () -> assertEquals(2, item.path("quantity").asInt()),
                () -> assertEquals(78_000, response.path("data").path("totalAmount").asInt())
        );
    }

    private void verifyReadOnlyDependencies() {
        verifyNoInteractions(
                orderRepository,
                paymentRepository,
                memberRepository,
                pointTransactionRepository
        );
    }

    private void assertItem(
            ItemDetail item,
            long productId,
            String productName,
            int price,
            int quantity
    ) {
        assertAll(
                () -> assertEquals(productId, item.id()),
                () -> assertEquals(productName, item.name()),
                () -> assertEquals(price, item.price()),
                () -> assertEquals(quantity, item.quantities())
        );
    }

    private Product product(Long id, String name, int price) throws Exception {
        Constructor<Product> constructor = Product.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Product product = constructor.newInstance();
        ReflectionTestUtils.setField(product, "id", id);
        ReflectionTestUtils.setField(product, "name", name);
        ReflectionTestUtils.setField(product, "price", price);
        return product;
    }

    private CartItem cartItem(Long id, Members member, Product product, int quantity) {
        CartItem cartItem = new CartItem(member, product, quantity);
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }

    private Members member(Long id) {
        Members member = new Members("test" + id + "@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
