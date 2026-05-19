package ua.edu.socbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;
import java.util.Locale;

@Component
public class BrowserLauncher {
    private final boolean enabled;
    private final String contextPath;
    private final WebServerApplicationContext serverContext;

    public BrowserLauncher(
            @Value("${socbot.browser.open:true}") boolean enabled,
            @Value("${server.servlet.context-path:}") String contextPath,
            WebServerApplicationContext serverContext
    ) {
        this.enabled = enabled;
        this.contextPath = contextPath;
        this.serverContext = serverContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowserAfterStartup() {
        if (!enabled) {
            return;
        }

        URI uri = URI.create(localUrl(serverContext.getWebServer().getPort(), contextPath));
        Thread launcher = new Thread(() -> open(uri), "soc-ai-bot-browser-launcher");
        launcher.setDaemon(true);
        launcher.start();
    }

    static String localUrl(int port, String contextPath) {
        String path = contextPath == null || contextPath.isBlank() ? "/" : contextPath.trim();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        return "http://localhost:" + port + path;
    }

    private void open(URI uri) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
                return;
            }
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", uri.toString()).start();
            }
        } catch (Exception ignored) {
            // The web server still works; the user can open the URL manually.
        }
    }
}
