package dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
