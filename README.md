# Booking 프로젝트 실행 가이드

## 설치 프로그램

### 1. IntelliJ IDEA 설치
 https://www.jetbrains.com/idea/

### 2. JDK 17 설치
 https://www.azul.com/downloads/?package=jdk#zulu

### 3. Docker Desktop 설치
 https://www.docker.com/products/docker-desktop/


## 셋팅 방법

### 1. 프로젝트 열기
IntelliJ 실행 → File → Open → booking 폴더 선택


### 2. Maven 프로젝트 확인
오른쪽 Maven 탭 확인 → Maven Projects import 완료 여부 확인


### 3. JDK 17 설정 (중요)
File → Project Structure → Project

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
인증 로그인 대신 X-MEMBER-ID를 헤더에 넣는걸로 단순 대체할 것입니다.
또한, 상품과 회원 모두 pk는 1만 존재한다고 가정하였습니다.

## Order And Payment Create API (이벤트 주문 및 지불 생성)

### 개요

이 API는 이벤트 상품을 기반으로 **이벤트 주문 및 지불 생성하는 API**입니다.

- 상품 재고 예약
- 주문 생성 (idempotency 지원)
- 결제 정보 생성
- 결제 전략으로 흐름 전달 (여러 결제 방식에 따라 다른 전략을 사용하도록 만듬)
