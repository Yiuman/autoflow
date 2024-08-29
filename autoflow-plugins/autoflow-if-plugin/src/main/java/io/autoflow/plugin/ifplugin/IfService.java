package io.autoflow.plugin.ifplugin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.plugin.ifplugin.enums.CalcType;
import io.autoflow.plugin.ifplugin.enums.Clause;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.utils.ExpressUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author yiuman
 * @date 2024/3/18
 */
public class IfService extends BaseService<IfParameter, IfResult> {

    private static final BiFunction<ExecutionContext, Condition, String> DEFAULT_CALC_TYPE_FUNC = (ctc, condition)
            -> {
        if (Objects.isNull(condition.getDataKey()) && Objects.isNull(condition.getValue())) {
            return null;
        }
        return StrUtil.format(
                "{} {} {}",
                ExpressUtils.convertCtxExpressStr(condition.getDataKey()),
                condition.getCalcType().getSymbol(),
                ExpressUtils.convertCtxExpressStr(condition.getValue()));

    };

    private static final Map<CalcType, BiFunction<ExecutionContext, Condition, String>> CALC_TYPE_FUNC_MAP = new HashMap<>() {{
        put(CalcType.Express, (ctc, condition) -> ExpressUtils.convertCtxExpressStr(condition.getValue()));
        put(CalcType.Empty, (ctc, condition) -> ExpressUtils.isEmptyExpress(ExpressUtils.convertCtxExpressStr(condition.getValue())));
        put(CalcType.NotEmpty, (ctc, condition) -> ExpressUtils.isNotEmptyExpress(ExpressUtils.convertCtxExpressStr(condition.getValue())));
    }};

    @Override
    public String getName() {
        return "IF";
    }

    @Override
    public IfResult execute(IfParameter ifParameter, ExecutionContext ctx) {
        String conditionStr = parseCondition(ctx, ifParameter.getCondition());
        Object conditionBooleanValue = ctx.parseValue(String.format("${%s}", conditionStr));
        return new IfResult(
                conditionStr,
                BooleanUtil.toBoolean(StrUtil.toString(conditionBooleanValue))
        );

    }


    private String parseCondition(ExecutionContext ctx, Condition condition) {
        StringBuilder stringBuilder = new StringBuilder();
        CalcType calcType = condition.getCalcType();
        if (Objects.nonNull(calcType)) {
            BiFunction<ExecutionContext, Condition, String> conditionStringFunction = CALC_TYPE_FUNC_MAP
                    .getOrDefault(calcType, DEFAULT_CALC_TYPE_FUNC);
            stringBuilder.append(conditionStringFunction.apply(ctx, condition));
        }

        List<Condition> children = condition.getChildren();
        if (CollUtil.isNotEmpty(children) && CollUtil.size(children) > 0) {
            List<String> conditionStrs = children.stream()
                    .map(child -> parseCondition(ctx, child)).toList();
            stringBuilder.append("(")
                    .append(conditionStrs.stream()
                            .filter(StrUtil::isNotBlank)
                            .collect(Collectors.joining(Clause.AND == condition.getClause() ? " && " : " || ")))
                    .append(")");
        }
        return stringBuilder.toString();

    }
}
