package io.autoflow.liteflow.cmp;

import cn.hutool.core.bean.BeanUtil;
import com.yomahub.liteflow.core.NodeIteratorComponent;
import io.autoflow.core.model.Loop;
import io.autoflow.plugin.loopeachitem.LoopItem;
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
            Integer loopCardinality = loop.getLoopCardinality();
            return IntStream.range(0, loopCardinality)
                    .mapToObj(i -> createLoopItemVariables(loop, i, i))
                    .iterator();
        } else {
            String collectionString = loop.getCollectionString();
            FlowExecutionContext contextBean = getContextBean(FlowExecutionContext.class);
            List<Object> objectList = (List<Object>) new ExecutionContextValueProvider(contextBean).get(collectionString);
            int size = objectList.size();
            loop.setLoopCardinality(size);
            return objectList.stream().map(obj -> createLoopItemVariables(loop, obj, objectList.indexOf(obj))).iterator();
        }
    }

    private Map<String, Object> createLoopItemVariables(Loop loop, Object object, Integer index) {
        LoopItem loopItem = new LoopItem();
        loopItem.setElementVariable(object);
        loopItem.setNrOfInstances(loop.getLoopCardinality());
        loopItem.setSequential(loop.getSequential());
        loopItem.setLoopCounter(index);
        Map<String, Object> variables = BeanUtil.beanToMap(loopItem);
        variables.put(loop.getElementVariable(), object);
        return variables;
    }

}