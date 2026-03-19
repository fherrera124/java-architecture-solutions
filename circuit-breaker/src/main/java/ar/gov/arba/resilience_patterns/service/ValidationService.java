package ar.gov.arba.resilience_patterns.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import ar.gov.arba.resilience_patterns.model.ValidationRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de validación de CUIT/CUIL contra la API de AFIP
 * 
 * CASO REAL: AFIP falla periódicamente durante madrugadas de procesamiento masivo
 * (entre las 02:00 y las 06:00 hs). Sin circuit breaker, esto bloqueaba transacciones
 * críticas durante esas horas.
 */
@Slf4j
@Service
public class ValidationService {
    
    // Cola de validaciones pendientes (asincrónicas)
    private static final Map<String, Map<String, Object>> PENDING_VALIDATIONS = 
        new ConcurrentHashMap<>();

    /**
     * Valida CUIT/CUIL contra API de AFIP
     * Nombre: "afipAPI" debe coincidir con application.properties
     */
    @CircuitBreaker(name = "afipAPI", fallbackMethod = "fallbackValidation")
    public Map<String, Object> validateCUIT(ValidationRequest request) {
        log.info("Validando CUIT: {} para contribuyente: {}", request.cuit(), request.fullName());
        
        // Simular falla de AFIP si se solicita
        if (request.simulate()) {
            throw new ResourceAccessException(
                "API AFIP no responde. Probablemente en mantenimiento nocturno."
            );
        }
        
        // En producción, aquí iría la llamada a AFIP
        // RestTemplate rt = new RestTemplate();
        // return rt.getForObject("https://api.afip.gob.ar/validate/" + request.cuit(), Map.class);
        
        // Para demo, validar CUIT simulado
        boolean isValid = request.cuit().length() == 11 && 
                         request.cuit().matches("\\d+");
        
        return Map.of(
            "status", "VALIDACION_EXITOSA",
            "cuit", request.cuit(),
            "valid", isValid,
            "name", request.fullName(),
            "validatedAt", System.currentTimeMillis(),
            "source", "AFIP_API"
        );
    }

    /**
     * Fallback: Se activa cuando AFIP no responde
     * Estrategia: ACEPTAR la transacción y validar de forma ASINCRÓNICA después
     * 
     * Esto es crítico en ARBA porque rechazar transacciones en horarios pico
     * causa pérdida de ingresos massiva (~$200K por hora de indisponibilidad)
     */
    public Map<String, Object> fallbackValidation(ValidationRequest request, Throwable throwable) {
        log.warn("CIRCUITO ABIERTO: API AFIP no disponible. Razón: {}", throwable.getMessage());
        log.info("Estrategia: Aceptar transacción + Validación ASINCRÓNICA posterior");
        
        // Generar ID único para esta validación pendiente
        String validationId = "VAL-" + UUID.randomUUID().toString();
        
        // Encolar para validación posterior
        PENDING_VALIDATIONS.put(validationId, Map.of(
            "cuit", request.cuit(),
            "name", request.fullName(),
            "queuedAt", System.currentTimeMillis(),
            "status", "PENDIENTE_VALIDACION"
        ));
        
        log.info("Validación encolada: {} - Se procesará cuando AFIP esté disponible", validationId);
        
        return Map.of(
            "status", "ACEPTADO_VALIDACION_PENDIENTE",
            "cuit", request.cuit(),
            "validationId", validationId,
            "message", "AFIP temporalmente no disponible. Tu CUIT será validado automáticamente " +
                      "cuando el servicio se restablezca (estimado en 30 segundos).",
            "acceptedAt", System.currentTimeMillis(),
            "nextRetry", System.currentTimeMillis() + 30000,
            "riskLevel", "LOW"  // Bajo riesgo porque se valida después
        );
    }
    
    /**
     * Método auxiliar para obtener estado de validaciones pendientes
     * (Usado internamente por un job asincrónico)
     */
    public int getPendingValidationsCount() {
        return PENDING_VALIDATIONS.size();
    }
}
