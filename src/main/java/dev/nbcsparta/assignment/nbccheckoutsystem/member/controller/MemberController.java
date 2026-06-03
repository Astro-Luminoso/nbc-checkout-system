package dev.nbcsparta.assignment.nbccheckoutsystem.member.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.dto.PointBalanceResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/points")
    public ResponseEntity<?> getPointBalance(
            @AuthenticationPrincipal Long memberId) {
        try {
            PointBalanceResponse response = memberService.getPointBalance(memberId);
            return ResponseEntity.ok(response);
        } catch (MemberNotFoundException exception) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", exception.getMessage()));
        }
    }
}
