package com.workout.global.version;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class VersionUpdateAspect {

  private final MasterDataVersionRepository versionRepository;

  // @MasterDataUpdate 어노테이션을 타겟으로 하고, 어노테이션 자체를 파라미터로 받는다.
  @Pointcut("@annotation(masterDataUpdate)")
  public void masterDataUpdatePointcut(MasterDataUpdate masterDataUpdate) {}

  // Pointcut에 정의된 파라미터를 Advice 메소드에서 받아서 사용한다.
  @AfterReturning(pointcut = "masterDataUpdatePointcut(masterDataUpdate)")
  public void updateVersionByCategory(JoinPoint joinPoint, MasterDataUpdate masterDataUpdate) {

    // 1. 어노테이션에서 카테고리(dataType)를 동적으로 가져온다.
    String dataType = masterDataUpdate.category();
    VersionIncrementType incrementType = masterDataUpdate.type();

    MasterDataVersion version = versionRepository.findById(dataType)
        .orElse(new MasterDataVersion(dataType, "0.0.0")); // 초기 버전은 0.0.0으로 설정

    // 2. 버전 네이밍 규칙에 따라 새 버전을 계산한다.
    String newVersion = calculateNextVersion(version.getVersion(), incrementType);

    version.updateVersion(newVersion);
    versionRepository.save(version);
  }

  private String calculateNextVersion(String currentVersion, VersionIncrementType type) {
    try {
      String[] parts = currentVersion.split("\\.");
      int major = Integer.parseInt(parts[0]);
      int minor = Integer.parseInt(parts[1]);
      int patch = Integer.parseInt(parts[2]);

      switch (type) {
        case MAJOR:
          major++;
          minor = 0;
          patch = 0;
          break;
        case MINOR:
          minor++;
          patch = 0;
          break;
        case PATCH:
          patch++;
          break;
      }
      return String.format("%d.%d.%d", major, minor, patch);
    } catch (Exception e) {
      // 버전 형식이 "1.0.0"과 맞지 않을 경우, 새로운 UUID로 초기화 (안전장치)
      return "1.0.0";
    }
  }
}