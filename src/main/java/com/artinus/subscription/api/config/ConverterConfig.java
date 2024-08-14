package com.artinus.subscription.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterConfig {
    @Bean
    public CellPhoneNumberConverter cellPhoneNumberConverter() {
        return new CellPhoneNumberConverter();
    }

    @Bean
    public ChannelAuthSetConverter channelAuthSetConverter() {
        return new ChannelAuthSetConverter();
    }
}
