package dev.nbcsparta.assignment.nbccheckoutsystem.order.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.SimpleOrderDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            LEFT JOIN FETCH o.payment
            WHERE o.id = :id
    """)
    Optional<Order> findOrderInFullDetailById(@Param("id") long id);

    @Query(
            """
            SELECT new dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.SimpleOrderDetail
            (o.id, o.orderStatus, o.totalAmount, o.createdDate)
            FROM Order o
            WHERE o.memberId = :memberId
            """)
    Page<SimpleOrderDetail> findByMemberIdFormatOfSimpleOrderDetail(@Param("memberId") long memberId, Pageable pageable);
}
