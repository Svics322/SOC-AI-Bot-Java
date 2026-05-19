package ua.edu.socbot.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.socbot.dto.ShutdownResponse;
import ua.edu.socbot.service.ShutdownService;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    private final ShutdownService shutdownService;

    public SystemController(ShutdownService shutdownService) {
        this.shutdownService = shutdownService;
    }

    @PostMapping("/shutdown")
    public ShutdownResponse shutdown() {
        return shutdownService.requestShutdown();
    }
}
