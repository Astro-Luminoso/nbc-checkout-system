package dev.nbcsparta.assignment.nbccheckoutsystem.facade;

import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemCommandService;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemQueryService;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.service.CartItemValidator;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Member;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.service.MemberService;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.*;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.service.OrderService;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.service.PaymentService;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CartItemCommandService cartItemCommandServiceService;
    private final CartItemQueryService cartItemQueryService;
    private final MemberService memberService;
    private final PointService pointService;
    private final CartItemValidator cartItemValidator;


    @Transactional
    public CreateOrderData createOrder(Long memberId, OrderCreateRequest request) {
        Member member = memberService.getMemberById(memberId);
        List<CartItem> cartItems = cartItemQueryService.getCartItemsByMemberIdIn(memberId, request.cartItemIds());
        Order order = orderService.createNewOrder(member, request, cartItems);
        cartItemCommandServiceService.deductProductStock(cartItems);
        Payment payment = paymentService.savePayment(order);
        return CreateOrderData.from(payment);
    }

    @Transactional(readOnly = true)
    public SpecificOrderDetail getOrderDetail(Long orderId, long memberId) {
        Order order = orderService.getOrder(orderId);
        orderService.isOwner(order.getMember().getId(), memberId);
        List<PointTransaction> pointTransactions = pointService.getPointTransactionsByOrderId(orderId);

        return SpecificOrderDetail.from(order, pointTransactions);
    }

    @Transactional(readOnly = true)
    public MyOrderDetail getMyOrderDetail(long memberId, Pageable pageable) {
        Pageable forcedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<SimpleOrderDetail> detail = orderService.getOrderDetailInSimpleFormat(memberId, forcedPageable);

        return MyOrderDetail.from(detail);
    }

    @Transactional
    public OrderPreviewDetail getOrderPreview(long memberId, String items) {
        List<CartItem> cartItems = (items == null || items.isBlank()) ?
                cartItemQueryService.getCartItemsByMemberId(memberId) :
                cartItemQueryService.getCartItemsByMemberIdIn(Arrays.stream(items.split(","))
                                                         .map(Long::parseLong).toList());
        cartItemValidator.inspectCartItemOwner(cartItems, memberId);

        return OrderPreviewDetail.from(cartItems);
    }

    @Transactional
    public OrderCancelDetail cancelOrder(long id, long memberId) {
        Order order = orderService.getOrder(id);
        orderService.isOwner(order.getMember().getId(), memberId);
        orderService.cancelOrder(order);

        return OrderCancelDetail.from(order);
    }
}
