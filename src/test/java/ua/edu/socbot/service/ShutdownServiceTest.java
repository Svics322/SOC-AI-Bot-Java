package ua.edu.socbot.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShutdownServiceTest {
    @Test
    void schedulesShutdownAfterReturningResponse() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        ShutdownService service = new ShutdownService(calls::incrementAndGet, Duration.ofMillis(20));

        String message = service.requestShutdown().message();

        assertEquals("Застосунок завершує роботу.", message);
        Thread.sleep(120);
        assertEquals(1, calls.get());
    }
}
