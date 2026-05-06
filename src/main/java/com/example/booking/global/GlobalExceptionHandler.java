package com.example.booking.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CustomException 처리
     * 커스텀 예외는 내부에 HttpStatus와 메시지를 포함하고 있으므로,
     * 이를 이용해 적절한 상태 코드와 메시지를 응답한다.
     */
    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handleCustomException(CustomException ex) {
        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                ex.getHttpStatus().value()
        );
        return new ResponseEntity<>(error, ex.getHttpStatus());
    }

    /**
     * 유효성 검사 실패 처리
     * 필드별 에러 메시지를 모두 합쳐서 반환한다.
     */
    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();

        String errorMessages = bindingResult.getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("잘못된 요청입니다.");

        ApiErrorResponse error = new ApiErrorResponse(
                errorMessages,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 유효성 검사 실패 처리
     * 필드별 에러 메시지를 모두 합쳐서 반환한다.
     */
    @ExceptionHandler
    public ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException exception) {

        // 각 파라미터별 검증 결과
        String errorMessages = exception.getParameterValidationResults().stream()
                .flatMap(paramResult ->
                        paramResult.getResolvableErrors().stream()
                                .map(resolvable -> {
                                    // 파라미터 이름 + 메시지 조합
                                    String paramName = paramResult.getMethodParameter().getParameterName();
                                    String message = resolvable.getDefaultMessage();
                                    return paramName + ": " + message;
                                })
                )
                .collect(Collectors.joining(", "));

        ApiErrorResponse error = new ApiErrorResponse(
                errorMessages,
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 그 외 모든 예외 처리
     * 내부 에러 메시지는 노출하지 않고, 서버 오류 메시지를 대신 응답한다.
     */
    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex) {

        // 내부 로그 남기기
        log.error("Unexpected error occurred", ex);

        ApiErrorResponse error = new ApiErrorResponse(
                "서버 오류가 발생했습니다.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {

        log.warn("Illegal argument: {}", ex.getMessage());

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage() != null ? ex.getMessage() : "잘못된 요청입니다.",
                "ILLEGAL_ARGUMENT",
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {

        log.warn("Illegal state: {}", ex.getMessage());

        ApiErrorResponse error = new ApiErrorResponse(
                ex.getMessage() != null ? ex.getMessage() : "현재 상태에서 처리할 수 없습니다.",
                "ILLEGAL_STATE",
                HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
