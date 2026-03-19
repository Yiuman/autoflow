package io.autoflow.agent.core.parser;

/**
 * Exception thrown when JSON action parsing fails.
 *
 * @author yiuman
 * @date 2024/10/11
 */
public class ActionParseException extends RuntimeException {

    public ActionParseException(String message) {
        super(message);
    }

    public ActionParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
