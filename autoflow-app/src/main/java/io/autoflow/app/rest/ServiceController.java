package io.autoflow.app.rest;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.app.model.ServiceEntity;
import io.autoflow.app.service.ServiceEntityService;
import io.ola.common.utils.WebUtils;
import io.ola.crud.rest.BaseQueryAPI;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author yiuman
 * @date 2023/7/27
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController implements BaseQueryAPI<ServiceEntity> {

    private final ServiceEntityService serviceEntityService;

    @GetMapping("/image/{serviceId}")
    public void svg(@PathVariable("serviceId") String serviceId, HttpServletResponse httpServletResponse) throws IOException {
        byte[] bytes = serviceEntityService.getImageBytesByServiceId(serviceId);
        if (ArrayUtil.isNotEmpty(bytes)) {
            httpServletResponse.setContentType("image/svg+xml");
            WebUtils.export(new ByteArrayInputStream(bytes), StrUtil.format("{}.svg", serviceId.replaceAll("\\.", "_")));
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            httpServletResponse.flushBuffer();
        }

    }
}
