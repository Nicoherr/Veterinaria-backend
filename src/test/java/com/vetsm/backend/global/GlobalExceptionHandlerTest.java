package com.vetsm.backend.global;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void illegalArgumentIsReportedAsBadRequest() {
        var response = exceptionHandler.handleIllegalArgumentException(new IllegalArgumentException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void unexpectedRuntimeFailureIsReportedAsInternalServerError() {
        var response = exceptionHandler.handleException(new IllegalStateException("Internal failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocurrió un error interno", response.getBody().get("message"));
    }

    @Test
    void malformedRequestBodyIsReportedAsBadRequest() {
        var exception = new HttpMessageNotReadableException(
                "Invalid JSON",
                new MockHttpInputMessage("{".getBytes()));

        var response = exceptionHandler.handleHttpMessageNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El cuerpo de la solicitud no es válido", response.getBody().get("message"));
    }
}
