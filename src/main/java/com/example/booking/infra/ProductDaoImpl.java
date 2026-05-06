package com.example.booking.infra;

import com.example.booking.domain.ProductDao;
import com.example.booking.domain.ProductRepository;
import com.example.booking.domain.ProductSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductDaoImpl implements ProductDao {

    private final ProductRepository productRepository;

    /**
     * 상품 단건 조회 (캐시 적용)
     *
     * <p>상품 기본 정보는 변경 빈도가 낮고 조회 트래픽이 높기 때문에
     * Caffeine 캐시를 적용한다.
     *
     * @param id 상품 ID
     * @return 상품 요약 정보
     */
    @Override
    @Cacheable(cacheNames = "product", key = "#id")
    public Optional<ProductSummary> findById(Long id) {
        return productRepository.findById(id)
                .map(ProductSummary::from);
    }
}
