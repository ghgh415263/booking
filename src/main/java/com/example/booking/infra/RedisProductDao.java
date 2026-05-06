package com.example.booking.infra;

import com.example.booking.domain.ProductSummary;
import com.example.booking.domain.ProductDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
public class RedisProductDao implements ProductDao {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public Optional<ProductSummary> findById(Long productId) {

        String value = stringRedisTemplate.opsForValue()
                .get(getKey(productId));

        if (value == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(value, ProductSummary.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getKey(long productId) {
        return "product:" + productId;
    }
}
