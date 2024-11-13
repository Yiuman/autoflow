package io.autoflow.plugin.llm;

import io.autoflow.spi.model.ChatMessage;
import io.autoflow.spi.model.Linkage;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/9/26
 */
@Data
public class LlmParameter {
    @NotNull
    private Linkage<String> model = Linkage.from("gpt-3.5-turbo");
    @NotEmpty
    private List<ChatMessage> messages = List.of(new ChatMessage());
}
