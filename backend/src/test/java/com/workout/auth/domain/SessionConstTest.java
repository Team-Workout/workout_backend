package com.workout.auth.domain;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionConstTest {

  @Test
  @DisplayName("private 생성자를 호출하면 예외가 발생하거나 정상적으로 커버리지를 만족시킨다")
  void private_constructor_test() throws Exception {
    Constructor<SessionConst> constructor = SessionConst.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();
  }
}