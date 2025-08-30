package com.workout.securityConverter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EncryptionIntegrationTest {

    @Autowired
    private AESService aesService; // 실제 빈 등록된 암호화 서비스

    @Test
    @DisplayName("AESService 암호화 후 복호화가 원래 값과 동일해야 한다")
    void aesService_encryptAndDecrypt() {
        // given
        BigDecimal raw = new BigDecimal("31.22");

        // when
        String encrypted = aesService.encrypt(raw);
        BigDecimal decrypted = aesService.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualByComparingTo(raw);
    }

    @Test
    @DisplayName("EncryptionConverter 를 통한 암호화/복호화가 정상 동작해야 한다")
    void encryptionConverter_encryptAndDecrypt() {
        // given
        EncryptionConverter converter = new EncryptionConverter(aesService);
        BigDecimal raw = new BigDecimal("12345.6789");

        // when
        String dbValue = converter.convertToDatabaseColumn(raw);
        BigDecimal entityValue = converter.convertToEntityAttribute(dbValue);

        // then
        assertThat(entityValue).isEqualByComparingTo(raw);
    }
}
