package dev.nbcsparta.assignment.nbccheckoutsystem.product.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductSearchCondition;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.exception.ProductNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;

	public Page<ProductResponse> getProducts(ProductSearchCondition condition) {
		return productRepository.searchProducts(
				condition.category(),
				condition.minPrice(),
				condition.maxPrice(),
				condition.saleStatus(),
				condition.toPageable()
			)
			.map(ProductResponse::from);
	}

	public ProductResponse getProduct(Long productId) {
		Product product = findProductEntity(productId);
		return ProductResponse.from(product);
	}

	// 상품 엔티티 조회 (다른 도메인에서 재사용 가능)
	public Product findProductEntity(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(ProductNotFoundException::new);
	}
}
