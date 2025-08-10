package com.workout.apiadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.workout.apiadmin",
    "com.workout.common",
})
@EntityScan(basePackages = {"com.com.workout.common"})
@EnableJpaRepositories(basePackages = {"com.workout.common.**.repository"})
public class ApiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiAdminApplication.class, args);
    }

}
