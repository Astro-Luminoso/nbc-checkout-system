package dev.nbcsparta.assignment.nbccheckoutsystem.facade;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemCommandService;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemQueryService;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemValidator;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.service.MemberService;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartItemFacade {

    private final CartItemCommandService cartItemCommandService;
    private final CartItemQueryService cartItemQueryService;
    private final CartItemValidator cartItemValidator;
    private final MemberService memberService;
    private final ProductService productService;

    @Transactional
    public CartItemResponse addCartItem(Long memberId, CartItemRequest request) {

        Member member = memberService.getMemberById(memberId);
        Product product = productService.findProductEntity(request.productId());
        CartItem cartItem = cartItemQueryService.getByMemberAndProduct(member, product);
        cartItemValidator.validateQuantity(product, request.quantity());
        cartItem = cartItemCommandService.updateCartItem(member, product, cartItem, request.quantity());

        return CartItemResponse.from(cartItem);
    }

    @Transactional(readOnly = true)
    public GetCartResponse getCartItems(Long memberId) {

        List<CartItem> cartItems = cartItemQueryService.getCartItemsByMemberId(memberId);
        return GetCartResponse.from(cartItems);
    }

    @Transactional
    public UpdateCartItemResponse updateCartItem(Long memberId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemQueryService.findOwnedCartItem(memberId, cartItemId);
        cartItemValidator.validateQuantity(cartItem.getProduct(), request.quantity());
        cartItem = cartItemCommandService.updateCartItem(cartItem, request.quantity());

        return new UpdateCartItemResponse(cartItem.getId(), cartItem.getQuantities());
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemQueryService.findOwnedCartItem(memberId,cartItemId);
        cartItemCommandService.deleteCartItem(cartItem);
    }

    @Transactional
    public void deleteAllCartItems(Long memberId) {
        cartItemCommandService.deleteAllCartItems(memberId);
    }
}
