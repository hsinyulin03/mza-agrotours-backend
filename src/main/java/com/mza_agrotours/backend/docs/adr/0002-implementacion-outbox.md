# ADR 0002: Implementación patrón Outbox para gestionar dual-write
- Fecha: 28/09/2026
## Contexto
Necesitamos una solución para gestionar las operaciones con dual-write (por lo general Firebase y la base de datos).
## Decisión
Implementar el patrón Outbox y tareas idempotentes para gestionar el estado de las operaciones y poder
reintentarlas en caso de fallo. Se considera como fuente de verdad la base de datos.
## Consecuencias
- (+) recuperación de datos en caso de fallo con backoff exponencial y jittering.
- (+) integración sencilla con servicios que provocan dual-write.
- (+) trazabilidad del historial de operaciones.
- (-) complejidad adicional.
- (-) posibilidad de duplicidad de datos en caso de llamadas a funciones no idempotentes.