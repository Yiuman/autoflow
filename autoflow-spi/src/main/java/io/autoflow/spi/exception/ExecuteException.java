package io.autoflow.spi.exception;


/**
 * @author yiuman
 * @date 2023/7/25
 */
public class ExecuteException extends RuntimeException {
    private final String serviceId;

    public ExecuteException(String serviceId) {
        this.serviceId = serviceId;
    }

    public ExecuteException(String message, String serviceId) {
        super(message);
        this.serviceId = serviceId;
    }

    public ExecuteException(String message, Throwable cause, String serviceId) {
        super(message, cause);
        this.serviceId = serviceId;
    }

    public ExecuteException(Throwable cause, String serviceName) {
        super(cause);
        this.serviceId = serviceName;
    }

    public ExecuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String serviceId) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }
}
