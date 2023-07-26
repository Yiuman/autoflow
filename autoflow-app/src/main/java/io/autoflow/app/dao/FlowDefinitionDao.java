package io.autoflow.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.autoflow.app.entity.FlowDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yiuman
 * @date 2023/7/26
 */
@Mapper
public interface FlowDefinitionDao extends BaseMapper<FlowDefinition> {
}