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

    Optional<CartItem> findByMembersAndProduct(Members members, Product product);
    @Query("SELECT c FROM CartItem c JOIN FETCH c.product WHERE c.members.id = :memberId AND c.id IN :cartItemIds")
    List<CartItem> findAllByIdIn(@Param("memberId") Long memberId, @Param("cartItemIds") List<Long> cartItemIds);

    void deleteByMembers(Members members);


    @Query("""
    select ci
    from CartItem ci
    join fetch ci.product
    where ci.members = :members
""")
    List<CartItem> findAllByMembers(Members members);

    void deleteAllByMembers(Members members);

    List<CartItem> findAllByMembersId(long memberId);

    List<CartItem> findAllByIdIn(List<Long> items);
}
