package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.enums.OrderStatus;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PointTransactionRepository pointTransactionRepository;


    public void isOwner(long ownerId, long memberId) {
        if (ownerId != memberId) {
            throw new UnauthorisedException();
        }
    }


    public CreateOrderData createOrder(Long memberId, OrderCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        List<CartItem> cartItems = cartItemRepository.findAllByIdIn(memberId, request.cartItemIds());

        Order order = new Order(member, request, cartItems);
        orderRepository.save(order);
        cartItems.forEach(item -> item.getProduct().deductStockValue(item.getQuantities()));

        Payment payment = new Payment(order);
        paymentRepository.save(payment);

        return CreateOrderData.from(payment);
    }

    @Transactional(readOnly = true)
    public SpecificOrderDetail getOrderDetail(Long orderId, long memberId) {
        Order order = orderRepository.findOrderInFullDetailById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        this.isOwner(order.getMember().getId(), memberId);
        List<PointTransaction> pointTransactions = pointTransactionRepository.findByOrderId(orderId);

        return SpecificOrderDetail.from(order, pointTransactions);
    }

    @Transactional(readOnly = true)
    public MyOrderDetail getMyOrderDetail(long memberId, Pageable pageable) {
        Pageable forcedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<SimpleOrderDetail> detail = orderRepository.findByMemberIdFormatOfSimpleOrderDetail(memberId, forcedPageable);

        return MyOrderDetail.from(detail);
    }

    @Transactional(readOnly = true)
    public OrderPreviewDetail getOrderPreview(long memberId, String items) {
        List<CartItem> cartItems = (items == null || items.isBlank()) ? cartItemRepository.findAllByMembersId(memberId) :
                cartItemRepository.findAllByIdIn(Arrays.stream(items.split(",")).map(Long::parseLong).toList());

        boolean unauthorised = cartItems.stream().anyMatch(item -> item.getMembers().getId() != memberId);
        if (unauthorised) {
            throw new UnauthorisedException();
        }

        return OrderPreviewDetail.from(cartItems);
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

}
