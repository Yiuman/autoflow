package io.autoflow.plugin.knowledgeretrieval;

import io.autoflow.spi.model.FileData;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/9/23
 */
@Data
public class KnowledgeRetrievalParameter {
    private String query;
    private String type;
    private FileData fileData;
    private Integer maxResult = 1;
}
