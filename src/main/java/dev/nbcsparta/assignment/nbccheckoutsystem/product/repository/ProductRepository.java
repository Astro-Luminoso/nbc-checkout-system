package dev.nbcsparta.assignment.nbccheckoutsystem.product.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
