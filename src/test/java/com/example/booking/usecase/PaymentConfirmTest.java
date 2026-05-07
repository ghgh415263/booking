package com.example.booking.usecase;

import com.example.booking.DatabaseHelper;
import com.example.booking.application.PaymentConfirmRequest;
import com.example.booking.application.PaymentConfirmResult;
import com.example.booking.application.PaymentService;
import com.example.booking.domain.OrderStatus;
import com.example.booking.domain.Product;
import com.example.booking.domain.payment.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Import(DatabaseHelper.class)
@SpringBootTest
public class PaymentConfirmTest {

    @Autowired
    private DatabaseHelper databaseHelper;

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private PgClient pgClient;

    @AfterEach
    void cleanUp() {
        databaseHelper.truncateAll();
    }

    @Test
    void PG사결제_확인성공(){
        databaseHelper.createProduct("이벤트 상품",5000,10,1);
        databaseHelper.createOrder("testOrderId", 1l, 5000, OrderStatus.READY);
        databaseHelper.createOrderItem("testOrderId", 1L, "이벤트 상품", 5000, 1);
        databaseHelper.createPayment("testOrderId", PaymentMethod.CARD, 5000, PaymentStatus.PENDING);

        PgClientConfirmResponse successResponse = new PgClientConfirmResponse(true, false, false,
                "결제 승인이 완료되었습니다."
        ); // PG 호출 성공했다고 가정
        given(pgClient.confirmPayment(any(PaymentConfirmRequest.class)))
                .willReturn(successResponse);

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(
                "pg_mock_key_12345",
                "testOrderId",
                new BigDecimal("5000.00")
        );
        PaymentConfirmResult result = paymentService.confirmPayment(confirmRequest, 1l);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(databaseHelper.findPaymentsByOrderId("testOrderId"))
                .isNotEmpty()
                .satisfies(list -> {
                    // 모든 상태가 SUCCESS인지 검증
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCESS));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });

        assertThat(databaseHelper.findProductById(1l))
                .isNotNull()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(9, 0);
    }

    @Test
    void PG사결제_포인트결제_복합결제_성공(){
        databaseHelper.createMemberPoint(1l,2000,3000);
        databaseHelper.createProduct("이벤트 상품",5000,10,1);
        databaseHelper.createOrder("testOrderId", 1l, 5000, OrderStatus.READY);
        databaseHelper.createOrderItem("testOrderId", 1L, "이벤트 상품", 5000, 1);
        databaseHelper.createPayment("testOrderId", PaymentMethod.CARD, 2000, PaymentStatus.PENDING);
        databaseHelper.createPayment("testOrderId", PaymentMethod.POINT, 3000, PaymentStatus.PENDING);

        PgClientConfirmResponse successResponse = new PgClientConfirmResponse(true, false, false,
                "결제 승인이 완료되었습니다."
        ); // PG 호출 성공했다고 가정
        given(pgClient.confirmPayment(any(PaymentConfirmRequest.class)))
                .willReturn(successResponse);

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest( // 2000원만 PG결제
                "pg_mock_key_12345",
                "testOrderId",
                new BigDecimal("2000.00")
        );
        PaymentConfirmResult result = paymentService.confirmPayment(confirmRequest, 1l);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(databaseHelper.findPaymentsByOrderId("testOrderId"))
                .isNotEmpty()
                .satisfies(list -> {
                    // 모든 상태가 SUCCESS인지 검증
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.SUCCESS));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });

        assertThat(databaseHelper.findProductById(1l))
                .isNotNull()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(9, 0);
    }

    @Test
    void PG사결제_실패_재시도필요없음(){
        databaseHelper.createProduct("이벤트 상품",5000,10,1);
        databaseHelper.createOrder("testOrderId", 1l, 5000, OrderStatus.READY);
        databaseHelper.createOrderItem("testOrderId", 1L, "이벤트 상품", 5000, 1);
        databaseHelper.createPayment("testOrderId", PaymentMethod.CARD, 5000, PaymentStatus.PENDING);

        PgClientConfirmResponse response = new PgClientConfirmResponse(false, true, false,
                "결제 실패"
        ); // PG 호출 실패했다고 가정
        given(pgClient.confirmPayment(any(PaymentConfirmRequest.class)))
                .willReturn(response);

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(
                "pg_mock_key_12345",
                "testOrderId",
                new BigDecimal("5000.00")
        );
        PaymentConfirmResult result = paymentService.confirmPayment(confirmRequest, 1l);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(databaseHelper.findPaymentsByOrderId("testOrderId"))
                .isNotEmpty()
                .satisfies(list -> {
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });

        assertThat(databaseHelper.findProductById(1l))
                .isNotNull()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(10, 0);
    }

    @Test
    void PG사결제_포인트결제_복합결제_실패_재시도필요없음(){
        databaseHelper.createMemberPoint(1l,2000,3000);
        databaseHelper.createProduct("이벤트 상품",5000,10,1);
        databaseHelper.createOrder("testOrderId", 1l, 5000, OrderStatus.READY);
        databaseHelper.createOrderItem("testOrderId", 1L, "이벤트 상품", 5000, 1);
        databaseHelper.createPayment("testOrderId", PaymentMethod.CARD, 2000, PaymentStatus.PENDING);
        databaseHelper.createPayment("testOrderId", PaymentMethod.POINT, 3000, PaymentStatus.PENDING);

        PgClientConfirmResponse response = new PgClientConfirmResponse(false, true, false,
                "결제 실패"
        ); // PG 호출 실패했다고 가정
        given(pgClient.confirmPayment(any(PaymentConfirmRequest.class)))
                .willReturn(response);

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest( // 2000원만 PG결제
                "pg_mock_key_12345",
                "testOrderId",
                new BigDecimal("2000.00")
        );
        PaymentConfirmResult result = paymentService.confirmPayment(confirmRequest, 1l);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(databaseHelper.findPaymentsByOrderId("testOrderId"))
                .isNotEmpty()
                .satisfies(list -> {
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.FAILED));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });

        assertThat(databaseHelper.findProductById(1l))
                .isNotNull()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(10, 0);
    }

    @Test
    void PG사결제_UNKNOWN_추후_스케줄러로_재시도(){
        databaseHelper.createProduct("이벤트 상품",5000,10,1);
        databaseHelper.createOrder("testOrderId", 1l, 5000, OrderStatus.READY);
        databaseHelper.createOrderItem("testOrderId", 1L, "이벤트 상품", 5000, 1);
        databaseHelper.createPayment("testOrderId", PaymentMethod.CARD, 5000, PaymentStatus.PENDING);

        PgClientConfirmResponse response = new PgClientConfirmResponse(false, false, true,
                "알수없는 상태"
        );
        given(pgClient.confirmPayment(any(PaymentConfirmRequest.class)))
                .willReturn(response);

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(
                "pg_mock_key_12345",
                "testOrderId",
                new BigDecimal("5000.00")
        );
        PaymentConfirmResult result = paymentService.confirmPayment(confirmRequest, 1l);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(databaseHelper.findPaymentsByOrderId("testOrderId"))
                .isNotEmpty()
                .satisfies(list -> {
                    assertThat(list).allSatisfy(p -> assertThat(p.getStatus()).isEqualTo(PaymentStatus.UNKNOWN));

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });
        assertThat(databaseHelper.findProductById(1l))
                .isNotNull()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(10, 1);
    }

    @Test
    void PG사결제_포인트결제_복합결제_UNKNOWN_추후_스케줄러로_재시도(){
        databaseHelper.createMemberPoint(1l,2000,3000);
        databaseHelper.createProduct("이벤트 상품",5000,10,1);
        databaseHelper.createOrder("testOrderId", 1l, 5000, OrderStatus.READY);
        databaseHelper.createOrderItem("testOrderId", 1L, "이벤트 상품", 5000, 1);
        databaseHelper.createPayment("testOrderId", PaymentMethod.CARD, 2000, PaymentStatus.PENDING);
        databaseHelper.createPayment("testOrderId", PaymentMethod.POINT, 3000, PaymentStatus.PENDING);

        PgClientConfirmResponse response = new PgClientConfirmResponse(false, false, true,
                "알수없는 상태"
        );
        given(pgClient.confirmPayment(any(PaymentConfirmRequest.class)))
                .willReturn(response);

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest( // 2000원만 PG결제
                "pg_mock_key_12345",
                "testOrderId",
                new BigDecimal("2000.00")
        );
        PaymentConfirmResult result = paymentService.confirmPayment(confirmRequest, 1l);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(databaseHelper.findPaymentsByOrderId("testOrderId"))
                .isNotEmpty()
                .satisfies(list -> {
                    assertThat(list).allSatisfy(p -> {
                        if (p.getMethod() == PaymentMethod.CARD) {
                            assertThat(p.getStatus()).isEqualTo(PaymentStatus.UNKNOWN);
                        } else {
                            assertThat(p.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
                        }
                    });

                    // 합계 검증
                    BigDecimal sum = list.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    assertThat(sum).isEqualByComparingTo("5000.00");
                });
        assertThat(databaseHelper.findProductById(1l))
                .isNotNull()
                .extracting(Product::getTotalStock, Product::getReservedStock) // 두 필드 추출
                .containsExactly(10, 1);
    }

    @Test
    void 주문_하나에_대해_100개_동시_승인_요청시_딱_한번만_성공해야함_멱등성() throws InterruptedException {
        // 1. Given: 재고 10개, 결제 대기 데이터 1개 준비
        int initialStock = 10;
        int threadCount = 100;

        databaseHelper.createProduct("한정판 상품", 5000, initialStock, 1); // 재고는 넉넉함
        databaseHelper.createOrder("testOrderId", 1L, 5000, OrderStatus.READY);
        databaseHelper.createOrderItem("testOrderId", 1L, "이벤트 상품", 5000, 1);
        databaseHelper.createPayment("testOrderId", PaymentMethod.CARD, 5000, PaymentStatus.PENDING);

        // PG사 응답 목킹 (무조건 성공 리턴)
        given(pgClient.confirmPayment(any())).willReturn(
                new PgClientConfirmResponse(true, false, false, "SUCCESS")
        );

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        PaymentConfirmRequest confirmRequest = new PaymentConfirmRequest(
                "pg_mock_key", "testOrderId", new BigDecimal("5000.00")
        );

        // 2. When: 동일한 주문 ID로 100명이 동시 레이스
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    readyLatch.await(); // 동시 출발
                    paymentService.confirmPayment(confirmRequest, 1L);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 이미 PROCESSING이거나 SUCCESS인 경우 markProcessing()에서 던지는 예외 발생
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        readyLatch.countDown();
        latch.await();
        executorService.shutdown();

        // 3. Then: 검증
        // 단 하나만 성공해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);

        // DB 상태 확인: 재고는 딱 1개만 깎여서 9여야 함
        Product product = databaseHelper.findProductById(1L);
        assertThat(product.getTotalStock()).isEqualTo(initialStock - 1);

        // 결제 상태 확인: SUCCESS 상태의 결제 건이 딱 1개여야 함
        List<Payment> payments = databaseHelper.findPaymentsByOrderId("testOrderId");
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
