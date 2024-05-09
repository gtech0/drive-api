package com.project.driveapi.controller;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.project.driveapi.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;

@RestControllerAdvice
public class ControllerErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ErrorResponse errorResponse = new ErrorResponse(new Date(), HttpStatus.BAD_REQUEST.value(),
                "Validation error. Check 'errors' field for details.");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse.addValidationError(new Date(), fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(GoogleJsonResponseException.class)
    public ResponseEntity<Object> handleGoogleJsonResponseException(GoogleJsonResponseException ex) {
        ErrorResponse errorResponse = new ErrorResponse(new Date(), ex.getStatusCode(), ex.getDetails().getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }
}
