package io.autoflow.plugin.uncompress;

import lombok.Data;

/**
 * @author yiuman
 * @date 2024/4/10
 */
@Data
public class CompressFileItem {
    private String path;
    private boolean folder;
    private byte[] bytes;
}
