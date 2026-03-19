package io.autoflow.agent;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.List;

/**
 * LangChain4j-based Reasoner implementation with streaming support.
 */
public class LangChainReasoner implements Reasoner {

    private final StreamingChatModel streamingChatModel;

    public LangChainReasoner(StreamingChatModel streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public void think(AgentContext context, StreamListener listener) {
        List<ChatMessage> messages = context.getMessages().stream()
                .map(this::toLangChainMessage)
                .toList();

        streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                listener.onToken(partialResponse);
            }

            @Override
            public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                listener.onComplete();
            }

            @Override
            public void onError(Throwable error) {
                listener.onError(error);
            }
        });
    }

    private ChatMessage toLangChainMessage(io.autoflow.spi.model.ChatMessage chatMessage) {
        return switch (chatMessage.getType()) {
            case USER -> dev.langchain4j.data.message.UserMessage.from(chatMessage.getContent());
            case ASSISTANT -> dev.langchain4j.data.message.AiMessage.from(chatMessage.getContent());
            default -> dev.langchain4j.data.message.SystemMessage.from(chatMessage.getContent());
        };
    }
}
