package it.marconi.biblioteca.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse<T> {
    private APIResponseStatus status;
    private String message;
    private Integer code;
    private Integer results;
    private T data;

    /**
     * - succes
     * - fail
     * - error
     */

    public static <T> APIResponse<T> success(T data){
        return APIResponse.<T>builder()
                .status(APIResponseStatus.SUCCESS)
                .data(data)
                .build();
    }

    public static <T extends Collection<?>> APIResponse<T> successCollection(T data){
        return APIResponse.<T>builder()
                .status(APIResponseStatus.SUCCESS)
                .data(data)
                .results(data != null ? data.size() : 0)
                .build();
    }

    public static <T> APIResponse<T> fail(String message, Integer code){
        return APIResponse.<T>builder()
                .status(APIResponseStatus.FAIL)
                .message(message)
                .code(code)
                .build();
    }

    public static <T> APIResponse<T> fail(T errors, String message, Integer code, Integer results){
        return APIResponse.<T>builder()
                .status(APIResponseStatus.FAIL)
                .message(message)
                .code(code)
                .data(errors)
                .results(results)
                .build();
    }

    public static <T> APIResponse<T> error(String message,Integer code){
        return APIResponse.<T>builder()
                .status(APIResponseStatus.ERROR)
                .message(message)
                .code(code)
                .build();
    }

    public static <T> APIResponse<T> error(T errors, String message,Integer code){
        return APIResponse.<T>builder()
                .status(APIResponseStatus.ERROR)
                .message(message)
                .code(code)
                .data(errors)
                .build();
    }
}
