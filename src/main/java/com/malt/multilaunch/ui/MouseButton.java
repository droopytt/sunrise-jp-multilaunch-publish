package com.malt.multilaunch.ui;

public enum MouseButton {
    LEFT,
    RIGHT,
    MIDDLE;

    public static MouseButton fromValue(int value) {
        return switch (value) {
            case 1 -> LEFT;
            case 2 -> MIDDLE;
            case 3 -> RIGHT;
            default -> throw new IllegalArgumentException("Invalid mouse value");
        };
    }
}
