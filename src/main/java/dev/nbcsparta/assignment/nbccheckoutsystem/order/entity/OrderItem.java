package dev.nbcsparta.assignment.nbccheckoutsystem.order.entity;

import dev.nbcsparta.assignment.nbccheckoutsystem.global.entity.Item;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_product",
                        columnNames = {"order_id", "product_id"}
                )
        }
)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class OrderItem extends Item {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public OrderItem(String name, int price, Integer quantities, Product product) {
        super(product, quantities);
        this.name = name;
        this.price = price;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

}
