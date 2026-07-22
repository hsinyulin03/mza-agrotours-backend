package com.mza_agrotours.backend.exceptions;

import com.google.firebase.auth.FirebaseAuthException;
import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.exceptions.actividad.ValidacionMultipleException;
import com.mza_agrotours.backend.exceptions.rangoEtario.RangoEtarioAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.rangoEtario.RangoEtarioInvalidoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNotFound.class)
    public ResponseEntity<?> handleUserNotFoundException(UsuarioNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("badCreds", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(UsuarioAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("userAlreadyExists", ex.getMessage()));
    }

    @ExceptionHandler(UserDeleteConditionNotMetException.class)
    public ResponseEntity<?> handleUserDeleteConditionNotMetException(UserDeleteConditionNotMetException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("userDeleteConditionNotMet", ex.getMessage(), ex.getCondiciones()));
    }

    @ExceptionHandler(TipoIdentificacionInvalidoException.class)
    public ResponseEntity<?> handleTipoIdentificacionInvalido(TipoIdentificacionInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("tipoIdentificacionInvalido", ex.getMessage()));
    }

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<?> handleFirebaseAuthException(FirebaseAuthException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(ex.getAuthErrorCode().toString(), ex.getMessage()));
    }

    @ExceptionHandler(UsuarioDeactivatedException.class)
    public ResponseEntity<?> handleUsuarioDeactivated(UsuarioDeactivatedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("usuarioDeBaja", ex.getMessage()));
    }

    @ExceptionHandler(EstablecimientoNotFoundException.class)
    public ResponseEntity<?> handleEstablecimientoNotFound(EstablecimientoNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("entityNonExistent", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("validationError", "Datos de entrada invalidos", errors));
    }

    //Actividad
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("resourceNotFound", ex.getMessage()));
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<?> handleValidacionNegocioException(ValidacionNegocioException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("validacionNegocio", ex.getMessage()));
    }

    @ExceptionHandler(DatoInvalidoException.class)
    public ResponseEntity<?> handleDatoInvalidoException(DatoInvalidoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("datoInvalido", ex.getMessage()));
    }

    @ExceptionHandler(ValidacionMultipleException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleValidacionMultiple(ValidacionMultipleException ex) {
        // Extraemos lista de errores que viajó adentro de la excepción
        List<String> listaDeErrores = ex.getErrores();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(
                        "validacionMultiple",
                        "Por favor, revisa los datos ingresados.",
                        listaDeErrores
                ));

    }

    //Rango Etario
    @ExceptionHandler(RangoEtarioAlreadyExistsException.class)
    public ResponseEntity<?> handleRangoEtarioAlreadyExistsException(RangoEtarioAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("rangoEtarioAlreadyExists", ex.getMessage()));
    }

    @ExceptionHandler(RangoEtarioInvalidoException.class)
    public ResponseEntity<?> handleEdadRangoInvalidoException(RangoEtarioInvalidoException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("rangoEtarioInvalido", ex.getMessage()));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("badRequest", "Parametros invalidos", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("internalServerError", ex.getMessage()));
    }

    /**
     * Maneja los errores cuando un @RequestParam recibe un valor inválido,
     * como un valor distinto de los permitidos para un enum. Devuelve una
     * respuesta HTTP 400 con un mensaje indicando el parámetro y el valor recibido.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String nombreParametro = ex.getName();
        String tipoEsperado = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido";
        String valorRecibido = ex.getValue() != null ? ex.getValue().toString() : "null";

        String mensajeDetallado = String.format(
                "El valor '%s' no es válido para el parámetro '%s'. Se esperaba un tipo '%s'.",
                valorRecibido, nombreParametro, tipoEsperado
        );

        ApiResponse<Void> apiResponse = ApiResponse.fail("badRequest", mensajeDetallado);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
}
