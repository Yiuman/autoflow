package io.autoflow.plugin.knowledgeretrieval;

import cn.hutool.core.io.IoUtil;
import dev.langchain4j.data.document.DocumentSource;
import dev.langchain4j.data.document.Metadata;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author yiuman
 * @date 2024/9/23
 */
public class BytesDocumentSource implements DocumentSource {
    private final InputStream inputStream;
    @Setter
    private Metadata metadata = new Metadata();

    public BytesDocumentSource(byte[] bytes) {
        this.inputStream = IoUtil.toStream(bytes);
    }

    @Override
    public InputStream inputStream() throws IOException {
        return inputStream;
    }

    @Override
    public Metadata metadata() {
        return metadata;
    }
}
