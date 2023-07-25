package io.autoflow.spi.exception;


/**
 * @author yiuman
 * @date 2023/7/25
 */
public class ExecuteException extends RuntimeException {
    private final String serviceName;

    public ExecuteException(String serviceName) {
        this.serviceName = serviceName;
    }

    public ExecuteException(String message, String serviceName) {
        super(message);
        this.serviceName = serviceName;
    }

    public ExecuteException(String message, Throwable cause, String serviceName) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public ExecuteException(Throwable cause, String serviceName) {
        super(cause);
        this.serviceName = serviceName;
    }

    public ExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String serviceName) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.serviceName = serviceName;
    }
}
