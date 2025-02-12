package io.autoflow.plugin.uncompress;

import io.autoflow.spi.model.FileData;
import lombok.Data;

/**
 * @author yiuman
 * @date 2025/2/12
 */
@Data
public class UncompressParameter {
    private FileData file;
}
