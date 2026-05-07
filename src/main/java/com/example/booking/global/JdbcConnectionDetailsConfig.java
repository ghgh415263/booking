package com.example.booking.global;

import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring Boot 3.1의 ConnectionDetails 커스텀 설정 클래스.
 *
 * proxyBeanMethods = false 이면 @Configuration 클래스에 대한 CGLIB 프록시를 생성하지 않으므로
 * @Bean 메서드 간 호출 기반의 싱글턴 보장은 비활성화된다.
 *
 * @Primary를 지정하여 Docker Compose/Testcontainers 등에서 자동 생성되는
 * JdbcConnectionDetails 빈보다 우선 적용되도록 한다.
 * (Spring Boot 3.1부터 ConnectionDetails는 datasource 프로퍼티(yml)보다 우선권을 가진다.)
 */
@Configuration(proxyBeanMethods = false)
public class JdbcConnectionDetailsConfig {

    @Bean
    @Primary
    public JdbcConnectionDetails jdbcConnectionDetails() {
        return new JdbcConnectionDetails() {

            @Override
            public String getJdbcUrl() {
                return "jdbc:mysql://localhost:3306/booking?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&rewriteBatchedStatements=true";
            }

            @Override
            public String getUsername() {
                return "user";
            }

            @Override
            public String getPassword() {
                return "1234";
            }
        };
    }
}
