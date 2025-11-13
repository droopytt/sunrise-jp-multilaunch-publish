package com.malt.multilaunch.ffm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

class CoreAssignerTest {

    @Test
    public void testAssignments() {
        var numCores = 4;
        var coreAssigner = CoreAssigner.create(numCores);

        assertThat(coreAssigner.assignedCores()).isEqualTo(new long[] {0, 0, 0, 0});

        var nextCore = coreAssigner.getNextAvailableCore(210);
        assertThat(nextCore).isEqualTo(0);
        assertThat(coreAssigner.assignedCores()).isEqualTo(new long[] {210, 0, 0, 0});

        for (int i = 1; i < numCores; i++) {
            assertThat(coreAssigner.getNextAvailableCore(
                            ThreadLocalRandom.current().nextInt()))
                    .isEqualTo(i);
        }

        assertThat(coreAssigner.getNextAvailableCore(200)).isLessThan(numCores);

        coreAssigner.removeAssignedCore(210);
        assertThat(coreAssigner.getNextAvailableCore(500)).isEqualTo(0);
    }
}
