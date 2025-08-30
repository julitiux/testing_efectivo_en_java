package com.circulosiete.curso.minibank.api;

import com.circulosiete.curso.minibank.service.AccountCommandService;
import com.circulosiete.curso.minibank.service.TransferService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(AccountCommandService.AlreadyProcessedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicate(AccountCommandService.AlreadyProcessedException ex,
                                         HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Idempotency conflict");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setTitle("Bad Request");
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        // Si el servicio lanzó "Account not found", devolvemos 404; si no, 400.
        var status = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")
            ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        var pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        pd.setTitle(status == HttpStatus.NOT_FOUND ? "Not Found" : "Bad Request");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        // Si el servicio lanzó "Insufficient funds", devolvemos 422; si no, 400.
        var status = ex.getMessage() != null && ex.getMessage().toLowerCase().contains("insufficient funds")
            ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.BAD_REQUEST;
        var pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        pd.setTitle(status == HttpStatus.UNPROCESSABLE_ENTITY ? "Insufficient funds" : "Bad Request");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        var pd = ex.getBody();
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

    @ExceptionHandler(TransferService.AlreadyProcessedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateTransfer(TransferService.AlreadyProcessedException ex,
                                                 HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Idempotency conflict");
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }

}
