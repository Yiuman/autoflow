package io.autoflow.app.query;

import io.ola.crud.query.annotation.Equals;
import lombok.Data;

@Data
public class ChatSessionQuery {
    @Equals
    private String status;
}
