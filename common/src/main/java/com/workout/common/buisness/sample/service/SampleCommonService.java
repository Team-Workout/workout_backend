package com.workout.common.buisness.sample.service;

import com.workout.common.buisness.sample.repository.SampleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleCommonService {

    private final SampleRepository sampleRepository;

    public void sample() {
        log.info("sample COMMON SERVICE");
        sampleRepository.sample();
    }
}
