package com.workout.global.securityConverter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@RequiredArgsConstructor
@Converter
public class EncryptionConverter implements AttributeConverter<BigDecimal, BigDecimal> {

    private final Encryptor encryptor;

    @Override
    public BigDecimal convertToDatabaseColumn(BigDecimal raw){
        try {
            return encryptor.encrypt(raw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BigDecimal convertToEntityAttribute(BigDecimal encrypted){
        try {
            return encryptor.decrypt(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
