package io.autoflow.plugin.sql;

import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.db.handler.EntityListHandler;
import cn.hutool.db.sql.SqlExecutor;
import cn.hutool.setting.Setting;
import io.autoflow.spi.context.ExecutionContext;
import io.autoflow.spi.exception.ExecuteException;
import io.autoflow.spi.impl.BaseService;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;

import java.sql.Connection;

/**
 * @author yiuman
 * @date 2024/3/1
 */
public class SqlService extends BaseService<SqlParameter, SqlResult> {

    @Override
    public String getName() {
        return "SQL";
    }

    @Override
    public SqlResult execute(SqlParameter sqlParameter, ExecutionContext ctx) {
        try (DSFactory dsFactory = DSFactory.create(buildSetting(sqlParameter));
             Connection connection = dsFactory.getDataSource().getConnection()) {
            String sql = sqlParameter.getSql();
            Statement statement = CCJSqlParserUtil.parse(sql);
            SqlResult sqlResult = new SqlResult();
            if (statement instanceof Select) {
                sqlResult.setRows(SqlExecutor.query(connection, sql, new EntityListHandler()));
            } else {
                sqlResult.setAffectedRows(SqlExecutor.execute(connection, sql));

            }
            return sqlResult;
        } catch (Throwable throwable) {
            throw new ExecuteException(throwable, getId());
        }

    }

    private Setting buildSetting(SqlParameter sqlParameter) {
        Setting setting = Setting.create();
        setting.put(LambdaUtil.getFieldName(SqlParameter::getUrl), sqlParameter.getUrl());
        setting.put(LambdaUtil.getFieldName(SqlParameter::getUsername), sqlParameter.getUsername());
        setting.put(LambdaUtil.getFieldName(SqlParameter::getPassword), sqlParameter.getPassword());
        setting.put(LambdaUtil.getFieldName(SqlParameter::getDriver), sqlParameter.getDriver());
        return setting;
    }
}
