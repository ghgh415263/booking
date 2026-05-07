# Booking 프로젝트 실행 가이드

## 설치 프로그램

### 1. IntelliJ IDEA 설치
 https://www.jetbrains.com/idea/

### 2. JDK 17 설치
 https://www.azul.com/downloads/?package=jdk#zulu

### 3. Docker Desktop 설치
 https://www.docker.com/products/docker-desktop/


## 셋팅 방법

### JDK 17 및 언어 수준 설정 (상세)
단순히 Project만 바꾸면 실행 시 에러가 날 수 있으므로 아래 3곳을 모두 확인해야 합니다.

Project: File → Project Structure → Project → SDK를 17로 선택.

Modules: 위와 동일 창의 Modules 탭 → Language level을 17로 설정.

Java Compiler: File → Settings (Ctrl+Alt+S) → Build, Execution, Deployment → Compiler → Java Compiler → Project bytecode version을 17로 설정.

### Docker 환경 준비 (스프링 도커 컴포즈용)
스프링 부트 3.1부터 지원되는 spring-boot-docker-compose 의존성을 사용한다면, 애플리케이션 실행 시 자동으로 컨테이너가 떠야 합니다.

Docker Desktop 실행: 프로젝트를 실행하기 전 반드시 Docker Desktop(또는 Docker Engine)이 실행 중이어야 합니다.

IntelliJ Docker 플러그인 확인: Settings → Plugins → Docker 설치 확인.

-> 이후에 인텔리제이로 실행하면 된다

---
# 시스템 아키텍처
현재 구현은 WAS만 되었습니다. WAS를 IDE로 열어서 실행하면 됩니다.
<img width="553" height="340" alt="image" src="https://github.com/user-attachments/assets/d2ea555d-9c67-4381-afeb-66315ca0f10a" />


---
# Database Schema
아래 테이블들은 제외하였습니다.
- 이력 관리용 History 테이블
- 감사(Auditing) 컬럼
    - created_by
    - created_at
    - updated_by
    - updated_at
    - update_reason
```sql
-- =========================================
-- PRODUCT
-- 단순하게 재고도 해당 엔티티에 포함
-- =========================================
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    check_in_at DATETIME NULL,
    check_out_at DATETIME NULL,
    total_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    event_product BOOLEAN NOT NULL DEFAULT FALSE
);

-- =========================================
-- MEMBER_POINT
-- =========================================
CREATE TABLE member_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    reserved DECIMAL(19,2) NOT NULL DEFAULT 0.00
);

-- =========================================
-- ORDERS
-- =========================================
CREATE TABLE orders (
    order_id VARCHAR(50) PRIMARY KEY,
    member_id BIGINT NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    idempotency_key VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    paid_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_member_id ON orders(member_id);
CREATE INDEX idx_orders_status ON orders(status);

-- =========================================
-- ORDER_ITEM
-- =========================================
CREATE TABLE order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(19,2) NOT NULL,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
            REFERENCES orders(order_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
            REFERENCES product(id)
);

CREATE INDEX idx_order_item_order_id ON order_item(order_id);

-- =========================================
-- PAYMENT
-- =========================================
CREATE TABLE payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    method VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    approved_at DATETIME NULL,
    payment_key VARCHAR(100) NULL UNIQUE,
    failed_count INT NOT NULL DEFAULT 0,
    fail_reason VARCHAR(255) NULL,

    CONSTRAINT fk_payment_order
        FOREIGN KEY (order_id)
            REFERENCES orders(order_id)
            ON DELETE CASCADE
);

CREATE INDEX idx_payment_order_id ON payment(order_id);
```


---
# 사용한 PG사
https://docs.tosspayments.com/guides/v2/get-started/payment-flow


---
# API 소개

## Checkout API (이벤트 결제 페이지 조회)

### 개요

이 API는 이벤트 상품의 결제 페이지를 구성하기 위해  
**상품 정보 + 회원 포인트 정보를 함께 조회**하는 API입니다.

### Example Request
```
GET /event/checkouts?productId=1
X-MEMBER-ID: 1
```

### 시퀀스다이어그램
<img width="1694" height="929" alt="image" src="https://github.com/user-attachments/assets/a9d13f1d-d30f-4086-821f-36338049fdf4" />
<br>
<br>

## Order And Payment Create API (이벤트 주문 및 지불 생성)

### 개요

이 API는 이벤트 상품을 기반으로 **주문 및 지불을 생성하는 API**입니다.

- 멱등성 기반 중복 주문 방지
- 상품 조회 및 검증
- 재고 예약 처리
- 주문 및 주문 아이템 생성
- 결제 정보 생성
- 결제 수단별 전략 처리
- 포인트 / PG / 혼합 결제 지원

### Example Request
```http
POST /event/orders
X-MEMBER-ID: 1
Content-Type: application/json
```

### Request Body

```json
{
  "idempotencyKey": "ORDER-20260507-0001", #멱등성을 위한 키
  "items": [   #상품정보
    {
      "productId": 1,
      "quantity": 1
    }
  ],
  "payments": [  # 지불정보
    {
      "type": "POINT",
      "amount": 5000
    },
    {
      "type": "PG",
      "amount": 25000
    }
  ]
}
```

### 시퀀스다이어그램
<img width="1561" height="1008" alt="image" src="https://github.com/user-attachments/assets/05feb739-975a-4cd6-8681-579c8a6316ff" />
<br>
<br>

## Payment Confirm API (PG 결제 승인 API)

### 개요

이 API는 PG(Payment Gateway) 결제 승인을 처리하는 API입니다.

클라이언트에서 결제창 완료 후 전달받은 결제 정보를 기반으로
실제 PG 승인(confirm)을 수행합니다.

## 처리 흐름

```text
1. 주문 상태 검증
2. 주문 상태 PROCESSING 변경
3. PG 승인 요청
4. PG 응답 수신
5. 결제 상태 반영
6. 주문 완료 처리
7. 재고 확정
```

## Example Request

```http
POST /event/orders/confirm
X-MEMBER-ID: 1
Content-Type: application/json
```

## Request Body

```json
{   #pg사에서 보내주는 정보들
  "paymentKey": "pay_202605070001",
  "orderId": "202605070001",
  "amount": 30000
}
```

## 시퀀스다이어그램
<img width="1701" height="925" alt="image" src="https://github.com/user-attachments/assets/16d92363-f998-40cf-910d-c04c39e2a5bb" />



