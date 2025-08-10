package com.workout.apistore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.workout.apistore",
    "com.workout.common",
})
@EntityScan(basePackages = {"com.com.workout.common"})
@EnableJpaRepositories(basePackages = {"com.workout.common.**.repository"})
public class ApiStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiStoreApplication.class, args);
    }

}
