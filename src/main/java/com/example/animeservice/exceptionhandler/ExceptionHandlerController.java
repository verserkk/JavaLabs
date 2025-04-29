package com.example.animeservice.exceptionhandler;

import com.example.animeservice.dto.ErrorResponse;
import com.example.animeservice.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;


@RestControllerAdvice
public class ExceptionHandlerController {

    @ExceptionHandler(EntityNotFoundException.class)
    @ApiResponses(value = {@ApiResponse(responseCode = "404",
            description = "Entity not found")
    })
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setError("Not Found");
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponses(value = {@ApiResponse(responseCode = "400",
            description = "Validation error")
    })
    public ResponseEntity<ErrorResponse>
        handleValidationExceptions(MethodArgumentNotValidException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setError("Bad Request");
        response.setMessage(ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    @ApiResponses(value = {@ApiResponse(responseCode = "400",
            description = "Invalid argument")
    })
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setError("Bad Request");
        response.setMessage(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ApiResponses(value = {@ApiResponse(responseCode = "404",
            description = "Endpoint not found")
    })
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setError("No Handler Found");
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    @ApiResponses(value = {@ApiResponse(responseCode = "500",
            description = "Unexpected server error")
    })
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setError("Internal Server Error");
        response.setMessage(ex.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ApiResponses(value = {@ApiResponse(responseCode = "400",
            description = "Database constraint violation")
    })
    public ResponseEntity<ErrorResponse>
        handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setError("Bad Request");
        response.setMessage("Database constraint violation: "
                + Objects.requireNonNull(ex.getRootCause()).getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}