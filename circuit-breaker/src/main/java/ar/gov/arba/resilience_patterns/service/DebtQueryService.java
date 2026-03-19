package ar.gov.arba.resilience_patterns.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import ar.gov.arba.resilience_patterns.model.DebtQueryRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class DebtQueryService {
    
    // Caché de datos para fallback
    private static final Map<String, Map<String, Object>> DEBT_CACHE = new ConcurrentHashMap<>();
    
    static {
        // Datos pre-cacheados para fallback en caso de indisponibilidad
        DEBT_CACHE.put("ABC123", Map.of(
            "debtAmount", 15234.50,
            "currency", "ARS",
            "lastUpdated", "2024-03-15T10:30:00",
            "properties", 3,
            "status", "CON_DEUDA"
        ));
        DEBT_CACHE.put("XYZ789", Map.of(
            "debtAmount", 0.0,
            "currency", "ARS",
            "lastUpdated", "2024-03-17T14:00:00",
            "properties", 2,
            "status", "SIN_DEUDA"
        ));
    }

    /**
     * Consulta deuda mediante API RENTAS con Circuit Breaker
     * Nombre: "rentasAPI" debe coincidir con application.properties
     */
    @CircuitBreaker(name = "rentasAPI", fallbackMethod = "fallbackDebtQuery")
    public Map<String, Object> queryDebt(DebtQueryRequest request) {
        log.info("Consultando deuda para CUIT: {} - Propiedad: {}", request.cuit(), request.propertyId());
        
        // Simular timeout si se solicita
        if (request.simulate()) {
            throw new ResourceAccessException(
                "Timeout en API RENTAS: No responde después de 5 segundos. " +
                "El sistema RENTAS está en mantenimiento no programado."
            );
        }
        
        // En producción, aquí iría la llamada real a RENTAS vía RestTemplate o WebClient
        // RestTemplate rt = new RestTemplate();
        // return rt.getForObject("https://api.rentas.gba.gov.ar/debt/" + request.cuit(), Map.class);
        
        // Para demo, retornamos datos simulados
        return Map.of(
            "status", "EXITOSA",
            "debtAmount", Math.random() * 50000,
            "currency", "ARS",
            "cuit", request.cuit(),
            "propertyId", request.propertyId(),
            "consultedAt", LocalDateTime.now().toString(),
            "source", "RENTAS_API"
        );
    }

    /**
     * Fallback: Se activa cuando el circuito está ABIERTO
     * Retorna datos cacheados si existen, sino informa indisponibilidad
     */
    public Map<String, Object> fallbackDebtQuery(DebtQueryRequest request, Throwable throwable) {
        log.warn("CIRCUITO ABIERTO: API RENTAS no disponible. Razón: {}", throwable.getMessage());
        
        // Intentar retornar datos en caché
        Map<String, Object> cachedData = DEBT_CACHE.get(request.propertyId());
        
        if (cachedData != null) {
            log.info("Retornando datos cacheados para propiedad: {}", request.propertyId());
            return Map.of(
                "status", "SERVICIO_DEGRADADO_CACHE",
                "message", "Sistema RENTAS temporalmente no disponible. Datos cacheados de hace 24 horas.",
                "debtAmount", cachedData.get("debtAmount"),
                "currency", cachedData.get("currency"),
                "lastUpdated", cachedData.get("lastUpdated"),
                "source", "CACHE_LOCAL",
                "warning", "Estos datos pueden haber cambiado. Consulte cuando RENTAS esté disponible"
            );
        }
        
        // Si no hay caché, informar al usuario
        return Map.of(
            "status", "SERVICIO_DEGRADADO",
            "message", "Sistema de consulta de deuda RENTAS no disponible. " +
                      "El sistema está siendo reparado y se restablecerá automáticamente " +
                      "dentro de 30 segundos. Intente nuevamente en 1 minuto.",
            "nextAttempt", System.currentTimeMillis() + 30000,
            "errorCode", "RENTAS_TIMEOUT",
            "supportContact", "soporte@arba.gob.ar"
        );
    }
}
