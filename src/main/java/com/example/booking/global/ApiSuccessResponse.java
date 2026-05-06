package com.example.booking.ui;

/**
 * API 성공 응답 정보를 표현하는 DTO 클래스입니다.
 *
 * 예:
 * {
 *   "message": "요청이 성공했습니다.",
 *   "code": "SUCCESS",
 *   "status": 200,
 *   "data": { ... }
 * }
 *
 * @param message 응답 메시지
 * @param code    응답 코드
 * @param status  HTTP 상태 코드
 * @param data    응답 데이터
 */
public record ApiSuccessResponse<T>(String message, String code, int status, T data) {

    public static <T> ApiSuccessResponse<T> of(T data) {
        return new ApiSuccessResponse<>("요청이 성공했습니다.", "SUCCESS", 200, data);
    }

    public static ApiSuccessResponse<Void> empty() {
        return new ApiSuccessResponse<>("요청이 성공했습니다.", "SUCCESS", 200, null);
    }
}
