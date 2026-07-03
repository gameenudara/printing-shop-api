package lk.oracene.hardware_management_api.exception;

import lk.oracene.hardware_management_api.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import lk.oracene.hardware_management_api.exception.TokenRefreshException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppControllerAdvisor {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse handleNFException(NotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ErrorResponse handleBadRequestException(BadRequestException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ErrorResponse handleUnauthorizedException(UnauthorizedException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ErrorResponse handleBadCredentials(BadCredentialsException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Invalid email or password");
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(TokenRefreshException.class)
    public ErrorResponse handleTokenRefreshException(TokenRefreshException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ErrorResponse handleInsufficientAuth(InsufficientAuthenticationException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Unauthorized: " + exception.getMessage());
        return errorResponse;
    }
}