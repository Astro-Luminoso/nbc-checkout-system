package dev.nbcsparta.assignment.nbccheckoutsystem.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.LoginRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.LoginResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.DuplicateEmailException;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.LoginFailedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.jwt.JwtProvider;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(memberRepository, passwordEncoder, jwtProvider);
    }

    @Test
    void signupCreatesMemberWithEncryptedPassword() {
        SignupRequest request = new SignupRequest(
                "member@example.com",
                "password1234",
                "홍길동",
                "010-1234-5678"
        );
        when(memberRepository.existsByEmail("member@example.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1L);
            return member;
        });

        SignupResponse response = authService.signup(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("member@example.com");
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.phoneNumber()).isEqualTo("010-1234-5678");
        assertThat(savedMember.getPassword()).isNotEqualTo("password1234");
        assertThat(passwordEncoder.matches("password1234", savedMember.getPassword())).isTrue();
    }

    @Test
    void signupRejectsDuplicateEmail() {
        SignupRequest request = new SignupRequest(
                "member@example.com",
                "otherPassword",
                "김철수",
                "010-9999-9999"
        );
        when(memberRepository.existsByEmail("member@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 가입된 이메일입니다.");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void loginReturnsAccessToken() {
        String encodedPassword = passwordEncoder.encode("password1234");
        Member member = new Member(
                "member@example.com",
                encodedPassword,
                "홍길동",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(member, "id", 1L);
        LoginRequest request = new LoginRequest("member@example.com", "password1234");
        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));
        when(jwtProvider.createToken(1L, "member@example.com")).thenReturn("access-token");

        LoginResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void loginRejectsUnknownEmail() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password1234");
        when(memberRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    void loginRejectsInvalidPassword() {
        String encodedPassword = passwordEncoder.encode("password1234");
        Member member = new Member(
                "member@example.com",
                encodedPassword,
                "홍길동",
                "010-1234-5678"
        );
        LoginRequest request = new LoginRequest("member@example.com", "wrongPassword");
        when(memberRepository.findByEmail("member@example.com")).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}
