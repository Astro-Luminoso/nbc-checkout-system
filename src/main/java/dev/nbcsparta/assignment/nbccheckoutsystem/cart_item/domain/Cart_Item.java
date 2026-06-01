package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.domain;


import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Cart_Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Members members; // TODO: Member엔티티로 수정 후 리펙토링 예정

    // TODO: Product 엔티티 생성 후 @ManyToOne 연관관계로 리팩토링 예정
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;



}
