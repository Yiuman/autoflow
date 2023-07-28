package io.autoflow.app.rest;

import io.autoflow.common.http.R;
import io.autoflow.core.Services;
import io.autoflow.spi.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yiuman
 * @date 2023/7/27
 */
@RestController
@RequestMapping("/nodes")
public class NodeController {

    @GetMapping
    public R<List<Service>> services() {
        return R.ok(Services.getServiceList());
    }
}
