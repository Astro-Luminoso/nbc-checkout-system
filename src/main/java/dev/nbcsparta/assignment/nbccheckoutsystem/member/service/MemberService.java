package dev.nbcsparta.assignment.nbccheckoutsystem.member.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.dto.PointBalanceResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public PointBalanceResponse getPointBalance(Long memberId) {
        Members member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return PointBalanceResponse.from(member);
    }
}
