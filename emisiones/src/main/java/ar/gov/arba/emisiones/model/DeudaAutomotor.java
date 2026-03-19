package ar.gov.arba.emisiones.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "deudas_automotor")
public class DeudaAutomotor {

    @Id
    private String id;

    private String patente;
    private String dniContribuyente;
    private String emailContribuyente;
    private BigDecimal monto;
    private LocalDate fechaVencimiento;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getDniContribuyente() {
        return dniContribuyente;
    }

    public void setDniContribuyente(String dniContribuyente) {
        this.dniContribuyente = dniContribuyente;
    }

    public String getEmailContribuyente() {
        return emailContribuyente;
    }

    public void setEmailContribuyente(String emailContribuyente) {
        this.emailContribuyente = emailContribuyente;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}