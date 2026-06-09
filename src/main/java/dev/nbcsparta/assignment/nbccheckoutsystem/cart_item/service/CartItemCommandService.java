package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.CartItemNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.exception.ProductNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ForbiddenCartItemException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemCommandService {


    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartItem updateCartItem(Member member, Product product, CartItem item, int quantities) {

        if (item == null) {
            item = cartItemRepository.save(new CartItem(member, product, quantities));
        } else {
            item = this.updateCartItem(item, quantities);
        }

        return item;
    }

    public CartItem updateCartItem(CartItem item, int requestQuantity) {
        item.addQuantity(requestQuantity);
        return item;
    }

    @Transactional
    public void deleteCartItem(CartItem cartItem) {
        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void deleteAllCartItems(Long memberId) {
        cartItemRepository.deleteAllByMember_Id(memberId);
    }

    @Transactional
    public void deductProductStock(List<CartItem> cartItems) {
        cartItems.forEach(item -> item.getProduct().deductStockValue(item.getQuantities()));
    }
}
