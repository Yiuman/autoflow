package io.autoflow.plugin.sql;

import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.handler.EntityListHandler;
import cn.hutool.db.sql.SqlExecutor;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import io.autoflow.spi.exception.ExecuteException;
import io.autoflow.spi.impl.BaseService;
import io.autoflow.spi.model.ExecutionData;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.sql.Connection;
import java.util.List;

/**
 * @author yiuman
 * @date 2024/3/1
 */
public class SqlService extends BaseService<SqlParameter> {

    @Override
    public String getName() {
        return "SQL";
    }

    @Override
    public ExecutionData execute(SqlParameter sqlParameter) {
        try {
            Setting setting = Setting.create();
            setting.put(LambdaUtil.getFieldName(SqlParameter::getUrl), sqlParameter.getUrl());
            setting.put(LambdaUtil.getFieldName(SqlParameter::getUsername), sqlParameter.getUsername());
            setting.put(LambdaUtil.getFieldName(SqlParameter::getPassword), sqlParameter.getPassword());
            setting.put(LambdaUtil.getFieldName(SqlParameter::getDriver), sqlParameter.getDriver());
            try (DSFactory dsFactory = DSFactory.create(setting);
                 Connection connection = dsFactory.getDataSource().getConnection()) {
                String sql = sqlParameter.getSql();
                Statement statement = CCJSqlParserUtil.parse(sql);
                ExecutionData executionData;
                if (statement instanceof Select) {
                    List<Entity> entities = SqlExecutor.query(connection, sql, new EntityListHandler());
                    executionData = ExecutionData.builder()
                            .json(JSONUtil.parseArray(entities))
                            .build();
                } else {
                    int execute = SqlExecutor.execute(connection, sql);
                    executionData = ExecutionData.builder()
                            .json(JSONUtil.createObj().set("hit", execute))
                            .build();
                }
                return executionData;
            }
        } catch (Throwable throwable) {
            throw new ExecuteException(throwable, getId());
        }

    }
}
