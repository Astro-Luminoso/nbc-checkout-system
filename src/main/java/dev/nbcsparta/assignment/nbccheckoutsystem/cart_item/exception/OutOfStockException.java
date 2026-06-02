package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

public class OutOfStockException extends RuntimeException{
    public OutOfStockException(){
        super("재고가 부족합니다.");
    }
}
