package io.autoflow.liteflow.cmp;

import com.yomahub.liteflow.core.NodeIteratorComponent;
import io.autoflow.core.model.Loop;
import io.autoflow.spi.context.FlowExecutionContext;
import io.autoflow.spi.provider.ExecutionContextValueProvider;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * @author yiuman
 * @date 2024/4/12
 */
@Component
public class LoopNodeComponent extends NodeIteratorComponent {

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<?> processIterator() {
        Loop loop = this.getCmpData(Loop.class);
        if (Objects.nonNull(loop.getLoopCardinality())) {
            return IntStream.range(0, loop.getLoopCardinality()).mapToObj(item -> Map.of(loop.getElementVariable(), item)).iterator();
        } else {
            String collectionString = loop.getCollectionString();
            FlowExecutionContext contextBean = getContextBean(FlowExecutionContext.class);
            List<Object> objectList = (List<Object>) new ExecutionContextValueProvider(contextBean).get(collectionString);
            return objectList.stream().map(obj -> Map.of(loop.getElementVariable(), obj)).iterator();
        }
    }
}
