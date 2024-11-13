package io.autoflow.plugin.knowledgeretrieval;

import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/9/25
 */
@Data
public class EmbeddingMatchInfo {
    private Double score;
    private String embeddingId;
    private float[] vector;
    private TextSegment textSegment;

    public EmbeddingMatchInfo(EmbeddingMatch<dev.langchain4j.data.segment.TextSegment> embeddingMatch) {
        this.score = embeddingMatch.score();
        this.embeddingId = embeddingMatch.embeddingId();
        this.vector = embeddingMatch.embedding().vector();
        dev.langchain4j.data.segment.TextSegment embedded = embeddingMatch.embedded();
        this.textSegment = new TextSegment(
                embedded.text(),
                embedded.metadata().toMap()
        );
    }
}
