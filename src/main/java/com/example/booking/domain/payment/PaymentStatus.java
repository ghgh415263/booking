package com.example.booking.domain;

public enum PaymentStatus {
    PENDING,     // 생성됨 (아직 결제 전)
    PROCESSING,  // 결제 진행 중
    SUCCESS,     // 성공
    FAILED       // 실패
}
