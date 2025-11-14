package io.autoflow.plugin.function;

import com.ql.util.express.DefaultContext;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.exception.ExecuteException;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.utils.ExpressUtils;

/**
 * @author yiuman
 * @date 2025/11/14
 */
public class FunctionService extends BaseService<FunctionInput, FunctionOutput> {
    @Override
    public String getName() {
        return "Function";
    }

    @Override
    public FunctionOutput execute(FunctionInput functionInput, ExecutionContext executionContext) {
        String fn = functionInput.getFunction();

        // 自动构造 "fn(arg1,arg2,...)"
        StringBuilder script = new StringBuilder();
        script.append(fn).append("(");
        if (functionInput.getArgs() != null) {
            for (int i = 0; i < functionInput.getArgs().size(); i++) {
                if (i > 0) {
                    script.append(",");
                }
                script.append("arg").append(i);
            }
        }

        script.append(")");
        // 构造上下文
        DefaultContext<String, Object> expressCtx = new DefaultContext<>();
        for (int i = 0; i < functionInput.getArgs().size(); i++) {
            expressCtx.put("arg" + i, functionInput.getArgs().get(i));
        }

        try {
            Object result = ExpressUtils.expressRunner().execute(
                    script.toString(),
                    expressCtx,
                    null,
                    true,
                    true
            );

            FunctionOutput output = new FunctionOutput();
            output.setResult(result);
            return output;

        } catch (Throwable throwable) {
            throw new ExecuteException(throwable, getId());
        }
    }
}
