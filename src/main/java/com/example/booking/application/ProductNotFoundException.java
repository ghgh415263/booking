package com.example.booking.application;

import com.example.booking.global.CustomException;
import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends CustomException {
    protected ProductNotFoundException() {
        super("존재하지 않는 상품입니다.", HttpStatus.NOT_FOUND);
    }
}
