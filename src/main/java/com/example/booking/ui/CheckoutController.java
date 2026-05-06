package com.example.booking.ui;

import com.example.booking.domain.MemberPointDao;
import com.example.booking.domain.MemberPointSummary;
import com.example.booking.domain.ProductDao;
import com.example.booking.domain.ProductSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/event/checkouts")
public class CheckoutController {

    private final ProductDao productDao;

    private final MemberPointDao memberPointDao;

    @GetMapping
    public CheckoutPageResponse getCheckout(@RequestParam Long productId,
                                            @RequestHeader("X-MEMBER-ID") Long memberId) {

        ProductSummary product = productDao.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음"));

        MemberPointSummary point = memberPointDao.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 포인트 없음"));

        return new CheckoutPageResponse(product, point);
    }
}
