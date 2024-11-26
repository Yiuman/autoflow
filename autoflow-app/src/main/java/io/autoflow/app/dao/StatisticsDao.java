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
            SELECT concat('COUNT_',flow_state) AS metric, COUNT(*) AS quantity
            FROM af_workflow_inst  group by flow_state
            """)
    List<Map<String, Object>> countOverview();

    @Select("""
            SELECT SERVICE_ID, TOTAL, FAIL, (TOTAL - FAIL) AS SUCCESS
                  FROM (SELECT SERVICE_ID, COUNT(*) AS TOTAL, COUNT(ERROR_MESSAGE) AS FAIL
                        FROM AF_EXECUTION_INST
                        GROUP BY SERVICE_ID)
                        COUNT_EXECUTION
            """)
    List<Map<String, Object>> countExecutionGroupByService();
}