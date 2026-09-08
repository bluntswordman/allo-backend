package com.allobank.splitbill.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception) {
		return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage());
	}

	@ExceptionHandler(DomainValidationException.class)
	ResponseEntity<ProblemDetail> handleDomainValidation(DomainValidationException exception) {
		return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleBeanValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

		ProblemDetail detail = createProblem(HttpStatus.BAD_REQUEST, "Validation failed",
				"One or more request fields are invalid");
		detail.setProperty("field_errors", fieldErrors);
		return ResponseEntity.badRequest().body(detail);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ProblemDetail> handleUnreadableBody() {
		return problem(HttpStatus.BAD_REQUEST, "Malformed request", "Request body is not valid JSON");
	}

	private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String message) {
		return ResponseEntity.status(status).body(createProblem(status, title, message));
	}

	private ProblemDetail createProblem(HttpStatus status, String title, String message) {
		ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
		detail.setTitle(title);
		detail.setType(URI.create("about:blank"));
		return detail;
	}
}
