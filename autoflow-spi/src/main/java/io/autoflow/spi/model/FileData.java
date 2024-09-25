package io.autoflow.spi.model;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;


/**
 * @author yiuman
 * @date 2023/7/13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileData {
    private String filename;
    private String base64;
    private byte[] content;
    private String fileType;

    public FileData(String filename, byte[] content) {
        this.filename = filename;
        this.content = content;
    }

    public static FileData fromPath(String path) {
        return new FileData(
                FileUtil.getName(path),
                ResourceUtil.readBytes(path)
        );
    }

    public byte[] getContent() {
        if (Objects.isNull(content) && StrUtil.isNotBlank(base64)) {
            content = Base64Decoder.decode(base64);
        }
        return content;
    }
}
