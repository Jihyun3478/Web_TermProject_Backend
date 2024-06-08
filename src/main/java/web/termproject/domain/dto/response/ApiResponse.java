package web.termproject.domain.dto.response;

import lombok.Builder;
import lombok.Data;
import web.termproject.domain.dto.response.status.ResponseCode;

@Data
@Builder
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> response(ResponseCode code, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(code.getCode())
                .message(code.getMessage())
                .data(data)
                .build();

        return response;
    }

    public static <T> ApiResponse<T> response(ResponseCode code, String message, T data) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .code(code.getCode())
                .message(message)
                .data(data)
                .build();

        return response;
    }
}
