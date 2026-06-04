package dev.nbcsparta.assignment.nbccheckoutsystem.order.service;

import dev.nbcsparta.assignment.nbccheckoutsystem.auth.exception.UnauthorisedException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.entity.CartItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.ItemsNotMatchException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.exception.PointExceedTotalCostException;
import dev.nbcsparta.assignment.nbccheckoutsystem.cart_item.repository.CartItemRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.domain.Members;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.exception.MemberNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.member.repository.MemberRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.OrderCreateRequest;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.SpecificOrderDetail;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NotOnSaleException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OrderNotFoundException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.OutOfStockException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.dto.CreateOrderData;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.Order;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.entity.OrderItem;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.exception.NoCartItemException;
import dev.nbcsparta.assignment.nbccheckoutsystem.order.repository.OrderRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.domain.Payment;
import dev.nbcsparta.assignment.nbccheckoutsystem.payment.repository.PaymentRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.domain.PointTransaction;
import dev.nbcsparta.assignment.nbccheckoutsystem.point.repository.PointTransactionRepository;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.Product;
import dev.nbcsparta.assignment.nbccheckoutsystem.product.entity.SaleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional
    public CreateOrderData createOrder(Long memberId, OrderCreateRequest request) {
        Members member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        List<CartItem> cartItems = cartItemRepository.findAllById(memberId, request.cartItemIds());
        if (cartItems.isEmpty()) {
            throw new NoCartItemException();
        }
        if (cartItems.size() != request.cartItemIds().size()) {
            throw new ItemsNotMatchException();
        }

        int totalCost = cartItems.stream()
                .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        if (totalCost < request.usePoint() || member.getPointBalance() < request.usePoint()) {
            throw new PointExceedTotalCostException();
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem item : cartItems) {
            Product product = item.getProduct();
            if(product.getSaleStatus() != SaleStatus.ON_SALE) {
                throw new NotOnSaleException();
            }

            if (product.getStockQuantity() - item.getQuantity() < 0) {
                throw new OutOfStockException();
            }

            orderItems.add(new OrderItem(product.getName(), product.getPrice(), item.getQuantity(), product));
            product.deductStockValue(item.getQuantity());
        }

        Order order = new Order(totalCost, request.usePoint(), memberId, orderItems);
        orderRepository.save(order);

        Payment payment = new Payment(order);
        paymentRepository.save(payment);

        return CreateOrderData.from(payment);
    }

    public SpecificOrderDetail getOrderDetail(Long orderId, long memberId) {
        Order order = orderRepository.findOrderInFullDetailById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        if (order.getMemberId() != memberId) {
            throw new UnauthorisedException();
        }

        List<PointTransaction> pointTransactions = pointTransactionRepository.findByOrderId(orderId);

        return SpecificOrderDetail.from(order, pointTransactions);
    }
}
