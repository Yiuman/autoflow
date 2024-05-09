package io.autoflow.app.rest;

import io.autoflow.app.model.GlobalVariable;
import io.ola.crud.rest.BaseRESTAPI;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局变量接口
 *
 * @author yiuman
 * @date 2024/5/9
 */
@RequestMapping("/variables")
@RestController
public class GlobalVariableController implements BaseRESTAPI<GlobalVariable> {
}
