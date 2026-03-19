package ar.gov.arba.emisiones.service;

import ar.gov.arba.emisiones.model.DeudaAutomotor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarNotificacion(DeudaAutomotor deuda) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(deuda.getEmailContribuyente());
        message.setSubject("Notificación de Deuda Próxima a Vencer - Impuesto Automotor");
        message.setText("Estimado contribuyente,\n\n" +
                "Le informamos que tiene una deuda pendiente del Impuesto Automotor para la patente " + deuda.getPatente() +
                " por un monto de $" + deuda.getMonto() + ", con vencimiento el " + deuda.getFechaVencimiento() + ".\n\n" +
                "Por favor, regularice su situación para evitar recargos.\n\n" +
                "Atentamente,\nAgencia de Recaudación de la Provincia de Buenos Aires (ARBA)");
        mailSender.send(message);
    }
}