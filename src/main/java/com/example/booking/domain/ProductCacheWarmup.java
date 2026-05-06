package com.example.booking.infra;

import com.example.booking.domain.ProductDao;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCacheWarmup implements ApplicationRunner {

    private final ProductDao productDao;

    @Override
    public void run(ApplicationArguments args) {

        Long targetProductId = 1L;

        productDao.findById(targetProductId);
    }
}
