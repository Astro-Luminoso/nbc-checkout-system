package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;


    public void isOwner(long ownerId, long memberId) {
        if (ownerId != memberId) {
            throw new UnauthorisedException();
        }
    }

    @Transactional(readOnly = true)
    public Order getOrder(long orderId) {
        return orderRepository.findOrderInFullDetailById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Page<SimpleOrderDetail> getOrderDetailInSimpleFormat(long memberId, Pageable pageable) {
        return orderRepository.findByMemberIdFormatOfSimpleOrderDetail(memberId, pageable);
    }

    public OrderCancelDetail cancelOrder(long id, long memberId) {
        Order order = orderRepository.findOrderInFullDetailById(id)
                .orElseThrow(OrderNotFoundException::new);

        this.isOwner(order.getMember().getId(), memberId);
        if (!order.isEqualStatus(OrderStatus.STANDBY)) {
            throw new CancellationNotAllowedException();
        }
        order.cancelOrder();

        return OrderCancelDetail.from(order);
    }

    public Order createNewOrder(Member member, OrderCreateRequest request, List<CartItem> cartItems) {
        Order order = new Order(member, request, cartItems);
        return orderRepository.save(order);
    }

    public void cancelOrder(Order order) {
        if (!order.isEqualStatus(OrderStatus.STANDBY)) {
            throw new CancellationNotAllowedException();
        }
        order.cancelOrder();
    }
}
