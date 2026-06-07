package dev.nbcsparta.assignment.nbccheckoutsystem.member.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);
}
