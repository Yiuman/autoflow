package io.autoflow.common.http;

import lombok.Data;

/**
 * @param <T> 返回结果类型
 * @author yiuman
 * @date 2022/6/22
 */
@Data
public class R<T> {

    private Integer code;
    private T data;
    private String message = "";

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        R<T> responseR = new R<>();
        responseR.setCode(ResultStatus.OK.getStatusCode());
        responseR.setMessage(ResultStatus.OK.getText());
        responseR.setData(data);
        return responseR;
    }

    public static <T> R<T> badRequest() {
        return error(ResultStatus.BAD_REQUEST.getStatusCode(), ResultStatus.BAD_REQUEST.getText());
    }

    public static <T> R<T> badRequest(String message) {
        return error(ResultStatus.BAD_REQUEST.getStatusCode(), message);
    }

    public static <T> R<T> error() {
        return error(ResultStatus.SERVER_ERROR.getStatusCode(), ResultStatus.SERVER_ERROR.getText());
    }

    public static <T> R<T> error(String message) {
        return error(ResultStatus.SERVER_ERROR.getStatusCode(), message);
    }

    public static <T> R<T> error(Integer code, String message) {
        R<T> responseR = new R<>();
        responseR.setCode(code);
        responseR.setMessage(message);
        return responseR;
    }
}
