package com.workout.global.securityConverter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EncryptionConverterTest {

    @InjectMocks
    private EncryptionConverter encryptionConverter;

    @Mock
    private Encryptor encryptor;

    @Nested
    @DisplayName("데이터 암호화")
    void EncryptWhenSave() throws Exception {
        // given
        BigDecimal privacyData = new BigDecimal("31.22");
        BigDecimal expected =

        // when -> then
        when(encryptor.encrypt())
    }
}