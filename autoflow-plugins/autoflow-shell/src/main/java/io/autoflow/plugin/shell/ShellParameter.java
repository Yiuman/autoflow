package io.autoflow.plugin.shell;

import io.autoflow.spi.annotation.Code;
import io.autoflow.spi.model.FileData;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2025/1/22
 */
@Data
public class ShellParameter {
    private List<String> args;
    @Code(lang = "shell")
    private String shellRaw;
    private FileData shellFile;
    private Long duration;
}
