package com.mza_agrotours.backend.exceptions;

import com.mza_agrotours.backend.dtos.CondicionDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class UserDeleteConditionNotMetException extends RuntimeException {
    private final List<CondicionDTO> condiciones;

    public UserDeleteConditionNotMetException(String message, List<CondicionDTO> condiciones) {
        super(message);
        this.condiciones = condiciones;
    }
}
