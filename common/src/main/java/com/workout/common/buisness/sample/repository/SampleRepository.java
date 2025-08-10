package com.workout.common.buisness.sample.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class SampleRepository {

    public void sample() {
        log.info("sample REPOSITORY");
    }
}
