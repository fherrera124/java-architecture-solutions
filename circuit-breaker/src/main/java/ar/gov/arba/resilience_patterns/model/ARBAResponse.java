package ar.gov.arba.resilience_patterns.model;

import java.time.LocalDateTime;
import java.util.Map;

public record ARBAResponse(
    String status,
    String message,
    Object data,
    LocalDateTime timestamp,
    String circuitState,
    Map<String, Object> metadata
) {
    public static ARBAResponse success(String message, Object data) {
        return new ARBAResponse(
            "EXITOSA",
            message,
            data,
            LocalDateTime.now(),
            "CLOSED",
            Map.of()
        );
    }

    public static ARBAResponse degraded(String message, String circuitState) {
        return new ARBAResponse(
            "SERVICIO_DEGRADADO",
            message,
            null,
            LocalDateTime.now(),
            circuitState,
            Map.of("recovery", "Reintentos automáticos en progreso")
        );
    }

    public static ARBAResponse processing(String message, String transactionId) {
        return new ARBAResponse(
            "EN_PROCESAMIENTO",
            message,
            transactionId,
            LocalDateTime.now(),
            "OPEN",
            Map.of("transactionId", transactionId)
        );
    }
}
