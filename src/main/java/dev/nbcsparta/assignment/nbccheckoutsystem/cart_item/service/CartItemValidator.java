package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.InvalidQuantityException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartItemValidator {

    public void validateQuantity(Product product, int quantity){
        if (quantity <= 0){
            throw new InvalidQuantityException();
        }
        if (product.getStockQuantity() < quantity){
            throw new OutOfStockException();
        }
    }

}
