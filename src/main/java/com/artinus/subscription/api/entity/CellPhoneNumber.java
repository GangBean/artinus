package com.artinus.subscription.api.entity;

import java.util.StringJoiner;

import com.artinus.subscription.api.exception.CellPhoneNumberNotValidException;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder @Getter
@EqualsAndHashCode
public class CellPhoneNumber {
    private final String front;
    private final String middle;
    private final String rear;

    @Override
    public String toString() {
        return new StringJoiner("-")
                .add(front)
                .add(middle)
                .add(rear)
                .toString();
    }

    public static CellPhoneNumber from(String format) {
        String[] numbers = format.split("-");
        if (numbers.length != 3) {
            throw new CellPhoneNumberNotValidException("유효하지 않은 형태의 휴대전화 입력입니다(-로 구분 필요): " + format);
        }
        return CellPhoneNumber.builder()
                .front(numbers[0])
                .middle(numbers[1])
                .rear(numbers[2]).build();
    }
}
