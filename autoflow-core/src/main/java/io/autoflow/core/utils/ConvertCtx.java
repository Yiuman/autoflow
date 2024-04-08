package io.autoflow.core.utils;

import io.autoflow.core.model.Flow;
import io.autoflow.core.model.Node;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ConvertCtx {
    private Flow flow;
    private Node currentNode;
}
