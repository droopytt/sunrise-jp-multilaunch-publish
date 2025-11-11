package com.malt.multilaunch.jna;

import java.awt.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class WindowData {

    private final int width;
    private final int height;
    private final int startX;
    private final int startY;
    private final int incrementX;
    private final int incrementY;

    private WindowData(int width, int height, int startX, int startY, int incrementX, int incrementY) {

        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        this.incrementX = incrementX;
        this.incrementY = incrementY;
    }

    public static WindowData create(Rectangle workingAreaRect, int numWindows, int offset) {
        int width = workingAreaRect.width / (numWindows / 2) + offset;
        int height = workingAreaRect.height / 2 + (offset / 2);
        int startX = workingAreaRect.x - (offset / 2);
        int startY = workingAreaRect.y;
        int incrementX = workingAreaRect.width / (numWindows / 2);
        int incrementY = workingAreaRect.height / 2;
        return new WindowData(width, height, startX, startY, incrementX, incrementY);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int startX() {
        return startX;
    }

    public int startY() {
        return startY;
    }

    public int incrementX() {
        return incrementX;
    }

    public int incrementY() {
        return incrementY;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
