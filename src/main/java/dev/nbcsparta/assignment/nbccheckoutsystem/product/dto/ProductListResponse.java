package dev.nbcsparta.assignment.nbccheckoutsystem.product.dto;

import java.util.List;

public record ProductListResponse(
	List<ProductResponse> products
) {

	public static ProductListResponse from(List<ProductResponse> products) {
		return new ProductListResponse(products);
	}
}
