package com.mza_agrotours.backend;

import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.EstadoEstablecimientoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class BackendApplicationTests extends AbstractIntegrationTest {

	@Autowired
	private EstadoEstablecimientoRepository estadoEstablecimientoRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void seedersPopulateTheEstadoEstablecimientoCatalog() {
		assertThat(estadoEstablecimientoRepository
				.findByNombreAndFechaBajaIsNull(EstadoEstablecimientoNombre.SUSPENDIDO))
				.isPresent();
	}

}