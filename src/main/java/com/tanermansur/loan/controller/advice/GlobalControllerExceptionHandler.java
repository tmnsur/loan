package com.tanermansur.loan.controller.advice;

import com.tanermansur.loan.dto.ErrorDTO;
import com.tanermansur.loan.exception.AbstractLoanException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public void handleAuthenticationException(AuthenticationException e) {
        log.warn(e.getMessage(), e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = AbstractLoanException.class, produces = "application/json")
    public @ResponseBody ErrorDTO handleLoanException(AbstractLoanException e) {
        log.warn(e.getMessage(), e);

        return ErrorDTO.builder().message(e.getLocalizedMessage()).build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) {
        log.error(e.getMessage(), e);
    }
}
