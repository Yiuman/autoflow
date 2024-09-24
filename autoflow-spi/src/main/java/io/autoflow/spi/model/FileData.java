package io.autoflow.spi.model;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author yiuman
 * @date 2023/7/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileData {
    private String filename;
    private byte[] content;

    public static FileData fromPath(String path) {
        return new FileData(
                FileUtil.getName(path),
                ResourceUtil.readBytes(path)
        );
    }
}
