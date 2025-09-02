package com.workout.securityConverter;

import java.math.BigDecimal;

public interface Encryptor {
    String encrypt(BigDecimal raw) throws Exception;
    BigDecimal decrypt(String encrypted) throws Exception;
}
