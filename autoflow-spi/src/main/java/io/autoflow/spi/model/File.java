package io.autoflow.spi.model;

import lombok.Data;

/**
 * @author yiuman
 * @date 2023/7/13
 */
@Data
public class File {
    private byte[] binary;
    private String filename;
}
