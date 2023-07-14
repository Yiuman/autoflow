package io.autoflow.spi;

import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.model.ExecutionData;
import io.autoflow.spi.model.Property;

import java.util.List;

/**
 * 节点定义信息
 *
 * @author yiuman
 * @date 2023/7/11
 */
public interface Service {

    String getName();

    List<Property> getProperties();

    String getDescription();

    List<ExecutionData> execute(ExecutionContext executionContext);

}