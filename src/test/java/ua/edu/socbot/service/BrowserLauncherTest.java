package ua.edu.socbot.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BrowserLauncherTest {
    @Test
    void buildsRootLocalUrl() {
        assertEquals("http://localhost:8080/", BrowserLauncher.localUrl(8080, ""));
    }

    @Test
    void buildsContextPathLocalUrl() {
        assertEquals("http://localhost:18080/soc/", BrowserLauncher.localUrl(18080, "soc"));
    }
}
