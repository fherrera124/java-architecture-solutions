package ar.gov.arba.resilience_patterns.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import ar.gov.arba.resilience_patterns.model.PaymentRequest;
import ar.gov.arba.resilience_patterns.model.DebtQueryRequest;
import ar.gov.arba.resilience_patterns.model.ValidationRequest;
import ar.gov.arba.resilience_patterns.service.PaymentService;
import ar.gov.arba.resilience_patterns.service.DebtQueryService;
import ar.gov.arba.resilience_patterns.service.ValidationService;

import java.util.Map;

/**
 * Controlador para casos de uso de ARBA
 * Demuestra el patrón Circuit Breaker en 3 escenarios críticos
 */
@RestController
@RequestMapping("/api/arba")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final DebtQueryService debtQueryService;
    private final ValidationService validationService;

    /**
     * ENDPOINT 1: Consultar Deuda Catastral (Integración RENTAS)
     * GET /api/arba/debt?cuit=20123456789&propertyId=ABC123&simulate=false
     */
    @GetMapping("/debt")
    public Map<String, Object> queryDebt(
            @RequestParam String cuit,
            @RequestParam String propertyId,
            @RequestParam(defaultValue = "false") boolean simulate) {
        
        DebtQueryRequest request = new DebtQueryRequest(cuit, propertyId, simulate);
        return debtQueryService.queryDebt(request);
    }

    /**
     * ENDPOINT 2: Validar CUIT/CUIL (Integración AFIP)
     * POST /api/arba/validate-cuit
     */
    @PostMapping("/validate-cuit")
    public Map<String, Object> validateCUIT(@RequestBody ValidationRequest request) {
        return validationService.validateCUIT(request);
    }

    /**
     * ENDPOINT 3: Procesar Orden de Pago (Integración Sistema Contable)
     * POST /api/arba/process-payment
     */
    @PostMapping("/process-payment")
    public Map<String, Object> processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPayment(request);
    }

    /**
     * ENDPOINT 4: Monitoreo - Estado de Pago Encolado
     * GET /api/arba/payment-status/{transactionId}
     */
    @GetMapping("/payment-status/{transactionId}")
    public Map<String, Object> getPaymentStatus(@PathVariable String transactionId) {
        return paymentService.getPaymentStatus(transactionId);
    }

    /**
     * ENDPOINT 5: Monitoreo - Tamaño de Cola de Pagos
     * GET /api/arba/queue-status
     */
    @GetMapping("/queue-status")
    public Map<String, Object> getQueueStatus() {
        return Map.of(
            "paymentQueueSize", paymentService.getPaymentQueueSize(),
            "pendingValidations", validationService.getPendingValidationsCount(),
            "message", "Estado actual de las colas de procesamiento asincrónico"
        );
    }
}
