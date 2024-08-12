package com.artinus.subscription.api.entity;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class CellPhoneNumber {
    private final String front;
    private final String middle;
    private final String rear;
}
