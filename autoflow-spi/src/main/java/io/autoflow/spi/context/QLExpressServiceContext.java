package io.autoflow.spi.context;

import com.ql.util.express.IExpressContext;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2023/7/25
 */
public class QLExpressServiceContext extends HashMap<String, Object> implements IExpressContext<String, Object> {

    private final ExecutionContext executionContext;

    public QLExpressServiceContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public Object get(Object name) {
        Object result = super.get(name);
        if (Objects.nonNull(result)) {
            return result;
        }
        return executionContext.getParameters().get(name);
    }

    @Override
    public Object put(String s, Object o) {
        return super.put(s, o);
    }
}
