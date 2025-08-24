package com.workout.gym.service;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GymService {

  private final GymRepository gymRepository;

  public GymService(GymRepository gymRepository) {
    this.gymRepository = gymRepository;
  }

  public Gym findById(Long id) {
    return gymRepository.findById(id)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.AUTHENTICATION_FAILED));
  }
}
