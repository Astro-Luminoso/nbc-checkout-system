package dev.nbcsparta.assignment.nbccheckoutsystem.order.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
