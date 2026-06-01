package dev.nbcsparta.assignment.nbccheckoutsystem.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signupCreatesMemberWithEncryptedPassword() {
        SignupRequest request = new SignupRequest(
                "member@example.com",
                "password1234",
                "홍길동",
                "010-1234-5678"
        );

        SignupResponse response = authService.signup(request);

        Members member = memberRepository.findByEmail("member@example.com").orElseThrow();
        assertThat(response.memberId()).isEqualTo(member.getId());
        assertThat(response.email()).isEqualTo("member@example.com");
        assertThat(member.getPassword()).isNotEqualTo("password1234");
        assertThat(passwordEncoder.matches("password1234", member.getPassword())).isTrue();
    }

    @Test
    void signupRejectsDuplicateEmail() {
        memberRepository.save(new Members(
                "member@example.com",
                passwordEncoder.encode("password1234"),
                "홍길동",
                "010-1234-5678"
        ));

        SignupRequest request = new SignupRequest(
                "member@example.com",
                "otherPassword",
                "김철수",
                "010-9999-9999"
        );

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 가입된 이메일입니다.");
    }
}
