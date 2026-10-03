package com.mza_agrotours.backend.services;

import com.google.api.core.ApiFutures;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.outbox.EstadoOutbox;
import com.mza_agrotours.backend.enums.outbox.TipoOperacion;
import com.mza_agrotours.backend.repositories.OutboxRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.schedules.OutboxScheduler;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureActividad;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * La baja de una actividad deja sus chats de solo lectura via outbox. Los chats se encuentran
 * en el indice del establecimiento filtrando por el sufijo del chatId, asi que el establecimiento
 * tiene ademas un chat de otra actividad que no se tiene que tocar.
 * Sin @Transactional a proposito, igual que OutboxFlowIT: el listener solo corre con un commit real.
 */
class ActividadBajaChatIT extends AbstractIntegrationTest {

    @Autowired private ActividadService actividadService;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private OutboxRepository outboxRepository;

    @Autowired private FixtureEstablecimiento establecimientos;
    @Autowired private FixtureActividad actividades;

    @MockitoBean
    private OutboxScheduler outboxScheduler;

    private Establecimiento establecimiento;
    private Actividad actividad;
    private Actividad otraActividad;

    private DatabaseReference chatsDelEstablecimiento;
    private DatabaseReference raiz;

    @BeforeEach
    void setUp() {
        this.establecimiento = establecimientos.establecimientoActivo();
        this.actividad = actividades.actividadPublicadaEn(establecimiento);
        this.otraActividad = actividades.actividadPublicadaEn(establecimiento);

        DatabaseReference chatsEstablecimiento = mock(DatabaseReference.class);
        this.chatsDelEstablecimiento = mock(DatabaseReference.class);
        this.raiz = mock(DatabaseReference.class);

        when(firebaseDatabase.getReference("chats_establecimiento")).thenReturn(chatsEstablecimiento);
        when(chatsEstablecimiento.child(establecimiento.getId().toString())).thenReturn(chatsDelEstablecimiento);
        when(firebaseDatabase.getReference()).thenReturn(raiz);
        when(raiz.updateChildrenAsync(anyMap())).thenReturn(ApiFutures.immediateFuture(null));
    }

    @AfterEach
    void limpiarOutbox() {
        outboxRepository.deleteAll();
    }

    @Test
    void dadoQueLaActividadTieneChats_cuandoSeDaDeBaja_entoncesSoloSusChatsQuedanDeBaja() {
        String chatA = "visitanteA_" + actividad.getId();
        String chatB = "visitanteB_" + actividad.getId();
        String chatDeOtraActividad = "visitanteA_" + otraActividad.getId();
        establecimientoConChats(chatA, chatB, chatDeOtraActividad);

        actividadService.darBajaActividad(establecimiento.getId(), actividad.getId());

        assertThat(escrituraEnFirebase()).containsOnly(
                Map.entry("/chats/" + chatA + "/baja", true),
                Map.entry("/chats/" + chatB + "/baja", true));
        assertThat(filaDe(actividad).getEstado()).isEqualTo(EstadoOutbox.EXITOSO);
    }

    @Test
    void dadoQueLaActividadNoTieneChats_cuandoSeDaDeBaja_entoncesNoSeEscribeEnFirebase() {
        establecimientoConChats("visitanteA_" + otraActividad.getId());

        actividadService.darBajaActividad(establecimiento.getId(), actividad.getId());

        verify(raiz, never()).updateChildrenAsync(anyMap());
        assertThat(filaDe(actividad).getEstado()).isEqualTo(EstadoOutbox.EXITOSO);
    }

    @Test
    void dadoQueFirebaseFalla_cuandoSeDaDeBaja_entoncesLaBajaSeMantieneYLaFilaQuedaParaReintentar() {
        doThrow(new RuntimeException("Firebase caido")).when(chatsDelEstablecimiento).addListenerForSingleValueEvent(any());

        actividadService.darBajaActividad(establecimiento.getId(), actividad.getId());

        assertThat(actividadRepository.findById(actividad.getId()).orElseThrow().getFechaHoraBaja())
                .as("un fallo de Firebase despues del commit no revierte la baja")
                .isNotNull();

        Outbox fila = filaDe(actividad);
        assertThat(fila.getEstado()).isEqualTo(EstadoOutbox.PENDIENTE);
        assertThat(fila.getReintentos()).isEqualTo(1);
    }

    private void establecimientoConChats(String... chatIds) {
        List<DataSnapshot> hijos = Arrays.stream(chatIds).map(chatId -> {
            DataSnapshot hijo = mock(DataSnapshot.class);
            when(hijo.getKey()).thenReturn(chatId);
            return hijo;
        }).toList();

        DataSnapshot chats = mock(DataSnapshot.class);
        when(chats.getChildren()).thenReturn(hijos);

        doAnswer(invocation -> {
            invocation.<ValueEventListener>getArgument(0).onDataChange(chats);
            return null;
        }).when(chatsDelEstablecimiento).addListenerForSingleValueEvent(any());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> escrituraEnFirebase() {
        ArgumentCaptor<Map<String, Object>> updates = ArgumentCaptor.forClass(Map.class);
        verify(raiz).updateChildrenAsync(updates.capture());
        return updates.getValue();
    }

    private Outbox filaDe(Actividad actividad) {
        List<Outbox> filas = outboxRepository.findAll().stream()
                .filter(outbox -> outbox.getEntidadId().equals(actividad.getId().toString()))
                .toList();
        assertThat(filas).hasSize(1);
        assertThat(filas.get(0).getOperacion()).isEqualTo(TipoOperacion.QUITAR_ACTIVIDAD_CHAT);
        return filas.get(0);
    }
}
