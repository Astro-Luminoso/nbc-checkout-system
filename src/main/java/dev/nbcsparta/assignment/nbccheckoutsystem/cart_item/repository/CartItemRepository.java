package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.domain.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByMembersAndProductId(Members members, Product product);


}
