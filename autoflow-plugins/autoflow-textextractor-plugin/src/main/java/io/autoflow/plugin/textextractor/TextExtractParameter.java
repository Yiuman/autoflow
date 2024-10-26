package io.autoflow.plugin.textextractor;

import io.autoflow.spi.model.FileData;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/10/26
 */
@Data
public class TextExtractParameter {
    @NotNull
    private FileData file;
}
