package dev.nbcsparta.assignment.nbccheckoutsystem.point.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.point.dto.PointTransactionResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;

    public List<PointTransactionResponse> getPointTransactions(Long memberId) {
        return pointTransactionRepository.findByMemberId(memberId).stream()
                .map(PointTransactionResponse::from)
                .toList();
    }
}
