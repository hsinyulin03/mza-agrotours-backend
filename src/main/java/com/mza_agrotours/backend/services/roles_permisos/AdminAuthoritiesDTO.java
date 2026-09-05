package com.mza_agrotours.backend.services.roles_permisos;

import java.util.List;

public record AdminAuthoritiesDTO (String rolNombre, List<String> permisos){
}
