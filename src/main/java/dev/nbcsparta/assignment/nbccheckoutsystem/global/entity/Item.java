package dev.nbcsparta.assignment.nbccheckoutsystem.global.entity;

import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    protected Product product;

    @Column(nullable = false)
    protected Integer quantities;

    public Item (Product product, Integer quantities) {
        this.product = product;
        this.quantities = quantities;
    }
}
