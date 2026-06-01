package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class) // @CreatedDate 작동을 위한 리스너 추가
@Table( // PRD 6.3 준수 / 복합 유니크 제약조건 추가
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_product",
                        columnNames = {"member_id", "product_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Members members;

    // TODO: Product 엔티티 생성 후 @ManyToOne 연관관계로 리팩토링 예정
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

}