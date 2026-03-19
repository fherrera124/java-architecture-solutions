#!/bin/bash
# ====================================================================
# CASOS DE PRUEBA - Patrón Circuit Breaker en ARBA
# ====================================================================
# Este script demuestra los 5 endpoints principales
# Ejecutar con: chmod +x test-cases.sh && ./test-cases.sh
# ====================================================================

BASE_URL="http://localhost:8080/api/arba"

echo "=================================================="
echo "CASO 1: Consulta Normal de Deuda (SIN FALLA)"
echo "=================================================="
curl -X GET "${BASE_URL}/debt?cuit=20123456789&propertyId=ABC123&simulate=false" | jq .
echo ""
sleep 2

echo "=================================================="
echo "CASO 2: Simular FALLA en API RENTAS (Timeout)"
echo "=================================================="
echo "Enviando 8 requests para gatillar apertura de circuito..."
for i in {1..8}; do
  echo "Request $i..."
  curl -s -X GET "${BASE_URL}/debt?cuit=20123456789&propertyId=ABC123&simulate=true" | jq .status
  sleep 0.5
done
echo ""
sleep 2

echo "=================================================="
echo "CASO 3: Ver Circuito ABIERTO (Fallback Activado)"
echo "=================================================="
curl -X GET "${BASE_URL}/debt?cuit=20123456789&propertyId=ABC123&simulate=false" | jq .
echo ""
sleep 2

echo "=================================================="
echo "CASO 4: Validación AFIP"
echo "=================================================="
echo "Validación Normal:"
curl -X POST "${BASE_URL}/validate-cuit" \
  -H "Content-Type: application/json" \
  -d '{
    "cuit": "20123456789",
    "fullName": "Juan Pérez",
    "simulate": false
  }' | jq .
echo ""
sleep 2

echo "Validación CON FALLA (Fallback Asincrónico):"
curl -X POST "${BASE_URL}/validate-cuit" \
  -H "Content-Type: application/json" \
  -d '{
    "cuit": "20123456789",
    "fullName": "Juan Pérez",
    "simulate": true
  }' | jq .
echo ""
sleep 2

echo "=================================================="
echo "CASO 5: Procesar Pago"
echo "=================================================="
echo "Pago Normal (Sin Falla):"
curl -X POST "${BASE_URL}/process-payment" \
  -H "Content-Type: application/json" \
  -d '{
    "propertyId": "ABC123",
    "amount": 5000.00,
    "currency": "ARS",
    "conceptCode": "INMUEBLE_2024",
    "simulate": false
  }' | jq .
echo ""
sleep 2

echo "Pago CON FALLA (Encolado para procesamiento asincrónico):"
for i in {1..3}; do
  echo "Request $i..."
  curl -s -X POST "${BASE_URL}/process-payment" \
    -H "Content-Type: application/json" \
    -d '{
      "propertyId": "ABC123",
      "amount": 5000.00,
      "currency": "ARS",
      "conceptCode": "INMUEBLE_2024",
      "simulate": true
    }' | jq .transactionId
  sleep 0.5
done
echo ""
sleep 2

echo "=================================================="
echo "CASO 6: Estado de Colas"
echo "=================================================="
curl -X GET "${BASE_URL}/queue-status" | jq .
echo ""

echo "=================================================="
echo "CASO 7: Health Check del Sistema"
echo "=================================================="
curl -X GET "http://localhost:8080/actuator/health" | jq .
echo ""

echo "=================================================="
echo "✅ Pruebas completadas. Verificar logs de la aplicación"
echo "=================================================="
