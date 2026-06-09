package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.Item;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class) // @CreatedDate 작동을 위한 리스너 추가
@Table( //  복합 유니크 제약조건 추가
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
public class CartItem extends Item {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public CartItem(Member members, Product product, Integer quantity) {
        super(product, quantity);
        this.member = members;
    }
    // 동일 상품 재담기 시에 수량 합산
    public void addQuantity(Integer additionalQuantity){
        this.quantities += additionalQuantity;
    }

    public void updateQuantity(Integer quantity) {
        this.quantities = quantity;
    }

}