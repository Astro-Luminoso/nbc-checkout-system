package dev.nbcsparta.assignment.nbccheckoutsystem.member.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Members, Long> {

    Optional<Members> findByEmail(String email);

    boolean existsByEmail(String email);
}
