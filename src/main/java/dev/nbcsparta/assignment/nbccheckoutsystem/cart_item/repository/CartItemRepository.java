package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByMembersAndProduct(Members members, Product product);

    @Query("""
    select ci
    from CartItem ci
    join fetch ci.product
    where ci.members = :members
""")
    // fetch join으로 N+1예방

    List<CartItem> findAllByMembers(Members members);
}
