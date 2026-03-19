# Reingenieria Emisiones - Notificaciones Masivas de Cuota Automotor

Este proyecto es una reingeniería del sistema de notificaciones masivas de deudas próximas a vencer del Impuesto Automotor de ARBA. Anteriormente implementado en Natural/Adabas, ahora se realiza con Spring Boot y Spring Batch.

## Funcionalidad

- Consulta de deudas próximas a vencer (dentro de los próximos 7 días).
- Generación y envío de notificaciones por correo electrónico a los contribuyentes.

## Tecnologías

- Spring Boot 3.5.11
- Spring Batch
- Spring Data JPA
- H2 Database (para desarrollo)
- JavaMail

## Configuración

1. Configurar las credenciales de email en `application.yml`.
2. Ejecutar la aplicación.
3. Lanzar el job de batch manualmente o programáticamente.

## Ejecución del Batch

Para ejecutar el job de notificaciones:

```java
@Autowired
private JobLauncher jobLauncher;

@Autowired
private Job job;

public void runJob() throws Exception {
    jobLauncher.run(job, new JobParameters());
}
```

## Estructura del Proyecto

- `model/`: Entidades JPA
- `repository/`: Interfaces de repositorio
- `service/`: Servicios de negocio (e.g., envío de emails)
- `batch/`: Configuración de Spring Batch