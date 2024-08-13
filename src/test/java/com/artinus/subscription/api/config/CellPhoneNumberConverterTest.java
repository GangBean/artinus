package com.artinus.subscription.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.artinus.subscription.api.entity.CellPhoneNumber;

public class CellPhoneNumberConverterTest {

    @Test
    void convertTest() {
        CellPhoneNumberConverter converter = new CellPhoneNumberConverter();
        CellPhoneNumber cellPhoneNumber = CellPhoneNumber.builder()
                .front("010")
                .middle("1234")
                .rear("4567")
                .build();

        Assertions.assertThat(converter.convertToDatabaseColumn(cellPhoneNumber)).isEqualTo("010-1234-4567");
    }
}
