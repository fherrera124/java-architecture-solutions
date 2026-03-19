INSERT INTO deudas_automotor (id, patente, dni_contribuyente, email_contribuyente, monto, fecha_vencimiento) VALUES
('1', 'ABC123', '12345678', 'contribuyente1@example.com', 1500.00, CURRENT_DATE + INTERVAL '3' DAY),
('2', 'DEF456', '87654321', 'contribuyente2@example.com', 2000.00, CURRENT_DATE + INTERVAL '5' DAY),
('3', 'GHI789', '11223344', 'contribuyente3@example.com', 1800.00, CURRENT_DATE + INTERVAL '7' DAY);