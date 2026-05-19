package ua.edu.socbot.controller;

import org.springframework.web.bind.annotation.*;
import ua.edu.socbot.dto.ChatRequest;
import ua.edu.socbot.dto.ChatResponse;
import ua.edu.socbot.service.BotService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final BotService botService;
    public ChatController(BotService botService) { this.botService = botService; }
    @PostMapping public ChatResponse chat(@RequestBody ChatRequest request) { return botService.process(request.message()); }
}
