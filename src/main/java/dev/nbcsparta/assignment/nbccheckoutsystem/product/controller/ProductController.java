package dev.nbcsparta.assignment.nbccheckoutsystem.product.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductListResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.exception.ProductNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.service.ProductService;

import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	@GetMapping("/{productId}")
	public ResponseEntity<?> getProduct(@PathVariable Long productId) {
		try {
			ProductResponse response = productService.getProduct(productId);
			return ResponseEntity.ok(response);
		} catch (ProductNotFoundException exception) {
			return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(Map.of("message", exception.getMessage()));
		}
	}
}
