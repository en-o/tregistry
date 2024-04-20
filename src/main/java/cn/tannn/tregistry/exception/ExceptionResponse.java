package cn.tannn.tregistry.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 异常消息
 */
@AllArgsConstructor
@Data
public class ExceptionResponse {
    private HttpStatus httpStatus;
    private String message;
}
