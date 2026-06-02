package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByMembersAndProductId(Members members, Product product);

    @Query("SELECT c FROM CartItem c JOIN FETCH c.productId WHERE c.id IN :cartItemIds")
    List<CartItem> findAllById(@Param("cartItemIds") List<Long> cartItemIds);



}
