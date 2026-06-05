package dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Refund;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    boolean existsByPaymentId(Long paymentId);

    Optional<Refund> findByPaymentId(Long paymentId);
}
