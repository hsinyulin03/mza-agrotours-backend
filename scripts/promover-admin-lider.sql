-- ============================================================================
-- Promueve a un usuario YA REGISTRADO al rol "Administrador Líder".
--
-- REQUISITO PREVIO: la persona debe haberse registrado por la app
-- (POST /usuario/create). No alcanza con insertar una fila en `usuario`:
-- FirebaseTokenFilter valida un ID token contra Firebase Auth y recién ahí
-- resuelve el usuario por email, así que un `firebaseuid` inventado a mano
-- nunca va a poder loguearse.
--
-- Por qué hace falta este script: AdministradorSistemasService.createAdmin()
-- excluye adrede el rol Administrador Líder (filtro NombreIsNotContaining),
-- así que por API no se puede crear otro. Esto lo saltea a propósito.
--
-- USO: cambiar el email en la línea marcada y ejecutar.
--   docker exec -i mza-agrotours-backend-db-1 \
--     psql -U root_dev -d mza-agrotours-bd -v ON_ERROR_STOP=1 \
--     < scripts/promover-admin-lider.sql
-- ============================================================================

BEGIN;

-- 1) Alta del administrador líder.
--    Los guards evitan: usuario inexistente o dado de baja, rol dado de baja,
--    y que la persona ya tenga un rol de admin vigente (administrador_sistemas
--    se consulta con findByEmailActivo(), que devuelve Optional y explota con
--    NonUniqueResultException si hay dos filas activas para el mismo email).
INSERT INTO administrador_sistemas (id, usuario_id, rol_id, fecha_hora_alta, fecha_hora_baja)
SELECT gen_random_uuid(), u.id, r.id, NOW(), NULL
FROM usuario u
JOIN rol r
  ON r.nombre = 'Administrador Líder'
 AND r.fecha_hora_baja IS NULL
WHERE lower(u.email) = lower('CAMBIAR@ejemplo.com')   -- <<<<<< EDITAR ACÁ
  AND u.fecha_hora_baja IS NULL
  AND NOT EXISTS (
        SELECT 1
        FROM administrador_sistemas a
        WHERE a.usuario_id = u.id
          AND a.fecha_hora_baja IS NULL
  );

-- 2) Verificación: tiene que listar al nuevo admin con rol "Administrador Líder".
--    Si no aparece, revisá el email (o ya tenía un rol admin vigente) y hacé ROLLBACK.
SELECT u.email,
       r.nombre       AS rol,
       a.fecha_hora_alta
FROM administrador_sistemas a
JOIN usuario u ON u.id = a.usuario_id
JOIN rol r     ON r.id = a.rol_id
WHERE a.fecha_hora_baja IS NULL
ORDER BY a.fecha_hora_alta;

COMMIT;
