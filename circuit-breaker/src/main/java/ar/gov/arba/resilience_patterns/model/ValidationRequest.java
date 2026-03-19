package ar.gov.arba.resilience_patterns.model;

public record ValidationRequest(
    String cuit,
    String fullName,
    boolean simulate
) {
    public ValidationRequest(String cuit, String fullName) {
        this(cuit, fullName, false);
    }
}
