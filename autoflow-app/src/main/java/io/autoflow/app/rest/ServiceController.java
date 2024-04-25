package io.autoflow.app.rest;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.autoflow.common.http.R;
import io.autoflow.common.utils.WebUtils;
import io.autoflow.core.Services;
import io.autoflow.spi.Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yiuman
 * @date 2023/7/27
 */
@RestController
@RequestMapping("/services")
public class ServiceController {

    private static final Map<String, byte[]> SERVICE_SVG_CACHE = new ConcurrentHashMap<>();

    @GetMapping
    public R<List<Service>> services() {
        return R.ok(Services.getServiceList());
    }

    @GetMapping("/image/{serviceId}")
    public void svg(@PathVariable("serviceId") String serviceId, HttpServletResponse httpServletResponse) throws IOException {
        if (!SERVICE_SVG_CACHE.containsKey(serviceId)) {
            try {
                SERVICE_SVG_CACHE.put(serviceId, ResourceUtil.readBytes(StrUtil.format("{}.svg", serviceId)));
            } catch (Throwable throwable) {
                SERVICE_SVG_CACHE.put(serviceId, new byte[]{});
            }

        }

        byte[] bytes = SERVICE_SVG_CACHE.get(serviceId);
        if (ArrayUtil.isNotEmpty(bytes)) {
            httpServletResponse.setContentType("image/svg+xml");
            WebUtils.export(new ByteArrayInputStream(bytes), StrUtil.format("{}.svg", serviceId.replaceAll("\\.", "_")));
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            httpServletResponse.flushBuffer();
        }

    }
}
