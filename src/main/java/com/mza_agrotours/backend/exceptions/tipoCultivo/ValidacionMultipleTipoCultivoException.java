package com.mza_agrotours.backend.exceptions.tipoCultivo;


import com.mza_agrotours.backend.exceptions.AppException;

import java.util.List;

public class ValidacionMultipleTipoCultivoException extends AppException {

    public ValidacionMultipleTipoCultivoException(List<String> errores) {
        super(TipoCultivoError.VALIDACION_MULTIPLE, "Por favor, revisá los datos ingresados.", errores);
    }
}
