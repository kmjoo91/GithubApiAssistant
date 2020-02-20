package me.kmjoo91.githubapiassistant.core.handler;

import java.nio.file.InvalidPathException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

@RestControllerAdvice
public class GlobalRestControllerAdvice {
	@ExceptionHandler(InvalidPathException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleInvalidPathException() {
		return "NOT_FOUND";
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleIllegalArgumentException() {
		return "BAD_REQUEST";
	}

	@ExceptionHandler(MethodNotAllowedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleMethodNotAllowedException() {
		return "Not Allowd Method";
	}
}
