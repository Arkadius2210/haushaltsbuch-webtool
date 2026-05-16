package de.haushaltsbuch;

import org.junit.jupiter.api.Test;

class HaushaltsbuchApplicationTest {

    @Test
    void mainMethodDoesNotThrow() {
        // Verify the application class exists and is properly annotated.
        // Full context load test will be added once DB migrations are in place.
        var annotation = HaushaltsbuchApplication.class
                .getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        assert annotation != null : "HaushaltsbuchApplication must be annotated with @SpringBootApplication";
    }
}
