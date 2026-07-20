package lk.oracene.hardware_management_api.client;

import lk.oracene.hardware_management_api.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Talks to the printer-agent service running on the till PC (see printer-agent/INSTALL.md).
 * This process itself runs in Docker and has no access to the OS print spooler, so every
 * print/drawer action is delegated to the agent over HTTP.
 */
@Slf4j
@Component
public class PrinterAgentClient {

    private final RestClient restClient;

    public PrinterAgentClient(
            @Value("${printer.agent.url}") String agentUrl,
            @Value("${printer.agent.token}") String agentToken,
            @Value("${printer.agent.timeout-seconds:10}") long timeoutSeconds) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        this.restClient = RestClient.builder()
                .baseUrl(agentUrl)
                .defaultHeader("X-Agent-Token", agentToken)
                .requestFactory(requestFactory)
                .build();
    }

    public List<String> listPrinters() {
        return execute(() -> restClient.get()
                .uri("/printers")
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {
                }));
    }

    public void printRaw(String printerName, byte[] data) {
        String dataBase64 = Base64.getEncoder().encodeToString(data);
        execute(() -> restClient.post()
                .uri("/print")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("printerName", printerName, "dataBase64", dataBase64))
                .retrieve()
                .toBodilessEntity());
    }

    public void openDrawer(String printerName, int drawerPin) {
        execute(() -> restClient.post()
                .uri("/open-drawer")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("printerName", printerName, "drawerPin", drawerPin))
                .retrieve()
                .toBodilessEntity());
    }

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientException e) {
            log.warn("Printer agent call failed: {}", e.getMessage());
            throw new BadRequestException(
                    "Printer agent is not reachable. Make sure the printer-agent service is running on the till PC. ("
                            + e.getMessage() + ")");
        }
    }
}
