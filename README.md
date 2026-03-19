# Java Architecture Patterns & Resilience Samples
> Proyectos de referencia para arquitecturas distribuidas y resilientes en el ecosistema Spring.

Este repositorio contiene implementaciones prácticas de patrones de diseño orientados a la alta disponibilidad, escalabilidad y tolerancia a fallos, desarrollados con **Java 17+** y **Spring Boot 3**.

## Proyectos Incluidos

### 1. Resilience: Circuit Breaker con Resilience4j
Implementación del patrón de aislamiento para evitar fallas en cascada en microservicios.
* **Tecnologías:** Spring Cloud Circuit Breaker, Resilience4j, Micrometer.
* **Escenario:** Integración con pasarelas de pago externas con latencia variable.
* **Features:** Configuración de umbrales de error, tiempos de espera y métodos de *fallback*.

### 2. Batch Processing: Optimización de Datos Masivos
Procesamiento asíncrono y particionado de grandes volúmenes de información.
* **Tecnologías:** Spring Batch, PostgreSQL.
* **Performance:** Implementación de *Chunk-oriented processing* para optimizar el uso de memoria.

### 3. Infrastructure & DevOps (Dockerized)
Configuraciones listas para producción para el despliegue de estos servicios.
* **Stack:** Docker Compose, Nginx como Reverse Proxy, y monitoreo básico.

---

## Cómo ejecutar los ejemplos

Cada carpeta contiene su propio `docker-compose.yml` para levantar el entorno completo (Base de datos + App + Monitoreo).

```bash
cd resilience-circuit-breaker
docker-compose up -d
```

## Autor
Francisco Herrera - Software Engineer / Java Architect
