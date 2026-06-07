package dev.nbcsparta.assignment.nbccheckoutsystem.auth.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.LoginRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.LoginResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.dto.SignupResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.DuplicateEmailException;
import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.LoginFailedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.global.jwt.JwtProvider;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        Member member = new Member(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phoneNumber()
        );
        Member savedMember = memberRepository.save(member);

        return SignupResponse.from(savedMember);
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(LoginFailedException::new);

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new LoginFailedException();
        }

        String accessToken = jwtProvider.createToken(member.getId(), member.getEmail());
        return LoginResponse.from(accessToken);
    }
}
