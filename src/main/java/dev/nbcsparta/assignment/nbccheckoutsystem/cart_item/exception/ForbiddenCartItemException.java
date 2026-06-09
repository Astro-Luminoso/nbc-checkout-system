package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception;

import org.springframework.http.HttpStatus;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.exception.BusinessException;

public class ForbiddenCartItemException extends BusinessException {

  public ForbiddenCartItemException() {
    super(HttpStatus.FORBIDDEN, "본인의 장바구니 항목만 접근할 수 있습니다.");
  }
}