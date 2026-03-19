package ar.gov.arba.resilience_patterns.model;

public record PaymentRequest(
    String propertyId,
    Double amount,
    String currency,
    String conceptCode,
    boolean simulate
) {
    public PaymentRequest(String propertyId, Double amount, String currency, String conceptCode) {
        this(propertyId, amount, currency, conceptCode, false);
    }
}
