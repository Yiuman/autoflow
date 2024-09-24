package io.autoflow.plugin.knowledgeretrieval;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.data.segment.TextSegment;
import io.autoflow.spi.model.FileData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;


/**
 * @author yiuman
 * @date 2024/9/23
 */
@Slf4j
class KnowledgeRetrievalServiceTest {

    @Test
    public void testKnowledgeRetrieval() {
        KnowledgeRetrievalService knowledgeRetrievalService = new KnowledgeRetrievalService();
        KnowledgeRetrievalParameter knowledgeRetrievalParameter = new KnowledgeRetrievalParameter();
        knowledgeRetrievalParameter.setQuery("What is your favourite sport?");
        knowledgeRetrievalParameter.setFileData(FileData.fromPath("test.txt"));
        List<TextSegment> execute = knowledgeRetrievalService.execute(knowledgeRetrievalParameter);
        log.info(JSONUtil.toJsonStr(execute));
        Assert.isTrue(CollUtil.isNotEmpty(execute));
    }
}