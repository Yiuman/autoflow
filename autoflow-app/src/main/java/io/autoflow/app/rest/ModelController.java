package io.autoflow.app.rest;

import io.autoflow.app.model.Model;
import io.autoflow.app.query.ModelQuery;
import io.ola.crud.query.annotation.Query;
import io.ola.crud.rest.BaseRESTAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/models")
@Query(ModelQuery.class)
public class ModelController implements BaseRESTAPI<Model> {
}
