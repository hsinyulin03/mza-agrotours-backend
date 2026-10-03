package com.mza_agrotours.backend.dtos.faq;

import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaqAMRequest {
    @NotBlank(message = "Ingresá la pregunta.")
    @Size(max = 160, message = "La pregunta no puede superar los 160 caracteres.")
    String pregunta;

    @NotBlank(message = "Ingresá la respuesta.")
    @Size(max = 700, message = "La respuesta no puede superar los 700 caracteres.")
    String respuesta;

    @NotNull(message = "La categoría es obligatoria")
    CategoriaFAQNombre categoria;


}
