package com.example.booking.ui;

import com.example.booking.application.OrderCreateRequest;
import com.example.booking.application.OrderCreateResponse;
import com.example.booking.application.OrderService;
import com.example.booking.global.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/event/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiSuccessResponse<OrderCreateResponse> create(@RequestBody OrderCreateRequest orderCreateRequest,
                                                          @RequestHeader("X-MEMBER-ID") Long memberId) {
        return ApiSuccessResponse.of(orderService.create(orderCreateRequest, memberId));
    }

}
