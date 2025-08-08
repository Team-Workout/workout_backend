package com.workout.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class UserSessionDto implements Serializable {
    private Long id;
    private String email;
}
