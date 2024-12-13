package io.autoflow.plugin.loopeachitem;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.lang.reflect.MethodHandleUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author yiuman
 * @date 2024/4/8
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class LoopItem extends HashMap<String, Object> {
    private String id;
    private Integer loopCounter;
    /**
     * 循环项的变量（当是loopCardinality时此值为loopCounter）
     */
    private Object elementVariable;
    /**
     * 是否串行
     */
    private Boolean sequential = false;
    private Integer nrOfInstances;
    private LoopItem preLoop;
    private String completionCondition;

    public String getLoopKey() {
        return StrUtil.format("{}_{}", id, getLoopCounter());
    }


    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (Objects.isNull(value)) {
            String keyStr = StrUtil.toString(key);
            PropertyDescriptor propertyDescriptor = BeanUtil.getPropertyDescriptor(LoopItem.class, keyStr);
            Method readMethod = propertyDescriptor.getReadMethod();
            if (Objects.nonNull(readMethod)) {
                value = MethodHandleUtil.invoke(this, readMethod);
            }
            put(keyStr, value);
        }
        return value;
    }

    public void setId(String id) {
        this.id = id;
        put(LambdaUtil.getFieldName(LoopItem::getId), id);
    }

    public void setLoopCounter(Integer loopCounter) {
        this.loopCounter = loopCounter;
        put(LambdaUtil.getFieldName(LoopItem::getLoopCounter), loopCounter);
    }

    public void setElementVariable(Object elementVariable) {
        this.elementVariable = elementVariable;
        put(LambdaUtil.getFieldName(LoopItem::getElementVariable), elementVariable);
    }

    public void setSequential(Boolean sequential) {
        this.sequential = sequential;
        put(LambdaUtil.getFieldName(LoopItem::getSequential), sequential);
    }

    public void setNrOfInstances(Integer nrOfInstances) {
        this.nrOfInstances = nrOfInstances;
        put(LambdaUtil.getFieldName(LoopItem::getNrOfInstances), nrOfInstances);
    }

    public void setPreLoop(LoopItem preLoop) {
        this.preLoop = preLoop;
        put(LambdaUtil.getFieldName(LoopItem::getPreLoop), preLoop);
    }
}
