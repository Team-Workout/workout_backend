package com.workout.global.securityConverter;

import java.math.BigDecimal;

public interface Encryptor {
    String encrypt(BigDecimal raw) throws Exception;
    BigDecimal decrypt(BigDecimal encrypted) throws Exception;
}
