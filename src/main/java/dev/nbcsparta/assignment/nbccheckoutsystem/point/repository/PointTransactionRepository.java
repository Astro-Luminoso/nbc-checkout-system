package dev.nbcsparta.assignment.nbccheckoutsystem.point.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    @Query("SELECT pt FROM PointTransaction pt WHERE pt.members.id = :memberId ORDER BY pt.createdDate DESC")
    List<PointTransaction> findByMemberId(@Param("memberId") Long memberId);
}
