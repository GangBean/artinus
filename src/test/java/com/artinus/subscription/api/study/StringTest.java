package com.artinus.subscription.api.study;

import java.util.StringJoiner;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringTest {
    @Test
    void substring() {
        String input = "20240830";
        String output = "2024-08-30";
        String actual = new StringJoiner("-").add(input.substring(0, 4))
                .add(input.substring(4, 6))
                .add(input.substring(6, 8))
                .toString();
        Assertions.assertThat(actual).isEqualTo(output);
    }
}
