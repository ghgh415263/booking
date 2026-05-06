package com.example.booking.global;

/**
 * API 에러 응답 정보를 표현하는 DTO 클래스입니다.
 *
 * 클라이언트에 반환할 에러 메시지, 에러 코드, HTTP 상태 코드를 포함합니다.
 * 예:
 * {
 * "message": "Invalid request parameter",
 * "code": "INVALID_PARAMETER",
 * "status": 400
 * }
 *
 * @param message 에러 메시지 내용
 * @param code    에러 코드 (애플리케이션 내 식별자)
 * @param status  HTTP 상태 코드 (예: 400, 404, 500 등)
 */
public record ApiErrorResponse(String message, String code, int status) {
}
