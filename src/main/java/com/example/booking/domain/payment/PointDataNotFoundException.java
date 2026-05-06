package com.example.booking.application;

import com.example.booking.global.CustomException;
import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends CustomException {
    protected OrderNotFoundException(Throwable cause) {
        super("존재하지 않는 주문입니다.", HttpStatus.NOT_FOUND, cause);
    }
}
