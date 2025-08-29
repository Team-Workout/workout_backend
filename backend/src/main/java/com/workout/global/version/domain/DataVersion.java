package com.workout.global.version.domain;

import java.util.Map;

public record DataVersion(
    Map<String, Long> versions
) {

}
