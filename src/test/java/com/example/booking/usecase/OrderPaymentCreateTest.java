package com.example.booking.usecase;

import com.example.booking.DatabaseHelper;
import com.example.booking.application.*;
import com.example.booking.domain.*;
import com.example.booking.domain.payment.Payment;
import com.example.booking.domain.payment.PaymentMethod;
import com.example.booking.domain.payment.PaymentRepository;
import com.example.booking.domain.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Transactional
@Import(DatabaseHelper.class)
@SpringBootTest
public class OrderPaymentCreateTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private DatabaseHelper databaseHelper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MemberPointRepository  memberPointRepository;

    @Test
    void 멱등성요청_검사(){
        databaseHelper.createMemberPoint(1l,5000,0);
        databaseHelper.createProduct("이벤트 상품",5000,10,0);
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 1)
        );
        List<PaymentMethodRequest> payments = List.of(
                new PaymentMethodRequest(PaymentMethod.POINT, new BigDecimal("5000.00"))
        );

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID().toString(), // 멱등성 키 (랜덤 생성)
                items,
                payments
        );

        orderService.create(request, 1L);
        OrderCreateResponse response = orderService.create(request, 1L);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(productRepository.findById(1L))
                .isPresent() // 값이 있는지 먼저 확인
                .get()       // Optional 내부 객체 추출
                .extracting(Product::getTotalStock) // 재고 필드 추출
                .isEqualTo(9); // 값 비교
        assertThat(paymentRepository.findByOrderId(response.orderId()))
                .isNotEmpty() // 리스트가 비어있지 않은지 먼저 확인
                .allSatisfy(payment -> {
                    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
                });
    }

    @Test
    void 포인트만_사용하여_주문결제_성공(){

        databaseHelper.createMemberPoint(1l,5000,0);
        databaseHelper.createProduct("이벤트 상품",5000,10,0);

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 1)
        );
        List<PaymentMethodRequest> payments = List.of(
                new PaymentMethodRequest(PaymentMethod.POINT, new BigDecimal("5000.00"))
        );

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID().toString(), // 멱등성 키 (랜덤 생성)
                items,
                payments
        );

        // 자체 포인트 사용 결제이므로 결제가 완료되야함
        OrderCreateResponse response = orderService.create(request, 1L);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(productRepository.findById(1L))
                .isPresent() // 값이 있는지 먼저 확인
                .get()       // Optional 내부 객체 추출
                .extracting(Product::getTotalStock) // 재고 필드 추출
                .isEqualTo(9); // 값 비교
        assertThat(paymentRepository.findByOrderId(response.orderId()))
                .isNotEmpty()
                .satisfies(list -> {
                    // 모든 상태가 SUCCESS인지 검증
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCESS));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });
        assertThat(memberPointRepository.findByMemberId(1L))
                .isPresent()
                .get()
                .satisfies(point -> {
                    assertThat(point.getBalance()).isEqualByComparingTo("0.00");
                    assertThat(point.getReserved()).isEqualByComparingTo("0.00");
                });
    }


    @Test
    void pg결제만_사용하여_주문결제_준비상태됨(){
        databaseHelper.createMemberPoint(1l,5000,0);
        databaseHelper.createProduct("이벤트 상품",5000,10,0);

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 1)
        );
        List<PaymentMethodRequest> payments = List.of(
                new PaymentMethodRequest(PaymentMethod.CARD, new BigDecimal("5000.00"))
        );

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID().toString(), // 멱등성 키 (랜덤 생성)
                items,
                payments
        );

        // pg사 결제이므로 추후 pg사 confirm을 위해서 ready상태가 되야함
        OrderCreateResponse response = orderService.create(request, 1L);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.READY);
        assertThat(productRepository.findById(1L))
                .isPresent()
                .get()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(10, 1); // 순서대로 (재고 10, 예약 1) 확인
        assertThat(paymentRepository.findByOrderId(response.orderId()))
                .isNotEmpty()
                .satisfies(list -> {
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });
    }

    @Test
    void pg결제_포인트_복합결제_사용하여_주문결제_준비상태됨() {
        databaseHelper.createMemberPoint(1l,5000,0);
        databaseHelper.createProduct("이벤트 상품",5000,10,0);

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 1)
        );
        List<PaymentMethodRequest> payments = List.of(
                new PaymentMethodRequest(PaymentMethod.CARD, new BigDecimal("3000.00")),
                new PaymentMethodRequest(PaymentMethod.POINT, new BigDecimal("2000.00"))
        );

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID().toString(), // 멱등성 키 (랜덤 생성)
                items,
                payments
        );

        // pg사 결제이므로 추후 pg사 confirm을 위해서 ready상태가 되야함
        OrderCreateResponse response = orderService.create(request, 1L);

        assertThat(response.orderStatus()).isEqualTo(OrderStatus.READY);
        assertThat(productRepository.findById(1L))
                .isPresent()
                .get()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(10, 1); // 순서대로 (재고 10, 예약 1) 확인
        assertThat(paymentRepository.findByOrderId(response.orderId()))
                .isNotEmpty()
                .satisfies(list -> {
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.PENDING));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });

        assertThat(memberPointRepository.findByMemberId(1L))
                .isPresent()
                .get()
                .satisfies(point -> {
                    assertThat(point.getBalance()).isEqualByComparingTo("5000.00");
                    assertThat(point.getReserved()).isEqualByComparingTo("2000.00");
                });
    }

    @Test
    void 결제금액과_상품총가격이_달라서_예외() {
        databaseHelper.createMemberPoint(1l,5000,0);
        databaseHelper.createProduct("이벤트 상품",5000,10,0);

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 1)
        );

        // 상품가격은 5000인데 결제금액은 4000원으로 입력받음
        List<PaymentMethodRequest> payments = List.of(
                new PaymentMethodRequest(PaymentMethod.CARD, new BigDecimal("2000.00")),
                new PaymentMethodRequest(PaymentMethod.POINT, new BigDecimal("2000.00"))
        );

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID().toString(), // 멱등성 키 (랜덤 생성)
                items,
                payments
        );

        assertThatThrownBy(() -> orderService.create(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액 불일치");
    }

    @Test
    void 포인트결제시_포인트가_부족하여_예외() {
        databaseHelper.createMemberPoint(1l,0,0); // 포인트 없음
        databaseHelper.createProduct("이벤트 상품",5000,10,0);

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1L, 1)
        );

        List<PaymentMethodRequest> payments = List.of(
                new PaymentMethodRequest(PaymentMethod.CARD, new BigDecimal("2000.00")),
                new PaymentMethodRequest(PaymentMethod.POINT, new BigDecimal("3000.00"))
        );

        OrderCreateRequest request = new OrderCreateRequest(
                UUID.randomUUID().toString(),
                items,
                payments
        );

        assertThatThrownBy(() -> orderService.create(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트가 부족");
    }

}
