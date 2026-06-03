package dev.nbcsparta.assignment.nbccheckoutsystem.product.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record ProductListResponse(
	List<ProductResponse> products, // 페이지의 상품 목록
	int page,                       // 페이지 번호 (사용자 기준 1부터)
	int size,                       // 페이지 크기
	long totalElements,             // 전체 상품 개수
	int totalPages,                 // 전체 페이지 수
	boolean last                    // 마지막 페이지인지 여부
) {

	public static ProductListResponse from(Page<ProductResponse> page) {
		return new ProductListResponse(
			page.getContent(),
			page.getNumber() + 1,
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.isLast()
		);
	}
}