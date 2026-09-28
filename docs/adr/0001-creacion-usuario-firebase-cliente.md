# ADR-0001: Creación y modificación de usuario en Firebase del lado del cliente
- Fecha: 28/09/2026
## Contexto
Necesitamos registrar instancias de usuarios tanto en Firebase como en la base de datos interna de Spring Boot.
Inicialmente consideramos realizar la operación de Firebase en el mismo backend utilizando el patrón
Outbox.
## Decisión
Vamos a realizar el registro de la instancia de usuario en Firebase por lado del cliente y, ya creado el usuario, se 
llama al backend para registrar el usuario en la base de datos de Spring Boot.
## Alternativas evaluadas
Las alternativas evaluadas no son claramente inferiores con respecto a la desición tomada. Pero tienen una 
mayor complejidad de implementación y una mayor cantidad de puntos de fallo.
### 1. Gestión total de la creación y modificación del usuario en el backend
- El cliente llama al backend para crear o modificar el usuario y se crea tanto en Firebase como en la base de datos.
- (+) solución sencilla para el frontend, simplemente tiene que llamar al backend.
- (+) gestión del dual-write por outbox en el backend
- (-) se requiere almacenar el payload en el outbox y deja de ser idempotente. Si se programasen dos Outbox que hagan la misma operación, uno sobreescribiría al otro, pudiendo suceder inconsistencias como: distintas contraseñas.
- (-) se deben tomar decisiones sobre como almacenar el payload y cómo cargarlo.
- (-) se debe almacenar la contraseña del usuario en la base de datos en texto plano
### 2. Implementación de (1) con encriptación de la contraseña
- (+) la contraseña se almacena en encriptada
- (-) el mecanismo de encriptación debe ser consistente tanto en el frontend como en el backend. La contraseña del usuario pasará a ser la contraseña encriptada y el frontend lo debe considerar cuando inicia la sesión (mediante Firebase).

## Consecuencias
- (+) No se necesita almacenar la contraseña (en texto plano) del usuario como payload para Outbox.
- (+) Permite focalizar la lógica de negocio en los métodos del backend.
- (-) El cliente debe tener mecanismos de recuperación ante una posible creación exitosa en Firebase y  no en la base de datos.
- (-) Las operaciones son lanzadas desde la conexión del cliente y su éxito también dependerá de esta.