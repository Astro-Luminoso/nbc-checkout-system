package dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByMemberAndProduct(Member members, Product product);


    @Query("SELECT c FROM CartItem c JOIN FETCH c.product WHERE c.member.id = :memberId AND c.id IN :cartItemIds")
    List<CartItem> findByMemberIdIn(@Param("memberId") Long memberId, @Param("cartItemIds") List<Long> cartItemIds);

    void deleteByMember(Member members);


    @Query("""
    select ci
    from CartItem ci
    join fetch ci.product
    where ci.member = :members
""")
    List<CartItem> findAllByMembers(Member members);

    void deleteAllByMember(Member members);

    List<CartItem> findAllByMemberId(long memberId);

    void deleteByMemberIdAndProductIn(Long memberId, List<Product> products);

    List<CartItem> findAllByIdIn(List<Long> items);

    void deleteAllByMember_Id(Long memberId);
}
