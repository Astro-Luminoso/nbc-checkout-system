package dev.nbcsparta.assignment.nbccheckoutsystem.product.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductListResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public ResponseEntity<ProductListResponse> getProducts() {
		return ResponseEntity.ok(ProductListResponse.from(productService.getProducts()));
	}
}
