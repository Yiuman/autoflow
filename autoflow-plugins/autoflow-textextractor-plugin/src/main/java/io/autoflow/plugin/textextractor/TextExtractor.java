package io.autoflow.plugin.textextractor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.exception.ExecuteException;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.FileData;
import io.docod.core.utils.OfficeUtils;

/**
 * @author yiuman
 * @date 2024/10/26
 */
public class TextExtractor extends BaseService<TextExtractParameter, TextExtractResult> {
    @Override
    public String getName() {
        return "TextExtractor";
    }

    @Override
    public TextExtractResult execute(TextExtractParameter textExtractParameter, ExecutionContext executionContext) {
        FileData file = textExtractParameter.getFile();
        byte[] content = file.getContent();
        String fileType = FileUtil.getSuffix(file.getFilename());
        try {
            return TextExtractResult.builder()
                    .text(OfficeUtils.toText(IoUtil.toStream(content), fileType.toLowerCase()))
                    .build();
        } catch (Throwable throwable) {
            throw new ExecuteException(throwable, getId());
        }

    }
}
