package com.workout.apiadmin.buisness.sample.service;

import com.workout.common.buisness.sample.service.SampleCommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleApiService {

    private final SampleCommonService sampleCommonService;

    public void sample() {
        log.info("sample API SERVICE");
        sampleCommonService.sample();
    }
}
