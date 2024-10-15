package com.finsightanalytics.common;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CommonApplication.class)
class CommonApplicationTests {

    @Test
    void contextLoads() {
        // This test checks if the Spring application context loads correctly
        assertThat(true).isTrue();
    }

    // Additional tests can be added here
}
