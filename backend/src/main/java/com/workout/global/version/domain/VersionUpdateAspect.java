package com.workout.global.version.domain;

import com.workout.global.version.service.MasterDataVersionService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class VersionUpdateAspect {

  private final MasterDataVersionService masterDataVersionService;

  // @MasterDataUpdate 어노테이션을 포인트컷으로 사용
  @AfterReturning(pointcut = "@annotation(masterDataUpdate)")
  public void updateVersionAfterSuccess(MasterDataUpdate masterDataUpdate) {
    masterDataVersionService.updateVersion(masterDataUpdate.category());
  }
}