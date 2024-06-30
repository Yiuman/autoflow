package io.autoflow.app.exceptions;

/**
 * @author yiuman
 * @date 2024/6/13
 */
public class MessageException extends RuntimeException {
    private final Integer code;

    public MessageException(Integer code) {
        this.code = code;
    }

    public MessageException(String message) {
        super(message);
        this.code = 500;
    }

    public MessageException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public MessageException(String message, Throwable cause, Integer code) {
        super(message, cause);
        this.code = code;
    }

    public MessageException(Throwable cause, Integer code) {
        super(cause);
        this.code = code;
    }

    protected MessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Integer code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
    }
}
