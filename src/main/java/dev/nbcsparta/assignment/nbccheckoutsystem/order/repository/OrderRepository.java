package dev.nbcsparta.assignment.nbccheckoutsystem.order.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.orderItems
            LEFT JOIN FETCH o.payment
            WHERE o.id = :id
    """)
    Optional<Order> findOrderInFullDetailById(@Param("id") long id);
}
