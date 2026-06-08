package dev.nbcsparta.assignment.nbccheckoutsystem.product.entity;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.BaseEntity;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private int price;

	@Column(nullable = false)
	private int stockQuantity;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private String salePrice;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private SaleStatus saleStatus;

	public void deductStockValue(int amount) {
		if (this.stockQuantity < amount) {
			throw new OutOfStockException();
		}
		this.stockQuantity -= amount;
	}

	public void restoreStockValue(int amount) {
		this.stockQuantity += amount;
	}
}
