package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService {


    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartItemResponse addCartItem(Long memberId, CartItemRequest request) {

        Member members = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        if (request.quantity() > product.getStockQuantity()) {
            throw new OutOfStockException();
        }

        CartItem cartItem = cartItemRepository
                .findByMembersAndProduct(members, product)
                .map(existing -> {
                    int newQuantity = existing.getQuantities() + request.quantity();

                    if (newQuantity > product.getStockQuantity()) {
                        throw new OutOfStockException();
                    }

                    existing.addQuantity(request.quantity());
                    return existing;
                })
                .orElseGet(() -> cartItemRepository.save(
                        new CartItem(members, product, request.quantity())
                ));

        return CartItemResponse.from(cartItem);
    }

    @Transactional(readOnly = true)
    public GetCartResponse getCartItems(Long memberId) {
        Member members = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<CartItem> cartItems = cartItemRepository.findAllByMembers(members);

        List<GetCartItemResponse> items = cartItems.stream()
                .map(GetCartItemResponse::from)
                .toList();

        int totalAmount = items.stream()
                .mapToInt(GetCartItemResponse::lineAmount)
                .sum();

        return new GetCartResponse(items, totalAmount);
    }

    @Transactional
    public UpdateCartItemResponse updateCartItem(Long memberId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 항목입니다."));

        if (!cartItem.getMembers().getId().equals(memberId)) {
            throw new IllegalArgumentException("회원님의 장바구니만 변경할 수 있습니다.");
        }

        Product product = cartItem.getProduct();
        if (request.quantity() > product.getStockQuantity()) {
            throw new OutOfStockException();
        }

        cartItem.updateQuantity(request.quantity());
        return new UpdateCartItemResponse(cartItem.getId(), cartItem.getQuantities());
    }

    @Transactional
    public void deleteCartItem(Long memberId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니 항목입니다."));

        if (!cartItem.getMembers().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인 장바구니 항목이 아닌 경우 삭제할 수 없습니다.");
        }
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void deleteAllCartItems(Long memberId) {
        Member members = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        cartItemRepository.deleteAllByMembers(members);
    }
}
