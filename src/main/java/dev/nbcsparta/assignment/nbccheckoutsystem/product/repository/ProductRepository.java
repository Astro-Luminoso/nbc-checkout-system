package dev.nbcsparta.assignment.nbccheckoutsystem.product.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.SaleStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
	@Query("""
		SELECT p FROM Product p
		WHERE (:category    IS NULL OR p.category   = :category)
		  AND (:minPrice    IS NULL OR p.price      >= :minPrice)
		  AND (:maxPrice    IS NULL OR p.price      <= :maxPrice)
		  AND (:saleStatus  IS NULL OR p.saleStatus = :saleStatus)
		""")
	Page<Product> searchProducts(
		@Param("category") String category,
		@Param("minPrice") Integer minPrice,
		@Param("maxPrice") Integer maxPrice,
		@Param("saleStatus") SaleStatus saleStatus,
		Pageable pageable
	);
}
