package dev.nbcsparta.assignment.nbccheckoutsystem.product.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.ProductSortType;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.SaleStatus;

public record ProductSearchCondition(
	String category,
	Integer minPrice,
	Integer maxPrice,
	SaleStatus saleStatus,
	ProductSortType sort,
	Integer page,
	Integer size
) {
	public ProductSearchCondition {
		if (sort == null) sort = ProductSortType.LATEST;
		if (page == null || page < 1) page = 1;
		if (size == null || size < 1) size = 10;
		if (size > 100) size = 100;
	}

	public Pageable toPageable() {

		return PageRequest.of(page - 1, size, sort.getSort());
	}
}
