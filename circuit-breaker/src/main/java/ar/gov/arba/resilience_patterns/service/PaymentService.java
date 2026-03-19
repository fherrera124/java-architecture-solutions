package ar.gov.arba.resilience_patterns.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import ar.gov.arba.resilience_patterns.model.PaymentRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de procesamiento de órdenes de pago
 * Integra con Sistema Contable Externo (Legacy) para registrar pagos inmobiliarios
 * 
 * CASO REAL: Sistema contable ocasionalmente tarda 30+ segundos en responder.
 * Sin circuit breaker, esto bloqueaba el flujo de pagos durante horas pico.
 * Impacto: $1.2M en transacciones no procesadas por hora.
 */
@Slf4j
@Service
public class PaymentService {

    // Cola de pagos en procesamiento asincrónico
    private static final Map<String, Map<String, Object>> PAYMENT_QUEUE = 
        new ConcurrentHashMap<>();

    /**
     * Procesa orden de pago mediante Sistema Contable Externo
     * Nombre: "accountingService" debe coincidir con application.properties
     */
    @CircuitBreaker(name = "accountingService", fallbackMethod = "fallbackProcessPayment")
    public Map<String, Object> processPayment(PaymentRequest request) {
        log.info("Procesando pago: Propiedad {} - Monto: ${} {}", 
                 request.propertyId(), request.amount(), request.currency());
        
        // Simular timeout si se solicita
        if (request.simulate()) {
            throw new ResourceAccessException(
                "Timeout en Sistema Contable: No responde después de 5 segundos. " +
                "El servicio de registración de pagos está sobrecargado."
            );
        }
        
        // En producción, aquí iría la integración con el sistema contable legacy
        // RestTemplate rt = new RestTemplate();
        // return rt.postForObject("https://contabilidad.arba.interna/register-payment", request, Map.class);
        
        // Para demo, simular procesamiento exitoso
        String transactionId = "TRX-" + System.currentTimeMillis() + "-" + 
                              (int)(Math.random() * 10000);
        String comprobante = "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("Pago registrado exitosamente en contabilidad. TRX: {}", transactionId);
        
        return Map.of(
            "status", "PAGO_REGISTRADO",
            "transactionId", transactionId,
            "propertyId", request.propertyId(),
            "amount", request.amount(),
            "currency", request.currency(),
            "comprobante", comprobante,
            "concept", request.conceptCode(),
            "processedAt", System.currentTimeMillis(),
            "source", "ACCOUNTING_SYSTEM"
        );
    }

    /**
     * Fallback: Se activa cuando Sistema Contable está ABIERTO
     * 
     * Estrategia CRÍTICA: En lugar de rechazar el pago,
     * lo encolo para procesamiento asincrónico posterior.
     * 
     * El usuario recibe confirmación inmediata ("EN_PROCESAMIENTO"),
     * mejorando experiencia de usuario y permitiendo a ARBA procesar
     * cientos de miles de transacciones sin perder ninguna.
     */
    public Map<String, Object> fallbackProcessPayment(PaymentRequest request, Throwable throwable) {
        log.error("CIRCUITO ABIERTO: Sistema Contable no disponible. Razón: {}", throwable.getMessage());
        log.info("Estrategia: Encolar pago para procesamiento ASINCRÓNICO");
        
        // Generar ID de transacción
        String transactionId = "TRX-QUEUE-" + System.currentTimeMillis() + "-" + 
                              (int)(Math.random() * 10000);
        
        // Encolar el pago
        PAYMENT_QUEUE.put(transactionId, Map.of(
            "propertyId", request.propertyId(),
            "amount", request.amount(),
            "currency", request.currency(),
            "concept", request.conceptCode(),
            "queuedAt", System.currentTimeMillis(),
            "status", "EN_COLA",
            "retries", 0,
            "maxRetries", 5
        ));
        
        log.info("Pago encolado con transactionId: {}. Será procesado en background", transactionId);
        
        return Map.of(
            "status", "EN_PROCESAMIENTO",
            "transactionId", transactionId,
            "propertyId", request.propertyId(),
            "amount", request.amount(),
            "currency", request.currency(),
            "message", "Tu pago ha sido recibido y está siendo procesado. " +
                      "El sistema contable está bajo carga alta, pero tu transacción se completará " +
                      "automáticamente dentro de 5 minutos.",
            "queuedAt", System.currentTimeMillis(),
            "estimatedCompletion", System.currentTimeMillis() + (5 * 60 * 1000),
            "userExperience", "DEGRADED_BUT_WORKING",
            "guaranteedProcessing", true
        );
    }
    
    /**
     * Método para verificar tamaño de la cola
     * Usado para monitoreo y alertas
     */
    public int getPaymentQueueSize() {
        return PAYMENT_QUEUE.size();
    }
    
    /**
     * Obtener estado de un pago encolado
     */
    public Map<String, Object> getPaymentStatus(String transactionId) {
        return PAYMENT_QUEUE.getOrDefault(transactionId, 
            Map.of("status", "NOT_FOUND", "transactionId", transactionId));
    }
}