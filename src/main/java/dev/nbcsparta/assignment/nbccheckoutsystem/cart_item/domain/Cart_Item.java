package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.domain;

import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
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
public class Cart_Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Members members;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product productId;

    @Column(nullable = false)
    private Integer quantity;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // 정적 팩토리 메서드 -> 새 장바구니 항목 생성
    public static Cart_Item create(Members members, Product product, Integer quantity){
        Cart_Item item = new Cart_Item();
        item.members = members;
        item.productId = product;
        item.quantity = quantity;
        return item;
    }
    // 동일 상품 재담기 시에 수량 합산
    public void addQuantity(Integer additionalQuantity){
        this.quantity += additionalQuantity;
    }

}