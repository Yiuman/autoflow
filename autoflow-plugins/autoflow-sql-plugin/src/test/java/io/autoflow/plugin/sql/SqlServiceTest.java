package io.autoflow.plugin.sql;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author yiuman
 * @date 2024/3/1
 */
@Slf4j
class SqlServiceTest {

    @Test
    public void testSelect() {
        SqlService sqlService = new SqlService();
        SqlParameter sqlParameter = new SqlParameter();
        sqlParameter.setSql("select * from sys_user");
        sqlParameter.setUrl("jdbc:mysql://localhost:3306/ola?allowPublicKeyRetrieval=true&useSSL=false");
        sqlParameter.setUsername("root");
        sqlParameter.setPassword("123456");
        SqlResult sqlResult = sqlService.execute(sqlParameter, null);
        log.info(JSONUtil.toJsonStr(sqlResult));
    }

    @Test
    public void testInsert() {
        SqlService sqlService = new SqlService();
        SqlParameter sqlParameter = new SqlParameter();
        String insertStr = """
                INSERT INTO ola.sys_user (id, password, username, mobile, avatar, status, create_time, creator, update_time,last_modifier)
                VALUES ('123', 'aa98efbb315cd1ad6efa4a4eeac728dd', 'test', null, null, 1,'2023-09-04 10:34:17', null, '2023-09-04 10:34:17', null);
                """;
        sqlParameter.setSql(insertStr);
        sqlParameter.setUrl("jdbc:mysql://localhost:3306/ola?useSSL=false");
        sqlParameter.setUsername("root");
        sqlParameter.setPassword("123456");
        SqlResult execute = sqlService.execute(sqlParameter, null);
        log.info(JSONUtil.toJsonStr(execute));
    }

    @Test
    public void testUpdate() {
        SqlService sqlService = new SqlService();
        SqlParameter sqlParameter = new SqlParameter();
        String updateStr = """
                update sys_user set username = 'test_username' where id = '123';
                """;
        sqlParameter.setSql(updateStr);
        sqlParameter.setUrl("jdbc:mysql://localhost:3306/ola?useSSL=false");
        sqlParameter.setUsername("root");
        sqlParameter.setPassword("123456");
        SqlResult execute = sqlService.execute(sqlParameter, null);
        log.info(JSONUtil.toJsonStr(execute));
    }

    @Test
    public void testDelete() {
        SqlService sqlService = new SqlService();
        SqlParameter sqlParameter = new SqlParameter();
        String updateStr = """
                delete from sys_user where username = 'test_username'
                """;
        sqlParameter.setSql(updateStr);
        sqlParameter.setUrl("jdbc:mysql://localhost:3306/ola?useSSL=false");
        sqlParameter.setUsername("root");
        sqlParameter.setPassword("123456");
        SqlResult execute = sqlService.execute(sqlParameter, null);
        log.info(JSONUtil.toJsonStr(execute));
    }

}