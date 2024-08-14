package com.artinus.subscription.api.config;

import java.util.StringJoiner;

import com.artinus.subscription.api.entity.CellPhoneNumber;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public final class CellPhoneNumberConverter implements AttributeConverter<CellPhoneNumber, String> {

    private static final String DELIMITER = "-";

    @Override
    public String convertToDatabaseColumn(CellPhoneNumber attribute) {
        return new StringJoiner(DELIMITER)
                .add(attribute.getFront())
                .add(attribute.getMiddle())
                .add(attribute.getRear())
                .toString();
    }

    @Override
    public CellPhoneNumber convertToEntityAttribute(String dbData) {
        return dbData != null ? CellPhoneNumber.from(dbData) : null;
    }
}
