package io.autoflow.app.rest;

import com.mybatisflex.core.query.QueryWrapper;
import io.autoflow.app.model.Workflow;
import io.autoflow.app.query.WorkflowQuery;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yiuman
 * @date 2024/4/28
 */
@RestController
@RequestMapping("/workflows")
@Query(WorkflowQuery.class)
public class WorkflowController implements BaseRESTAPI<Workflow> {

    @Override
    public QueryWrapper buildWrapper(HttpServletRequest request) {
        QueryWrapper queryWrapper = BaseRESTAPI.super.buildWrapper(request);
        queryWrapper.orderBy(Workflow::getCreateTime, false);
        return queryWrapper;
    }
}
