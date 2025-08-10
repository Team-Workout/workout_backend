package com.workout.apiadmin.buisness.sample.controller;

import com.workout.apiadmin.buisness.sample.service.SampleApiService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SampleController {

    private final SampleApiService sampleApiService;

    @GetMapping("/v1/samples")
    public String sample() {
        sampleApiService.sample();
        return "ok";
    }

}
