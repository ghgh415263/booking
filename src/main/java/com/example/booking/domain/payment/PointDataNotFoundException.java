package com.example.booking.domain.payment;

import com.example.booking.global.CustomException;
import org.springframework.http.HttpStatus;

public class PointDataNotFoundException extends CustomException {
    public PointDataNotFoundException() {
        super("존재하지 않는 포인트 정보입니다.", HttpStatus.NOT_FOUND);
    }
}
