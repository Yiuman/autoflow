package io.autoflow.app.model;

import cn.hutool.core.io.IoUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.InputStream;

/**
 * @author yiuman
 * @date 2024/6/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FileResourceStream extends FileResource {
    private byte[] bytes;

    public InputStream getInputStream() {
        return IoUtil.toStream(bytes);
    }
}
