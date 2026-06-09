package dev.nbcsparta.assignment.nbccheckoutsystem.member.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return PointBalanceResponse.from(member);
    }

    public Member getMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    @Transactional
    public void deductPoint(Long memberId, int amount) {
        if (amount <= 0) return;
        Member member = getMemberById(memberId);
        member.deductPointBalance(amount);
    }

    @Transactional
    public void addPoint(Long memberId, int amount) {
        if (amount <= 0) return;
        Member member = getMemberById(memberId);
        member.addPointBalance(amount);
    }
}
