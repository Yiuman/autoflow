package io.autoflow.app.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author yiuman
 * @date 2024/11/25
 */
@Mapper
public interface StatisticsDao {

    @Select("""
            SELECT 'COUNT_DEF' AS metric, count(*) AS quantity
            FROM af_workflow
            UNION ALL
            SELECT 'COUNT_INST' AS metric, count(1) AS quantity
            FROM af_workflow_inst
            UNION ALL
            SELECT concat('COUNT_',flow_state) AS metric, COUNT(CASE WHEN flow_state = 'START' THEN 1 END) AS quantity
            FROM af_workflow_inst  group by flow_state
            """)
    List<Map<String, Object>> countOverview();
}