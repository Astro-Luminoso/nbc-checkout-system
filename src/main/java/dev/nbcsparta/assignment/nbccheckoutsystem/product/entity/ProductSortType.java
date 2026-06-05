package dev.nbcsparta.assignment.nbccheckoutsystem.product.entity;

import org.springframework.data.domain.Sort;

import lombok.Getter;

@Getter
public enum ProductSortType {
	// 각 정렬 옵션이 자기만의 정렬 기준를 직접 들고 있습니다!
	// 이렇게 하면 서비스나 컨트롤러에서 if 분기할 필요가 없어서 enum으로 추가했습니다!

	// 최신순
	LATEST(Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("id"))),
	// 가격 오름차순
	PRICE_ASC(Sort.by(Sort.Order.asc("price"), Sort.Order.desc("id"))),
	// 가격 내림차순
	PRICE_DESC(Sort.by(Sort.Order.desc("price"), Sort.Order.desc("id")));

	private final Sort sort;

	ProductSortType(Sort sort) {
		this.sort = sort;
	}

}
