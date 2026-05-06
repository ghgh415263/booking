package com.example.booking.domain.payment;

import com.example.booking.domain.Product;

public record ReservedProduct(
        Product product,
        int quantity
) {
}
