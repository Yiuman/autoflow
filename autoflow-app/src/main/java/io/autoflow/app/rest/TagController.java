package io.autoflow.app.rest;

import io.autoflow.app.model.Tag;
import io.autoflow.app.query.TagQuery;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yiuman
 * @date 2024/5/14
 */
@RequestMapping("/tags")
@RestController
@Query(TagQuery.class)
public class TagController implements BaseRESTAPI<Tag> {
}
