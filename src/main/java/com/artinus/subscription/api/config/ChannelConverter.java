package com.artinus.subscription.api.config;

import com.artinus.subscription.api.entity.Channel;
import com.artinus.subscription.api.entity.ChannelType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ChannelConverter implements AttributeConverter<Channel, String> {

    @Override
    public String convertToDatabaseColumn(Channel attribute) {
        return attribute.getChannelType().toString();
    }

    @Override
    public Channel convertToEntityAttribute(String dbData) {
        return dbData != null ? Channel.builder()
                .channelType(ChannelType.valueOf(dbData))
                .build() : null;
    }
}
