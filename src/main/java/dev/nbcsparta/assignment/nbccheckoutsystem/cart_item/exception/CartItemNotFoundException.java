package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;


public class CartItemNotFoundException extends RuntimeException{
    public CartItemNotFoundException(){
        super("존재하지 않는 장바구니 항목입니다.");
    }
}
