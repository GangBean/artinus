package com.artinus.subscription.api.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.artinus.subscription.api.entity.ChannelAuth;
import com.artinus.subscription.api.entity.ChannelAuthSet;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ChannelAuthSetConverter implements AttributeConverter<ChannelAuthSet, String> {
    private static final String DELIMITER = ";";

    @Override
    public String convertToDatabaseColumn(ChannelAuthSet attribute) {
        return attribute != null && attribute.getAuths().size() > 0 ? attribute.getAuths().stream()
                .sorted()
                .map(ChannelAuth::toString)
                .collect(Collectors.joining(DELIMITER)) : null;
    }

    @Override
    public ChannelAuthSet convertToEntityAttribute(String dbData) {
        return dbData != null && !dbData.isBlank() ? ChannelAuthSet.builder()
                .auths(Arrays.stream(dbData.split(DELIMITER))
                        .map(ChannelAuth::valueOf)
                        .collect(Collectors.toSet()))
                .build() : null;
    }
}
