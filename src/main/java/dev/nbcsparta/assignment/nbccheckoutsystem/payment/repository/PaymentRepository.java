package dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.PaymentStatus;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.order.id = :orderId")
    Optional<Payment> findByOrderIdWithOrder(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p JOIN FETCH p.order WHERE p.portOnePaymentId = :portOnePaymentId")
    Optional<Payment> findByPortOnePaymentIdWithOrder(@Param("portOnePaymentId") String portOnePaymentId);

    @Query("""
            SELECT DISTINCT p FROM Payment p
            JOIN FETCH p.order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            WHERE p.id = :paymentId
            """)
    Optional<Payment> findByIdWithOrderAndItems(@Param("paymentId") Long paymentId);

    boolean existsByPortOnePaymentIdAndStatus(String portOnePaymentId, PaymentStatus status);
}
