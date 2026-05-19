package ua.edu.socbot.service;

import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ua.edu.socbot.dto.ShutdownResponse;

@Service
public class ShutdownService {
    private final Runnable shutdownAction;
    private final Duration delay;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "soc-ai-bot-shutdown");
        thread.setDaemon(false);
        return thread;
    });

    @Autowired
    public ShutdownService(ConfigurableApplicationContext context) {
        this(() -> {
            int exitCode = SpringApplication.exit(context, () -> 0);
            System.exit(exitCode);
        }, Duration.ofMillis(500));
    }

    ShutdownService(Runnable shutdownAction, Duration delay) {
        this.shutdownAction = shutdownAction;
        this.delay = delay;
    }

    public ShutdownResponse requestShutdown() {
        executor.schedule(shutdownAction, delay.toMillis(), TimeUnit.MILLISECONDS);
        return new ShutdownResponse("Застосунок завершує роботу.");
    }
}
