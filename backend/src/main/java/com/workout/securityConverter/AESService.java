package com.workout.securityConverter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class AESService implements Encryptor{


    private static final String AES = "AES";
    private static final int INITIAL_VECTOR_LENGTH = 12;
    private static final String ENCRYPTION_TRANSFORM = "AES/GCM/NoPadding";

    @Value("${encryption.key}")
    private String encryptionKey;

    private Cipher cipher;
    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        try {
            this.secretKey = new SecretKeySpec(this.encryptionKey.getBytes(), AES);
        } catch (Exception e) {
            log.error("AESService secretKey Initialization FAILED: ", e);
            throw new IllegalStateException("암호화 설정 초기화 중 오류 발생", e);
        }
    }

    @Override
    public String encrypt(BigDecimal attribute) {
        // bigDecimal --> Encrypted String
        if (attribute == null) {
            return null;
        }

        try {
            String plainText = attribute.toPlainString(); // BigDecimal → String

            byte[] iv = new byte[INITIAL_VECTOR_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmParameter = new GCMParameterSpec(128, iv);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, this.secretKey, gcmParameter);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedBytes);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (GeneralSecurityException e) {
            log.error("암호화 실패", e);
            throw new IllegalStateException("암호화 중 오류 발생", e);
        }
    }

    @Override
    public BigDecimal decrypt(String encryptedText) {
        // Encrypted BigDecimal --> String
        if (encryptedText == null) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] iv = Arrays.copyOfRange(decoded, 0, INITIAL_VECTOR_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(decoded, INITIAL_VECTOR_LENGTH, decoded.length);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORM);
            GCMParameterSpec gcmParameter = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameter);

            byte[] plainText = cipher.doFinal(cipherText);
            return new BigDecimal(new String(plainText));
        } catch (GeneralSecurityException e) {
            log.error("복호화 실패", e);
            throw new IllegalStateException("복호화 중 오류 발생", e);
        }
    }
}
