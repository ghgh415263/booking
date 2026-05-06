package com.example.booking.ui;

import com.example.booking.application.PaymentConfirmRequest;
import com.example.booking.application.PaymentConfirmResult;
import com.example.booking.application.PaymentService;
import com.example.booking.global.ApiSuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/event/orders")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * PG 결제 승인(confirm) 요청을 처리한다.
     *
     * <p>클라이언트에서 전달한 결제 승인 정보를 기반으로
     * PG 승인 처리 및 주문/결제 상태 확정을 수행한다.
     *
     * @param request 결제 승인 요청 정보
     * @param memberId 요청 회원 ID
     * @return 결제 승인 결과
     */
    @PostMapping("/confirm")
    public ApiSuccessResponse<PaymentConfirmResult> confirm(@RequestBody PaymentConfirmRequest request,
                                                            @RequestHeader("X-MEMBER-ID") Long memberId) {
        return ApiSuccessResponse.of(paymentService.confirmPayment(request, memberId));
    }
}
