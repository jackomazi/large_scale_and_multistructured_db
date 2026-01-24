package it.unipi.chessApp.service.exception;

import it.unipi.chessApp.dto.ResponseWrapper;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ResponseWrapper<String>> handleBusinessException(
    BusinessException ex
  ) {
    ResponseWrapper<String> errorResponse = new ResponseWrapper<>(
      "Operation failed",
      ex.getMessage()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ResponseWrapper<String>> handleAuthenticationException(
    AuthenticationException ex
  ) {
    ResponseWrapper<String> errorResponse = new ResponseWrapper<>(
      "Authentication failed",
      "Invalid credentials"
    );
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ResponseWrapper<String>> handleAccessDeniedException(
    AccessDeniedException ex
  ) {
    ResponseWrapper<String> errorResponse = new ResponseWrapper<>(
      "Access denied",
      "Insufficient permissions"
    );
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGenericException(
    Exception ex
  ) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Unexpected Error");
    response.put("details", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      response
    );
  }
}
