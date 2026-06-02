package dev.nbcsparta.assignment.nbccheckoutsystem.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Product {

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

	@CreatedDate
	@Column(nullable = false)
	private LocalDateTime createdDate;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedDate;

	public void deductStockValue(int amount) {
		this.stockQuantity -= amount;
	}


}
