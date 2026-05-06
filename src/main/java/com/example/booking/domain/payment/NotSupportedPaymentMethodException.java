package com.example.booking.domain.payment;

import com.example.booking.global.CustomException;
import org.springframework.http.HttpStatus;

public class NotSupportedPaymentMethodException extends CustomException {
    public NotSupportedPaymentMethodException() {
        super("잘못된 결제수단 선택입니다.", HttpStatus.BAD_REQUEST);
    }
}
