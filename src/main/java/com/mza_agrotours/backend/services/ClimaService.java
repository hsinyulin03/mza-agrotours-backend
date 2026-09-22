package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.clients.openweather.OpenWeatherClient;
import com.mza_agrotours.backend.dtos.clima.PronosticoSlot;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.clima.ClimaDptoDia;
import com.mza_agrotours.backend.enums.CondicionClima;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.repositories.ClimaRepository;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
public class ClimaService {
    public static final String ZONA_ID = "America/Argentina/Buenos_Aires";

    private static final Logger log = LoggerFactory.getLogger(ClimaService.class);
    private static final ZoneId ZONA = ZoneId.of(ZONA_ID);
    private static final int SLOTS_MINIMOS_DIA_FUTURO = 5;

    private final ClimaRepository climaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final OpenWeatherClient openWeatherClient;

    public ClimaService(ClimaRepository climaRepository,
                        DepartamentoRepository departamentoRepository,
                        OpenWeatherClient openWeatherClient) {
        this.climaRepository = climaRepository;
        this.departamentoRepository = departamentoRepository;
        this.openWeatherClient = openWeatherClient;
    }

    public List<ClimaDptoDia> obtenerPronosticoByDepartamentoNombre(String departamentoNombre) {
        Departamento departamento = this.departamentoRepository.findByNombre(departamentoNombre)
                .orElseThrow(() -> new EntityNotFoundException("Departamento " + departamentoNombre + " no encontrado"));

        return this.climaRepository.findByDepartamento(departamento);
    }

    public void actualizarPronosticos() {
        List<Departamento> departamentos = this.departamentoRepository.findAll();
        int actualizados = 0;

        for (Departamento departamento : departamentos) {
            try {
                this.actualizarPronostico(departamento);
                actualizados++;
            } catch (Exception e) {
                log.error("No se pudo actualizar el pronostico de {}", departamento.getNombre(), e);
            }
        }

        this.climaRepository.eliminarAnterioresA(LocalDate.now(ZONA));
        log.info("Pronostico actualizado para {}/{} departamentos", actualizados, departamentos.size());
    }

    private void actualizarPronostico(Departamento departamento) {
        List<PronosticoSlot> slots = this.openWeatherClient
                .obtenerPronostico(departamento.getLat(), departamento.getLon());
        this.climaRepository.saveAll(this.agregarPorDia(departamento, slots));
    }

    private List<ClimaDptoDia> agregarPorDia(Departamento departamento, List<PronosticoSlot> slots) {
        LocalDate hoy = LocalDate.now(ZONA);

        Map<LocalDate, ClimaDptoDia> existentes = this.climaRepository.findByDepartamento(departamento)
                .stream()
                .collect(Collectors.toMap(ClimaDptoDia::getFecha, Function.identity(), (a, b) -> a));

        Map<LocalDate, List<PronosticoSlot>> porFecha = slots.stream()
                .filter(slot -> !slot.fecha().isBefore(hoy))
                .collect(Collectors.groupingBy(PronosticoSlot::fecha, TreeMap::new, Collectors.toList()));

        List<ClimaDptoDia> dias = new ArrayList<>();
        for (Map.Entry<LocalDate, List<PronosticoSlot>> entrada : porFecha.entrySet()) {
            LocalDate fecha = entrada.getKey();
            List<PronosticoSlot> delDia = entrada.getValue();

            if (fecha.isAfter(hoy) && delDia.size() < SLOTS_MINIMOS_DIA_FUTURO) {
                continue;
            }

            ClimaDptoDia dia = existentes.getOrDefault(fecha, new ClimaDptoDia());
            dia.setDepartamento(departamento);
            dia.setFecha(fecha);
            dia.setTemperaturaMax(this.maximo(delDia, PronosticoSlot::temperaturaMax));
            dia.setTemperaturaMin(this.minimo(delDia, PronosticoSlot::temperaturaMin));
            dia.setTemperaturaMed(this.promedio(delDia));
            dia.setProbabilidadLluvia(this.maximo(delDia, PronosticoSlot::probabilidadLluvia));
            dia.setCondicion(this.condicionPredominante(delDia));

            dias.add(dia);
        }

        return dias;
    }

    private CondicionClima condicionPredominante(List<PronosticoSlot> slots) {
        return slots.stream()
                .map(PronosticoSlot::condicion)
                .max(Comparator.comparingInt(CondicionClima::getPrioridad))
                .orElseThrow();
    }

    private Double maximo(List<PronosticoSlot> slots, ToDoubleFunction<PronosticoSlot> campo) {
        return this.redondear(slots.stream().mapToDouble(campo).max().orElseThrow());
    }

    private Double minimo(List<PronosticoSlot> slots, ToDoubleFunction<PronosticoSlot> campo) {
        return this.redondear(slots.stream().mapToDouble(campo).min().orElseThrow());
    }

    private Double promedio(List<PronosticoSlot> slots) {
        return this.redondear(slots.stream()
                .mapToDouble(PronosticoSlot::temperatura)
                .average()
                .orElseThrow());
    }

    private Double redondear(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
