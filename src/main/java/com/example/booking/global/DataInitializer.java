package com.example.booking.global;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. "--test.case"로 넘어온 값이 있는지 확인
        if (args.containsOption("test.case")) {
            String caseName = args.getOptionValues("test.case").get(0);
            log.info(">>>> 테스트 케이스 실행 시작: {}", caseName);

            // 2. SQL 실행 (예: hotel.sql)
            executeSql(caseName + ".sql");
        }
    }

    private void executeSql(String fileName) {
        Resource resource = new ClassPathResource("test-data/" + fileName);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setSqlScriptEncoding("UTF-8");
        DatabasePopulatorUtils.execute(populator, dataSource);
        log.info(">>>> SQL 스크립트 실행 완료: {}", fileName);
    }

}
