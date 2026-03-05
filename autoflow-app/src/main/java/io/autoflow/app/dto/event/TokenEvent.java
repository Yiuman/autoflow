package io.autoflow.app.dto.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Event representing a streaming token from LLM response.
 * Used for real-time token-by-token streaming of AI responses.
 *
 * @author autoflow
 * @date 2025/03/05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TokenEvent extends ChatEvent {
    /**
     * Event type constant for token events
     */
    public static final String TYPE_TOKEN = "token";

    /**
     * The token content from the streaming response
     */
    private String token;

    /**
     * Constructs a new TokenEvent.
     *
     * @param sessionId the session ID
     * @param token     the token content
     */
    public TokenEvent(final String sessionId, final String token) {
        super(TYPE_TOKEN, sessionId);
        this.token = token;
    }
}
