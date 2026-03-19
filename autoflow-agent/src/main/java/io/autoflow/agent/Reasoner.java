package io.autoflow.agent;

public interface Reasoner {
    void think(AgentContext context, StreamListener listener);
}
