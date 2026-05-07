package com.example.booking;

import com.example.booking.domain.OrderStatus;
import com.example.booking.domain.Product;
import com.example.booking.domain.payment.Payment;
import com.example.booking.domain.payment.PaymentMethod;
import com.example.booking.domain.payment.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DatabaseHelper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 테스트용 멤버 포인트 생성
     * @param memberId 회원 ID
     * @param balance 초기 잔액
     * @param reserved 초기 예약 포인트
     */
    public void createMemberPoint(Long memberId, double balance, double reserved) {
        String sql = "INSERT INTO member_point (member_id, balance, reserved) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql,
                memberId,
                BigDecimal.valueOf(balance),
                BigDecimal.valueOf(reserved)
        );
    }

    public void createProduct(String name, double price, int stock, int reservedStock) {
        String sql = """
        INSERT INTO product (id, name, price, total_stock, reserved_stock, event_product) 
        VALUES (1, ?, ?, ?, ?, TRUE)
    """;

        jdbcTemplate.update(sql,
                name,
                BigDecimal.valueOf(price),
                stock,
                reservedStock
        );
    }

    public void createPayment(String orderId, PaymentMethod method, double amount, PaymentStatus status) {
        String sql = """
        INSERT INTO payment (order_id, method, amount, status, approved_at, payment_key) 
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(sql,
                orderId,
                method.name(),
                BigDecimal.valueOf(amount),
                status.name(),
                PaymentStatus.SUCCESS.equals(status) ? LocalDateTime.now() : null, // 성공 시에만 승인시간 설정
                null
        );
    }

    public void createOrder(String orderId, Long memberId, double totalAmount, OrderStatus status) {
        String sql = """
        INSERT INTO orders (order_id, member_id, total_amount, idempotency_key, status) 
        VALUES (?, ?, ?, ?, ?)
    """;

        jdbcTemplate.update(sql,
                orderId,
                memberId,
                BigDecimal.valueOf(totalAmount), // total_amount 반영
                UUID.randomUUID().toString(),
                status.name() // Enum 상태값
        );
    }

    public void createOrderItem(String orderId, Long productId, String productName, double price, int quantity) {
        String sql = """
        INSERT INTO order_item (order_id, product_Id, product_Name, price, quantity, total_price) 
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        BigDecimal priceBD = BigDecimal.valueOf(price);
        BigDecimal totalPriceBD = priceBD.multiply(BigDecimal.valueOf(quantity));

        jdbcTemplate.update(sql,
                orderId,
                productId,
                productName,
                priceBD,
                quantity,
                totalPriceBD
        );
    }

    public List<Payment> findPaymentsByOrderId(String orderId) {
        String sql = "SELECT * FROM payment WHERE order_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            // 1. 생성자로 기본 객체 생성 (PENDING 상태로 시작됨)
            Payment payment = new Payment(
                    rs.getString("order_id"),
                    PaymentMethod.valueOf(rs.getString("method")),
                    rs.getBigDecimal("amount")
            );

            // 2. 리플렉션을 사용하여 나머지 필드 강제 주입 (상태가 SUCCESS 등으로 바뀌어야 하므로)
            // 직접 필드에 접근하기 위해 ReflectionTestUtils(Spring 제공)를 쓰면 편합니다.
            ReflectionTestUtils.setField(payment, "id", rs.getLong("id"));
            ReflectionTestUtils.setField(payment, "status", PaymentStatus.valueOf(rs.getString("status")));
            ReflectionTestUtils.setField(payment, "paymentKey", rs.getString("payment_key"));
            ReflectionTestUtils.setField(payment, "approvedAt",
                    rs.getTimestamp("approved_at") != null ? rs.getTimestamp("approved_at").toLocalDateTime() : null);

            return payment;
        }, orderId);
    }

    public Product findProductById(Long productId) {
        String sql = "SELECT * FROM product WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            // 1. 생성자로 기본 객체 생성
            Product product = new Product(
                    rs.getString("name"),
                    rs.getBigDecimal("price"),
                    rs.getTimestamp("check_in_at") != null ? rs.getTimestamp("check_in_at").toLocalDateTime() : null,
                    rs.getTimestamp("check_out_at") != null ? rs.getTimestamp("check_out_at").toLocalDateTime() : null,
                    rs.getBoolean("event_product")
            );

            // 2. 리플렉션으로 나머지 필드 주입
            ReflectionTestUtils.setField(product, "id", rs.getLong("id"));
            ReflectionTestUtils.setField(product, "totalStock", rs.getInt("total_stock"));
            ReflectionTestUtils.setField(product, "reservedStock", rs.getInt("reserved_stock"));

            return product;
        }, productId);
    }

    public void truncateAll() {
        // 1. 외래 키 제약 조건 해제 (MySQL용)
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // 2. 초기화할 테이블 목록 (엔티티 필드와 매핑되는 테이블명)
        List<String> tables = List.of(
                "member_point",
                "product",
                "orders",
                "order_item",
                "payment"
        );

        for (String table : tables) {
            // 테이블 데이터 삭제
            jdbcTemplate.execute("TRUNCATE TABLE " + table);

            // AUTO_INCREMENT 초기화 (MySQL 방식)
            // TRUNCATE만으로도 보통 초기화되지만, 명시적으로 1로 세팅
            jdbcTemplate.execute("ALTER TABLE " + table + " AUTO_INCREMENT = 1");
        }

        // 3. 외래 키 제약 조건 재설정
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
