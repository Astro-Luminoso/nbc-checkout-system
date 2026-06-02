package dev.nbcsparta.assignment.nbccheckoutsystem.point.controller;

import dev.nbcsparta.assignment.nbccheckoutsystem.point.dto.PointTransactionResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/transactions")
    public ResponseEntity<List<PointTransactionResponse>> getPointTransactions(
            @AuthenticationPrincipal Long memberId) {
        List<PointTransactionResponse> response = pointService.getPointTransactions(memberId);
        return ResponseEntity.ok(response);
    }
}
