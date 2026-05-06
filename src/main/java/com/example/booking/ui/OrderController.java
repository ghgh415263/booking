package com.example.booking.ui;

import com.example.booking.application.OrderPaymentCreateRequest;
import com.example.booking.application.OrderCreateResponse;
import com.example.booking.application.OrderPaymentService;
import com.example.booking.global.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/event/orders")
public class OrderPaymentController {

    private final OrderPaymentService orderPaymentService;

    @PostMapping
    public ApiSuccessResponse<OrderCreateResponse> create(@RequestBody OrderPaymentCreateRequest orderPaymentCreateRequest,
                                                          @RequestHeader("X-MEMBER-ID") Long memberId) {
        return ApiSuccessResponse.of(orderPaymentService.create(orderPaymentCreateRequest, memberId));
    }

}
