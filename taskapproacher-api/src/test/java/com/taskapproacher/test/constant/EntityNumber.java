package com.taskapproacher.test.constant;

public enum EntityNumber {
    FIRST(0),
    SECOND(1),
    THIRD(3),
    FOURTH(4);

    private final int number;

    EntityNumber(int number) {
        this.number = number;
    }

    public int getIntValue() {
        return this.number;
    }
}
