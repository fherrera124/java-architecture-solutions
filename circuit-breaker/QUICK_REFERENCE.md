# ⚡ Quick Reference - Circuit Breaker ARBA

## 🎯 3 Circuitos en 30 Segundos

### 1️⃣ RENTAS (Consulta de Deuda)
```bash
# Prueba normal
curl http://localhost:8080/api/arba/debt?cuit=20123456789&propertyId=ABC123

# Simular falla (8+ requests abre circuito)
for i in {1..10}; do curl -X GET "http://localhost:8080/api/arba/debt?simulate=true"; done

# Ver estado
curl http://localhost:8080/actuator/health | jq ".components.rentasAPI"
```

**Estados Esperados:**
- `UP` → Normal, retorna datos de RENTAS
- `CIRCUIT_OPEN` → Degradado, retorna caché local
- `HALF_OPEN` → Probando, intenta reconectar

---

### 2️⃣ AFIP (Validación CUIT)
```bash
# POST con validación
curl -X POST http://localhost:8080/api/arba/validate-cuit \
  -H "Content-Type: application/json" \
  -d '{"cuit":"20123456789","fullName":"Juan","simulate":false}'

# Simular falla
curl -X POST http://localhost:8080/api/arba/validate-cuit \
  -H "Content-Type: application/json" \
  -d '{"cuit":"20123456789","fullName":"Juan","simulate":true}'

# Respuesta cuando abierto: "ACEPTADO_VALIDACION_PENDIENTE" + validationId
```

---

### 3️⃣ Contabilidad (Procesar Pago)
```bash
# Pago normal
curl -X POST http://localhost:8080/api/arba/process-payment \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId":"ABC123",
    "amount":5000.00,
    "currency":"ARS",
    "conceptCode":"INMUEBLE_2024"
  }'

# Simular falla
curl -X POST http://localhost:8080/api/arba/process-payment \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId":"ABC123",
    "amount":5000.00,
    "currency":"ARS",
    "conceptCode":"INMUEBLE_2024",
    "simulate":true
  }'

# Respuesta cuando abierto: "EN_PROCESAMIENTO" + transactionId
```

---

## 🔄 Esperar Recuperación Automática

```bash
# Monitorear estado en tiempo real (refresh cada 2s)
watch -n 2 'curl -s http://localhost:8080/actuator/health | jq ".components"'

# Esperar ~30-45 segundos y ver cómo se cierra el circuito automáticamente
# OPEN → HALF_OPEN → CLOSED
```

---

## 📊 3 Umbrales de Configuración

| Circuito | Timeout | Error Rate | Wait | Prueba Hits |
|----------|---------|-----------|------|-------------|
| **RENTAS** | 5s | 50% | 30s | 3 exitosos |
| **AFIP** | 3s | 30% | 20s | 2 exitosos |
| **Contable** | 4s | 40% | 45s | 3 exitosos |

**¿Cómo funcionan?**
- Si **timeout** < respuesta → Fallo
- Si **error rate** > umbral en últimas 10-20 llamadas → Abre
- Después de **wait** → Intenta **Prueba Hits** veces
- Si pruebas exitosas → Cierra automáticamente

---

## 🧪 Test Rápido (2 minutos)

### Terminal 1 - Ejecutar app
```bash
cd circuit-breaker && ./mvnw spring-boot:run
```

### Terminal 2 - Monitorear
```bash
watch -n 1 'curl -s localhost:8080/actuator/health | jq'
```

### Terminal 3 - Gatillar fallos
```bash
# Abre circuito RENTAS
for i in {1..8}; do
  curl -s "http://localhost:8080/api/arba/debt?cuit=20&simulate=true" &
done

# Mira cómo respuestas pasan de 5+ segundos a <10ms (fallback)
# Health pasa de UP a CIRCUIT_OPEN
# Después 30s espera
# Vuelve a HALF_OPEN y finalmente CLOSED
```

---

## 🎯 Estados Visuales

```
CLOSED (✅ Normal)
  ↓ (detecta > 50% fallos)
OPEN (🔴 Bloqueado)
  ↓ (espera 30s)
HALF_OPEN (⚠️ Intentando)
  ├─ Si éxito → CLOSED
  └─ Si fallo → OPEN (reinicia espera)
```

---

## 📈 Líneas de Código Clave

### Activar Circuit Breaker
```java
@CircuitBreaker(name = "rentasAPI", fallbackMethod = "fallbackDebtQuery")
public Map<String, Object> queryDebt(DebtQueryRequest request) { ... }
```

### Fallback Method
```java
public Map<String, Object> fallbackDebtQuery(DebtQueryRequest request, Throwable t) {
    return Map.of("status", "SERVICIO_DEGRADADO", "data", cachedData);
}
```

### Configuración
```properties
resilience4j.circuitbreaker.instances.rentasAPI.failureRateThreshold=50
resilience4j.circuitbreaker.instances.rentasAPI.slowCallDurationThreshold=5000ms
```

---

## 🚨 Troubleshooting en 30s

| Problema | Solución |
|----------|----------|
| Circuito no abre | Necesitas >50% fallos en 10 llamadas, intenta 8+ requests |
| No veo fallback | Esperar a que circuito abra primero (~8 requests) |
| Health no muestra CB | Habilitar en application.properties: `management.health.circuitbreakers.enabled=true` |
| Servicio no responde | Verificar que app está en puerto 8080: `curl localhost:8080/actuator/health` |

---

## 💡 3 Estrategias de Fallback

1. **RENTAS:** Retorna caché local (datos de 24h)
2. **AFIP:** Acepta + valida después (asincrónico)
3. **Contabilidad:** Encola + procesa cuando servicable online

---

## 📞 Contactanos Si

- CB no se abre después de fallos
- Fallback no se ejecuta
- Health no muestra estado del circuito
- Quieres agregar más circuit breakers

---

**Tiempo total para entender y probar: ~5 minutos** ⏱️

Documentación completa en [USAGE_GUIDE.md](./USAGE_GUIDE.md)  
Análisis detallado en [INCIDENT_ANALYSIS.md](./INCIDENT_ANALYSIS.md)
