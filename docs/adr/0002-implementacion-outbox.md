# ADR 0002: Implementación patrón Outbox para gestionar dual-write
- Fecha: 28/09/2026
## Contexto
Necesitamos una solución para gestionar las operaciones con dual-write (por lo general Firebase y la base de datos).
## Decisión
Implementar el patrón Outbox y tareas idempotentes para gestionar el estado de las operaciones y poder
reintentarlas en caso de fallo. Se considera como fuente de verdad la base de datos.
## Alternativas evaluadas
### 1. Método del avestruz
- No implementar nada. 
- (+) no se escribe código adicional.
- (-) si Firebase falla, los datos quedan inconsistentes y el se produce malfuncionamiento.

### 2. Escribir primero en Firebase y compensar si falla en la DB
- Se hace la escritura en Firebase y en caso de que la transacción a la DB falle, se compensa la escritura en Firebase.
- (+) poca complejidad.
- (-) complejidad adicional en los métodos de dual-write.
- (-) si el intento de conciliación falla, los datos quedan inconsistentes.

### 3. Alternativa (2) con backoff exponencial de N intentos
- Se implementa (2) con reintentos en caso de fallar la conciliación.
- (-) mantiene al cliente a la espera de una respuesta durante periodos largos según la cantidad de intentos establecido.

## Consecuencias
- (+) recuperación de datos en caso de fallo con backoff exponencial y jittering.
- (+) integración sencilla con servicios que provocan dual-write.
- (+) trazabilidad del historial de operaciones.
- (-) complejidad adicional.
- (-) posibilidad de duplicidad de datos en caso de llamadas a funciones no idempotentes.