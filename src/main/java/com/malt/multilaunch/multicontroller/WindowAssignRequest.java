package com.malt.multilaunch.multicontroller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WindowAssignRequest {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private int groupNumber;
    private long hwnd;
    private PairDirection pairDirection;

    @JsonCreator
    public WindowAssignRequest(
            @JsonProperty("GroupNumber") int groupNumber,
            @JsonProperty("HWnd") long hwnd,
            @JsonProperty("Pair") PairDirection pairDirection) {
        this.groupNumber = groupNumber;
        this.hwnd = hwnd;
        this.pairDirection = pairDirection;
    }

    @JsonGetter("GroupNumber")
    public int groupNumber() {
        return groupNumber;
    }

    @JsonGetter("HWnd")
    public long hwnd() {
        return hwnd;
    }

    @JsonGetter("Pair")
    public int pairDirection() {
        return pairDirection.ordinal();
    }

    public String toJsonString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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

    public enum ControllerMode {
        GROUP,
        MIRROR_ALL;

        public enum Substate {
            QUAD,
            ALLGROUPS,
            RESET
        }
    }

    public enum PairDirection {
        LEFT,
        RIGHT;

        public static PairDirection fromIntValue(int value) {
            return switch (value) {
                case 0 -> LEFT;
                case 1 -> RIGHT;
                default -> throw new RuntimeException("Invalid value %d".formatted(value));
            };
        }
    }
}
