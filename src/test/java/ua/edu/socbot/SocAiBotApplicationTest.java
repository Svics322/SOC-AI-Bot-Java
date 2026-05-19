package ua.edu.socbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "socbot.browser.open=false"
)
class SocAiBotApplicationTest {
    @Test
    void contextLoads() {
    }
}
