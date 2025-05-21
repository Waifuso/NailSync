package phong.demo;


import org.junit.jupiter.api.Test;
import phong.demo.Springpro.RandomNumberService;

import static org.assertj.core.api.Assertions.assertThat;

class RandomNumberServiceTest {

    private final RandomNumberService service = new RandomNumberService();

    @Test
    void testGenerateRandomNumber_WithinBounds() {
        int bound = 100;
        for (int i = 0; i < 1000; i++) { // Run multiple times to ensure randomness
            int result = service.generateRandomNumber(bound);
            assertThat(result).isGreaterThanOrEqualTo(0).isLessThan(bound);
        }
    }

    @Test
    void testGenerateRandomNumber_BoundZero_AlwaysZero() {
        int result = service.generateRandomNumber(1); // random.nextInt(1) always returns 0
        assertThat(result).isEqualTo(0);
    }
}
