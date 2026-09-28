# ADR-0001: Creación y modificación de usuario en Firebase del lado del cliente
- Fecha: 28/09/2026
## Contexto
Necesitamos registrar instancias de usuarios tanto en Firebase como en la base de datos interna de Spring Boot.
Inicialmente consideramos realizar la operación de Firebase en el mismo backend utilizando el patrón
Outbox.
## Decisión
Vamos a realizar el registro de la instancia de usuario en Firebase por lado del cliente y, ya creado el usuario, se 
llama al backend para registrar el usuario en la base de datos de Spring Boot.
## Consecuencias
- (+) No se necesita almacenar la contraseña (en texto plano) del usuario como payload para Outbox.
- (+) Permite focalizar la lógica de negocio en los métodos del backend.
- (-) El cliente debe tener mecanismos de recuperación ante una posible creación exitosa en Firebase y  no en la base de datos.