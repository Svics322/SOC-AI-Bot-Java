package ua.edu.socbot.nlp;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.*;

@Component
public class EntityExtractor {
    private static final Pattern IP = Pattern.compile("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern EMAIL = Pattern.compile("\\b[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\\b");
    private static final Pattern URL = Pattern.compile("\\bhttps?://[^\\s]+");
    private static final Pattern DOMAIN = Pattern.compile("\\b(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}\\b");
    private static final Pattern HASH = Pattern.compile("\\b[a-fA-F0-9]{32,64}\\b");
    private static final Pattern CVE = Pattern.compile("\\bCVE-\\d{4}-\\d{4,7}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PORT = Pattern.compile("\\b(?:порт|port)\\s*(\\d{1,5})\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HOSTNAME = Pattern.compile("\\b(?:PC|SERVER|SRV|LAPTOP|NB|WS)-[A-ZА-Я0-9-]+\\b", Pattern.CASE_INSENSITIVE);
    private static final List<String> PROTOCOLS = List.of("RDP", "SSH", "SMB", "HTTP", "HTTPS", "FTP", "VPN", "DNS");
    private static final List<String> SEVERITY = List.of("критично", "терміново", "негайно", "високий ризик");
    private static final List<String> TIME = List.of("сьогодні", "вчора", "щойно", "зранку", "вночі");

    public Map<String, List<String>> extract(String text) {
        Map<String, List<String>> e = new LinkedHashMap<>();
        add(e, "ip_address", find(IP, text)); add(e, "email", find(EMAIL, text)); add(e, "url", find(URL, text));
        add(e, "domain", find(DOMAIN, text)); add(e, "hash", find(HASH, text)); add(e, "cve", find(CVE, text));
        add(e, "port", find(PORT, text)); add(e, "hostname", find(HOSTNAME, text));
        String up = text.toUpperCase(Locale.ROOT), lo = text.toLowerCase(Locale.ROOT);
        add(e, "protocol", PROTOCOLS.stream().filter(up::contains).toList());
        add(e, "severity_marker", SEVERITY.stream().filter(lo::contains).toList());
        add(e, "time_marker", TIME.stream().filter(lo::contains).toList());
        return e;
    }
    private List<String> find(Pattern p, String text) {
        List<String> r = new ArrayList<>(); Matcher m = p.matcher(text);
        while (m.find()) r.add(m.groupCount() >= 1 && m.group(1) != null ? m.group(1) : m.group());
        return r;
    }
    private void add(Map<String, List<String>> target, String key, List<String> values) {
        List<String> clean = values.stream().filter(Objects::nonNull).map(String::trim).filter(v -> !v.isBlank()).distinct().toList();
        if (!clean.isEmpty()) target.put(key, clean);
    }
}
