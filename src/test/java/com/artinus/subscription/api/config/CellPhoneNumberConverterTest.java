package com.artinus.subscription.api.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.artinus.subscription.api.entity.CellPhoneNumber;

public class CellPhoneNumberConverterTest {

    @Test
    void convertTest() {
        CellPhoneNumberConverter converter = new CellPhoneNumberConverter();
        String number = "010-1234-4567";
        CellPhoneNumber cellPhoneNumber = CellPhoneNumber.from(number);

        Assertions.assertThat(converter.convertToDatabaseColumn(cellPhoneNumber)).isEqualTo(number);
    }
}
