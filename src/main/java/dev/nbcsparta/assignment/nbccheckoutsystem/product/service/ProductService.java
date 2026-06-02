package dev.nbcsparta.assignment.nbccheckoutsystem.product.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.dto.ProductResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.repository.ProductRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;

	public List<ProductResponse> getProducts() {
		return productRepository.findAll().stream()
			.map(ProductResponse::from)
			.toList();
	}
}
