package io.autoflow.plugin.knowledgeretrieval;

import io.autoflow.spi.model.FileData;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/9/23
 */
@Data
public class KnowledgeRetrievalParameter {
    @NotBlank
    private String query;
    @NotNull
    private FileData fileData;
    @Min(1)
    private Integer maxResult = 1;
}
