package io.autoflow.plugin.ifplugin;

import io.autoflow.plugin.ifplugin.enums.CalcType;
import lombok.Data;

/**
 * @author yiuman
 * @date 2024/4/16
 */
@Data
public class IfParameter {
    private Condition condition;

    public static IfParameter express(String express) {
        IfParameter expressIfParameter = new IfParameter();
        Condition cdt = new Condition();
        cdt.setCalcType(CalcType.Express);
        cdt.setValue(express);
        expressIfParameter.setCondition(cdt);
        return expressIfParameter;
    }
}
