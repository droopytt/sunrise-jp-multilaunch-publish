package com.malt.multilaunch.multicontroller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WindowAssignRequestSerializationTest {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(WindowAssignRequestSerializationTest.class);

    @Test
    public void shouldSerializeToCSharpStyleFieldNames() throws IOException {
        var windowRequest = new WindowAssignRequest(1, 100, WindowAssignRequest.PairDirection.RIGHT);
        var requestAsString = windowRequest.toJsonString();
        LOG.info("String content is {}", requestAsString);
        var read = OBJECT_MAPPER.readValue(requestAsString, WindowAssignRequest.class);
        assertThat(read.groupNumber()).isEqualTo(1);
        assertThat(read.hwnd()).isEqualTo(100);
        assertThat(WindowAssignRequest.PairDirection.fromIntValue(read.pairDirection()))
                .isEqualTo(WindowAssignRequest.PairDirection.RIGHT);
    }
}
