package dev.nbcsparta.assignment.nbccheckoutsystem.product.dto;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;

public record ProductResponse(
	Long id,
	String name,
	String description,
	Integer price,
	Integer stockQuantity,
	String category,
	String salePrice
) {

	public static ProductResponse from(Product product) {
		return new ProductResponse(
			product.getId(),
			product.getName(),
			product.getDescription(),
			product.getPrice(),
			product.getStockQuantity(),
			product.getCategory(),
			product.getSalePrice()
		);
	}
}
