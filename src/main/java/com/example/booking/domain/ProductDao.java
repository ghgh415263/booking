package com.example.booking.domain;

import java.util.Optional;

public interface ProductDao {

    Optional<ProductSummary> findById(Long id);
}
