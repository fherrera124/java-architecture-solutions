package ar.gov.arba.resilience_patterns.model;

public record DebtQueryRequest(
    String cuit,
    String propertyId,
    boolean simulate
) {
    public DebtQueryRequest(String cuit, String propertyId) {
        this(cuit, propertyId, false);
    }
}
