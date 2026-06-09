package dev.nbcsparta.assignment.nbccheckoutsystem.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.dto.PointBalanceResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void getPointBalanceReturnsCorrectBalance() {
        // Given
        Long memberId = 1L;
        Member member = new Member("test@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "pointBalance", 10000);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // When
        PointBalanceResponse response = memberService.getPointBalance(memberId);

        // Then
        assertThat(response.pointBalance()).isEqualTo(10000L);
    }
}
