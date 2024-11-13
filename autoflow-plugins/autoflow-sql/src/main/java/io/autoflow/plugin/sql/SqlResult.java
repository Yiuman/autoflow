package io.autoflow.plugin.sql;

import cn.hutool.db.Entity;
import lombok.Data;

import java.util.List;

/**
 * @author yiuman
 * @date 2024/8/29
 */
@Data
public class SqlResult {
    private List<Entity> rows;
    private int affectedRows;
}
