package io.autoflow.plugin.knowledgeretrieval;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.spi.data.document.parser.DocumentParserFactory;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.FileData;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.spi.ServiceHelper.loadFactories;

/**
 * @author yiuman
 * @date 2024/9/23
 */
public class KnowledgeRetrievalService extends BaseService<KnowledgeRetrievalParameter, List<EmbeddingMatchInfo>> {

    private static final DocumentParser DEFAULT_DOCUMENT_PARSER = getOrDefault(loadDocumentParser(), TextDocumentParser::new);
    private static final int MAX_SEGMENT_SIZE_IN_CHARS = 1000;
    private static final int MAX_OVERLAP_SIZE_IN_CHARS = 0;

    private static DocumentParser loadDocumentParser() {
        Collection<DocumentParserFactory> factories = loadFactories(DocumentParserFactory.class);
        if (factories.size() > 1) {
            throw new RuntimeException("Conflict: multiple document parsers have been found in the classpath. "
                    + "Please explicitly specify the one you wish to use.");
        }

        for (DocumentParserFactory factory : factories) {
            return factory.create();
        }

        return null;
    }

    @Override
    public String getName() {
        return "KnowledgeRetrieval";
    }

    @Override
    public List<EmbeddingMatchInfo> execute(KnowledgeRetrievalParameter knowledgeRetrievalParameter,
                                            ExecutionContext executionContext) {
        //todo 适配多种类型的Embeddings
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        FileData fileData = knowledgeRetrievalParameter.getFileData();
        BytesDocumentSource bytesDocumentSource = new BytesDocumentSource(fileData.getContent());
        Document document = DocumentLoader.load(bytesDocumentSource, DEFAULT_DOCUMENT_PARSER);
        EmbeddingStoreIngestor embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(MAX_SEGMENT_SIZE_IN_CHARS, MAX_OVERLAP_SIZE_IN_CHARS))
                .build();
        embeddingStoreIngestor.ingest(document);

        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(embeddingModel.embed(
                        knowledgeRetrievalParameter.getQuery()
                ).content())
                .maxResults(knowledgeRetrievalParameter.getMaxResult())
                .build();
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(embeddingSearchRequest);
        return search.matches().stream().map(EmbeddingMatchInfo::new).collect(Collectors.toList());

    }
}
