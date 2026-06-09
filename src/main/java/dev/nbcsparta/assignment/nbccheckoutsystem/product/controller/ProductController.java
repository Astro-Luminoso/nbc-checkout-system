package dev.nbcsparta.assignment.nbccheckoutsystem.product.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.response.ApiResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductListResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductSearchCondition;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public ResponseEntity<ApiResponse<ProductListResponse>> getProducts(
		@ModelAttribute ProductSearchCondition condition // requestparm을 쓰니 너무 지저분에서 modelattribute로 작성함
	) {
		Page<ProductResponse> result = productService.getProducts(condition);
		return ResponseEntity.ok(ApiResponse.success(ProductListResponse.from(result)));
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long productId) {
		ProductResponse response = productService.getProduct(productId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
