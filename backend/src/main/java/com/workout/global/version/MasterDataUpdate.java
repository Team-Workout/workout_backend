package com.workout.global.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MasterDataUpdate {
  String category(); // "EXERCISE", "MUSCLE" 등 데이터 카테고리 지정

  VersionIncrementType type() default VersionIncrementType.PATCH; // 버전 네이밍 규칙 지정
}