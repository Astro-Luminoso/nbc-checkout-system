package dev.nbcsparta.assignment.nbccheckoutsystem.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransactionType;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.dto.PointTransactionResponse;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    void getPointTransactionsReturnsAllTransactions() {
        // Given
        Long memberId = 1L;

        Member member = new Member("test@test.com", "password", "name", "010-0000-0000");
        ReflectionTestUtils.setField(member, "id", memberId);

        Order order = newInstance(Order.class);
        ReflectionTestUtils.setField(order, "id", 201L);
        ReflectionTestUtils.setField(order, "member", member);
        ReflectionTestUtils.setField(order, "totalAmount", 22000);
        ReflectionTestUtils.setField(order, "usedPoint", 7000);
        ReflectionTestUtils.setField(order, "orderItems", List.of());

        LocalDateTime earnCreatedDate = LocalDateTime.of(2026, 6, 4, 10, 0);
        PointTransaction earnPointTransaction = PointTransaction.createEarn(member, 15000, order);
        ReflectionTestUtils.setField(earnPointTransaction, "id", 101L);
        ReflectionTestUtils.setField(earnPointTransaction, "createdDate", earnCreatedDate);

        LocalDateTime useCreatedDate = LocalDateTime.of(2026, 6, 4, 9, 0);
        PointTransaction usePointTransaction = PointTransaction.createUse(member, 7000, order);
        ReflectionTestUtils.setField(usePointTransaction, "id", 102L);
        ReflectionTestUtils.setField(usePointTransaction, "createdDate", useCreatedDate);

        given(pointTransactionRepository.findByMemberId(memberId))
                .willReturn(List.of(earnPointTransaction, usePointTransaction));

        // When
        List<PointTransactionResponse> responses = pointService.getPointTransactions(memberId);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).type()).isEqualTo(PointTransactionType.EARN);
        assertThat(responses.get(0).amount()).isEqualTo(15000);
        assertThat(responses.get(0).createdDate()).isEqualTo(earnCreatedDate);
        assertThat(responses.get(1).type()).isEqualTo(PointTransactionType.USE);
        assertThat(responses.get(1).amount()).isEqualTo(7000);
        assertThat(responses.get(1).createdDate()).isEqualTo(useCreatedDate);
    }

    private <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
