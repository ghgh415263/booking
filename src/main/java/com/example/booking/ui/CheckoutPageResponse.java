package com.example.booking.ui;

import com.example.booking.domain.MemberPointSummary;
import com.example.booking.domain.ProductSummary;

public record CheckoutPageResponse(
        ProductSummary product,
        MemberPointSummary memberPoint) {
}
